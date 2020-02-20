package q2.b;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.Thread.currentThread;



public class PIncrement implements Runnable {

    private final int left = 0;
    private final int right = 1;
    private final int down = 2;

    static int num;
    static int count = 0;

    volatile static long X = -1;
    volatile static long Y = -1;
    static ConcurrentHashMap<Long, Boolean> flags;

    public static int parallelIncrement(int c, int numThreads) {
        count = c;
        num = numThreads;
        flags = new ConcurrentHashMap<>();
        ArrayList<Thread> threads = new ArrayList<Thread>();

        for(int i = 0; i < numThreads; i++){
            Thread t = new Thread(new PIncrement());
            threads.add(t);
            t.start();
        }
        for(Thread t : threads) {
            try {
                t.join();
            }
            catch(Exception e){
                System.out.println("bad stuff " + e);
            }
        }

        return count;
    }

    @Override
    public void run() {
        long me = currentThread().getId();
        flags.put(me, false);
        for(int i = 0; i < 120000/num; i++){
            acquire(me);
            count++;
            release(me);
        }
    }

    public void acquire(long i) {
        while(true){
            flags.put(i, true);
            X = i;
            if(Y != -1){
                flags.put(i, false);
                while(Y != -1);
                continue;
            }
            else {
                Y = i;
                if (X == i) { return; } //won fast part
                flags.put(i, false);
                boolean allDown = false;
                while(!allDown){
                    allDown = true;
                    for(Boolean flag : flags.values()){
                        if(flag) allDown = false;
                    }
                }
                if(Y == i){
                    return; //won slow
                }
                else{
                    while(Y != -1);
                    continue;
                }
            }
        }
    }
    public void release(long i){
        Y = -1;
        flags.put(i, false);
    }
}

