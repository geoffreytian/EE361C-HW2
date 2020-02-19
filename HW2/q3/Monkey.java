package q3;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Monkey {

    // declare the variables here
    static AtomicBoolean kongHere = new AtomicBoolean();
    static ArrayList<Tuple> tuples;
    static AtomicBoolean madeLocks = new AtomicBoolean(false);
    static boolean madeLocksComplete = false;
    int direction;



    public Monkey() {
        if(madeLocks.compareAndSet(false, true)){
            tuples = new ArrayList<>();
            for(int i = 0; i < 3; i++) {
                ReentrantLock lock = new ReentrantLock();
                Condition cv0 = lock.newCondition();
                Condition cv1 = lock.newCondition();
                Condition cv2 = lock.newCondition();
                Tuple tuple = new Tuple(lock, cv0, cv1, cv2);
                tuples.add(tuple);
            }
            madeLocksComplete = true;
        }
        while(!madeLocksComplete);
    }

    // A monkey calls the method when it arrives at the river bank and
    // wants to climb the rope in the specified direction (0 or 1);
    // Kong’s direction is -1.
    // The method blocks a monkey until it is allowed to climb the rope.

    public void ClimbRope(int direction) throws InterruptedException {
        this.direction = direction;
        if(direction == -1){
            //Kong stuff
            return;
        }
        for(int i = 0; i < 3; i++){
            if(tuples.get(i).getLock().tryLock()){ //see if any locks already open
                //acquired lock
                tuples.get(i).setDirection(direction);
                return;
            }
        }
        //if no locks already open, wait on condition with least waiters
        Tuple t = getLeastWaitersTuple(direction);
        t.getCondition(direction).await();
        t.getLock().lock();
        //acquired lock
        t.setDirection(direction);
        return;
    }

    // After crossing the river, every monkey calls this method which
    // allows other monkeys to climb the rope.

    public void LeaveRope() {
        int i = 0;
        while(!tuples.get(i).getLock().isHeldByCurrentThread()){
            i++;
            assert(i != 3) : "leaving rope without holding lock";
        }
        ReentrantLock myLock = tuples.get(i).getLock();
        assert(myLock.isHeldByCurrentThread()) : "myLock not held by current thread";
        myLock.unlock();

        tuples.get(i).setDirection(-2); //no longer going in a direction.
        tuples.get(i).getCondition(direction).notify(); //notify someone waiting to go this direction
    }

    /**
     * Returns the number of monkeys on the rope currently for test purpose.
     *
     * @return the number of monkeys on the rope
     *
     * Positive Test Cases:
     * case 1: when normal monkey (0 and 1) is on the rope, this value should <= 3, >= 0
     * case 2: when Kong is on the rope, this value should be 1
     */
    public int getNumMonkeysOnRope() {
        int count = 0;
        for(Tuple t : tuples){
            if(t.getLock().isLocked()) count++;
        }
        return count;
    }

    public Tuple getLeastWaitersTuple(int direction) {
        Tuple t0 = tuples.get(0);
        Tuple t1 = tuples.get(1);
        Tuple t2 = tuples.get(2);

        int lock0;
        int lock1;
        int lock2;

        try {
            lock0 = t0.getLock().getWaitQueueLength(t0.getCondition(direction));
        }
        catch(IllegalMonitorStateException e){
            //lock not held
            return tuples.get(0);
        }
        try {
            lock1 = t1.getLock().getWaitQueueLength(t1.getCondition(direction));
        }
        catch(IllegalMonitorStateException e){
            //lock not held
            return tuples.get(1);
        }
        try {
            lock2 = t2.getLock().getWaitQueueLength(t2.getCondition(direction));
        }
        catch(IllegalMonitorStateException e){
            //lock not held
            return tuples.get(2);
        }

        if((lock0 <= lock1) && (lock0 <= lock2)){
            return tuples.get(0);
        }
        if((lock1 <= lock0) && (lock1 <= lock2)){
            return tuples.get(1);
        }
        else{
            return tuples.get(2);
        }
    }

}

class Tuple {

    public ReentrantLock getLock() {
        return lock;
    }

    public void setLock(ReentrantLock lock) {
        this.lock = lock;
    }

    public Condition getCv0() {
        return cv0;
    }

    public void setCv0(Condition cv0) {
        this.cv0 = cv0;
    }

    public Condition getCv1() {
        return cv1;
    }

    public void setCv1(Condition cv1) {
        this.cv1 = cv1;
    }

    public Condition getCv2() {
        return cv2;
    }

    public void setCv2(Condition cv2) {
        this.cv2 = cv2;
    }

    ReentrantLock lock;
    Condition cv0;
    Condition cv1;
    Condition cv2;

    public int getDirection() {
        return direction.get();
    }

    public void setDirection(int direction) {
        this.direction.set(direction);
    }

    AtomicInteger direction;


    public Tuple(ReentrantLock lock, Condition cv0, Condition cv1, Condition cv2){
        this.direction = new AtomicInteger(-2);
        this.cv0 = cv0;
        this.cv1 = cv1;
        this.cv2 = cv2;
        this.lock = lock;
    }

    public Condition getCondition(int direction){
        assert(direction == 0 || direction == 1 || direction == -1) : "direction not -1, 0, or 1";
        if(direction == 0) return cv0;
        if(direction == 1) return cv1;
        else return cv2;
    }
}
