//package com.thread;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ct {
        public static void main(String args[]) throws Exception {
        int cNum = 8;
        int times = 10000000;

        AtomicInteger total = new AtomicInteger(0);

        CombiningTree ct = new CombiningTree(cNum);

        //Thread
// CombiningTree getAndIncrement
        CountDownLatch latch = new CountDownLatch(cNum);
        int res;

        long start = System.currentTimeMillis();
        for (int i = 0; i < cNum; i++) {
            ThreadC t = new ThreadC("tt" + i, latch);
            t.setCT(ct, times, cNum);
            t.start();
        }

        latch.await();
        long end = System.currentTimeMillis();
        long costtime = end - start;
        System.out.println("finish! in " + costtime + "ms");

// default getandincrement
        int res2;
        CountDownLatch latch2 = new CountDownLatch(cNum);

        start = System.currentTimeMillis();
        for (int i = 0; i < cNum; i++) {
            ThreadG t = new ThreadG("gai" + i, latch2, times, cNum, total);
            t.start();
        }

        latch2.await();
        end = System.currentTimeMillis();
        costtime = end - start;
        System.out.println("finish 2! in " + costtime + "ms");
    }


}

class ThreadC extends Thread {
    private CombiningTree ct;
    private int runTimes;
    private int cNum = 8;
    private int vv = 5;
    CountDownLatch latch;
    public void setCT (CombiningTree ct, int times, int cNum) {
        this.ct = ct; 
        this.runTimes = times;
        this.cNum = cNum;

    }
    public ThreadC(String s, CountDownLatch latch) {
        super(s);
        this.latch = latch;
    }
    @Override
    public void run() {
        int res = -1;
        try {
            for (int i = 0; i < this.runTimes/this.cNum/this.vv; i++) {
                res = this.ct.getAndIncrement(vv);
            }
        } catch (Exception e) {
            System.out.println(e);
        }

        latch.countDown();
        System.out.println("res!: " + res);
    }
}
/**/
class ThreadG extends Thread {
    int count = 0;
    private int runTimes;
    private int cNum = 8;
    CountDownLatch latch;
    AtomicInteger total;
    public ThreadG(String s, CountDownLatch latch, int runTimes, int cNum, AtomicInteger total) {
        super(s);
        this.latch = latch;
        this.cNum = cNum;
        this.runTimes = runTimes;
        this.total = total;
    }

    @Override
    public void run() {
        ReentrantLock lock = new ReentrantLock();

        for (int i = 0; i < this.runTimes/this.cNum; i ++) {
            this.total.getAndIncrement();
            // synchronized(this) {
            //     this.count++;
            // }
            // lock.lock();
            // try {
            //     this.count++;
            // } finally {
            //     lock.unlock();
            // }
        }

        System.out.println("res2: " + total);
        //System.out.println("res2: " + count);
        latch.countDown();
    }
}