package com.neusoft.concurrent;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

/**
 * @Title: Counter
 * @ProjectName MyJdbcSink
 * @Description: TODO
 * @Author yisheng.wu
 * @Date 2019/10/3111:36
 */
public class Counter {

    private volatile int counter = 0;

    private static long offset;
    private static Unsafe unsafe;
    static {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            unsafe = (Unsafe) f.get(null);
            offset = unsafe.objectFieldOffset(Counter.class.getDeclaredField("counter"));
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    // 失败了就重试直到成功为止
    private void increment(){
        int before = counter;
        while(!unsafe.compareAndSwapInt(this, offset, before, before+1)){
            before = counter;
        }
    }

    public int getCounter() {
        return counter;
    }

    public static void main(String[] args) throws InterruptedException {

        Counter cnter = new Counter();
        ExecutorService threadPool = Executors.newFixedThreadPool(100);
        // 起100个线程，每个线程自增10000次
        IntStream.range(0, 100).forEach(i->threadPool.submit(
                ()->IntStream.range(0, 1000).forEach(j->cnter.increment())
        ));

        threadPool.shutdown();
        Thread.sleep(1000);
        System.out.println(cnter.getCounter());
    }
}
