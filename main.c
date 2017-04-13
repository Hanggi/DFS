#include <stdio.h>
#include <sys/time.h>
#include <pthread.h>
#include <stdlib.h>

#define DEBUG

long count = 0;
int total = 100000000;
int Tlock = 0;

struct thread_args {
	int thread_n;
	int opt;
};

int wrap(int lock) {
	int ret;
	//printf("%d", lock);
	ret = __sync_lock_test_and_set(&lock, 1);
	//printf("%d-%d\n", lock, ret);
	return ret;
}

void TLock(int *lock) {
	while (__sync_lock_test_and_set(lock, 1) == 1) {
	//while(wrap(lock) == 1) {
		//printf("in spin\n");
	}
}

void TTLock(int *lock) {
	
	while (1) {
		while (*lock);
		if (__sync_lock_test_and_set(lock, 1) == 0) {
			return;
		}
	}
}

void TUnlock(int *lock) {
	__sync_lock_release(lock, 0);
}

void *process_request(void *vargs) {
	struct thread_args *args = (struct thread_args *)vargs;

	int i;
	int tt = total / args->thread_n;
	int ret = 0;
	int opt = args->opt;

	//printf("opt: %d\n", opt);
	//printf("=%d \n", tt);
	if (opt == 1) {
		for(i = 0; i < tt; i++) {
			TLock(&Tlock);
			count++;
			//printf("%d\n", count);
			TUnlock(&Tlock);

		}
		//printf("%ld \n", count);
	} else if (opt == 2) {
		for(i = 0; i < tt; i++) {
			TTLock(&Tlock);
			count++;
			//printf("%d\n", count);
			TUnlock(&Tlock);

		}
	} else if (opt == 3) {
		int tmp, tmp2;
		for (i = 0; i < tt; i++) {
			tmp = count;
			//tmp2 = count + 1;
			ret = __sync_bool_compare_and_swap(&count, tmp, tmp + 1);
			if (!ret) {
				i--;
			}
		}
	} else if (opt == 4) {
		for (i = 0; i < tt; i++) {
			__sync_fetch_and_add(&count, 1);
		}
	} else {
		printf("option error!\n");
	}
	//printf("--%ld \n", count);
}


void CS(int n, int opt) {
	int i;
	//int n = 1;
	pthread_t tid[n];
	int ret[n];
	struct thread_args *argsp = (struct thread_args *) malloc(sizeof(struct thread_args));
	argsp->thread_n = n;
	argsp->opt = opt;

	int pi = 0;
	while(pi < n) {
		ret[pi] = pthread_create(&tid[pi], NULL, process_request, (void *)argsp);
		pi++;
	}

	pi = 0;
	while(pi < n) {
		pthread_join(tid[pi], NULL);
		pi++;
	}
}

void testWrap(int opt) {
	int tn[4] = {1, 2, 4, 8};
	int i;

	if (opt == 1)
		printf(" TAS:");
	else if (opt == 2)
		printf("TTAS:");
	else if (opt == 3)
		printf(" CAS:");
	else if (opt == 4)
		printf(" FAA:");

	for (i = 0; i < 4; i++) {
		struct timeval start1, end1;
		gettimeofday(&start1, NULL);
	
		// critical section
		CS(tn[i], opt);

		gettimeofday(&end1, NULL);
		long timeuse1 = 1000000 * (end1.tv_sec - start1.tv_sec) + end1.tv_usec - start1.tv_usec;
		//printf("=>%ld \n", count);
		if (count != total) {
			printf("\nincrement result error! [%d]\n", count);
		}
		count = 0;
		printf("\033[1;36m%11ld\033[0m", timeuse1/1000);
		fflush(stdout);
	}
	printf("\n");
}

void timeCheck() {


	printf("\033[1;33mStart 1~8 thread\033[0m %d \033[1;33mincreasment...\033[0m\n\n", total);
	printf("(ms)  [1 thread] [2 thread] [4 thread] [8 thread]\n");
	testWrap(1);
	testWrap(2);
	testWrap(3);
	testWrap(4);

	int ii;

	struct timeval start1, end1;
	gettimeofday(&start1, NULL);
	for (ii = 0; ii < total; ii++) {
		count++;
	}
	gettimeofday(&end1, NULL);
	long timeuse1 = 1000000 * (end1.tv_sec - start1.tv_sec) + end1.tv_usec - start1.tv_usec;
	printf("\nsingle thread inc: \033[1;32m%ld\033[0m ms - %ld\n", timeuse1/1000, count);
}

int main () {

	timeCheck();
	//int l = 0;
	//TLock(l);

	//printf("count: \033[1;36m%ld\033[0m, time: \033[1;36m%ld\033[0m ms \n\n", count, timeuse1/1000);



	return 0;
}
