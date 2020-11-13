package com.neusoft.concurrent;

import java.util.concurrent.Semaphore;

/**
 * @Title: CyclicBarrierTest
 * @ProjectName Jdk8Train
 * @Description: TODO
 * @Author yisheng.wu
 * @Date 2020/8/715:16
 */
class SemaphoreTest {

    public static void main(String[] args) {
        Semaphore semaphore= new Semaphore( 3 );
        for(int i=0; i<7; i++){
            new SecurityCheckThread(i, semaphore).start();
        }
    }

    private static class SecurityCheckThread extends Thread {
        private int seq;
        private Semaphore semaphore ;

        public SecurityCheckThread(int seq, Semaphore semaphore) {
            this.seq = seq;
            this.semaphore = semaphore;
        }

        @Override
        public void run() {
            try {
                semaphore.acquire();
                System.out.println("No." + seq + "乘客正在查验中");
                if(seq % 2 == 0 ) {
                    Thread.sleep( 1000);
                    System.out.println("No." + seq +"乘客身份可疑");
                }
            } catch (InterruptedException e) {
                e . printStackTrace() ;
            } finally {
                semaphore.release();
                System.out.println("No." + seq + "乘客完成检查");
            }
        }
    }
}