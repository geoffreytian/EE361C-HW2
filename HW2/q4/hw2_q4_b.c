#include <stdio.h>
#include <stdlib.h>
#include <omp.h>
int omp_get_thread_num();
#include <stdbool.h>
#include <math.h>
#include <time.h>

int Sieve(int N, int threads){
    //Write your code here
    omp_set_num_threads(threads);
    bool flags[N];
    int count = 0;
    int rootN = (int)sqrt(N);
    printf("root: %d\n", rootN);
    int i;
#pragma omp parallel
{
    #pragma omp for private(i)
    for(i = 0; i < N; i++){
         flags[i] = true;
    }
    #pragma omp for private(i)
    for(i = 2; i <= rootN; i++){
         if(flags[i] == false) continue;
         int start = i * i;
	 int j;
	 #pragma for private(j)
         for(j = start; j < N; j += i){
             flags[j] = false;
         } 
    }
    #pragma omp master
    for(i = 2; i < N; i++){
	if(flags[i] == true) count++;
    }
}
    return count;
}

void main(void) {
    time_t t = clock();
    int num_primes;
    int num_threads;
    num_threads = 2;
    num_primes = Sieve(1000000, num_threads);
    printf("%d\n", num_primes);
    printf("Time elapsed: %lu\n", (clock() - t));

}
