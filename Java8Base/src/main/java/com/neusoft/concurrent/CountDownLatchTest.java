package com.neusoft.concurrent;

import java.util.concurrent.CountDownLatch;

/**
 * @Title: CountDownLatchTest
 * @ProjectName MyJdbcSink
 * @Description: TODO
 * @Author yisheng.wu
 * @Date 2019/9/3018:41
 */
/*
countDownLatch这个类使一个线程等待其他线程各自执行完毕后再执行。
是通过一个计数器来实现的，计数器的初始值是线程的数量。每当一个线程执行完毕后，计数器的值就-1，当计数器的值为0时，
表示所有线程都执行完毕，然后在闭锁上等待的线程就可以恢复工作了。
*/
public class CountDownLatchTest {

    static class TaskThread extends Thread {

        CountDownLatch count;

        public TaskThread(CountDownLatch count) {
            this.count = count;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(1000);
                System.out.println(getName() + " 到达栅栏 A");
                count.countDown();
                count.await();
                System.out.println(getName() + " 冲破栅栏 A");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        int threadNum = 5;
        CountDownLatch count = new CountDownLatch(threadNum);

        for(int i = 0; i < threadNum; i++) {
            new TaskThread(count).start();
        }
    }

}