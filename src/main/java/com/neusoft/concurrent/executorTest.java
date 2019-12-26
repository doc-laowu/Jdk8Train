package com.neusoft.concurrent;

import java.util.ArrayList;
import java.util.concurrent.*;

/**
 * @Title: executorTest
 * @ProjectName MyJdbcSink
 * @Description: TODO
 * @Author yisheng.wu
 * @Date 2019/8/1418:47
 */
public class executorTest {

    public static void main(String[] args) {

        ExecutorService executor = Executors.newCachedThreadPool();

        ArrayList<Callable> list = new ArrayList<>();

        for(int i=0; i<5; i++) {
//            executor.execute(new liftOff());
            executor.submit(new CallableDemo());
            list.add(new CallableDemo());
        }
        executor.shutdown();
    }

    public static class liftOff implements Runnable{

        @Override
        public void run() {
            for(int i=0; i< 10; i++){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("-------->"+i);
            }

            System.out.println("<------------------->");
        }
    }

    public static class CallableDemo implements Callable<Integer> {

        @Override
        public Integer call() throws Exception {

            int ret = 0;

            for (int i = 0; i < 10; i++) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("-------->" + i);
                ret = i;
            }

            System.out.println("<------------------->");

            return ret;
        }
    }

}
