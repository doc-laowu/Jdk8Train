package com.neusoft.concurrent;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

/**
 * @Title: FIFOMutex
 * @ProjectName Jdk8Train
 * @Description: TODO
 * @Author yisheng.wu
 * @Date 2020/6/1610:57
 */
class FIFOMutex {
    private final AtomicBoolean locked = new AtomicBoolean(false);
    private final Queue<Thread> waiters = new ConcurrentLinkedQueue<Thread>();

    public void lock() {
        boolean wasInterrupted = false;
        Thread current = Thread.currentThread();
        waiters.add(current);

        // Block while not first in queue or cannot acquire lock
        while (waiters.peek() != current || !locked.compareAndSet(false, true)) {
          LockSupport.park(this);
          if (Thread.interrupted()) // ignore interrupts while waiting
            wasInterrupted = true;
        }

        waiters.remove();
        if (wasInterrupted)          // reassert interrupt status on exit
          current.interrupt();
    }

    public void unlock() {
        locked.set(false);
        LockSupport.unpark(waiters.peek());
    }

    public static long counter = 1000;

    public static void main(String[] args) {

        for(int i=0; i<5; i++){
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    FIFOMutex fifoMutex = new FIFOMutex();
                    try {
                        for (int i = 0; i < 100; i++) {
                            fifoMutex.lock();
                            counter++;;
                        }
                    }finally {
                        fifoMutex.unlock();
                    }
                }
            });
            t.start();
        }

    }

}