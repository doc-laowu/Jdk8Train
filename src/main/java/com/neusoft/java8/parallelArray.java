package com.neusoft.java8;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @Title: parallelArray
 * @ProjectName MyJdbcSink
 * @Description: TODO 并行数组
 * @Author yisheng.wu
 * @Date 2019/8/3017:59
 */
public class parallelArray {

    public static void main(String[] args) {

        long[] arrayOfLong = new long [ 20000 ];

        Arrays.parallelSetAll( arrayOfLong,
                index -> ThreadLocalRandom.current().nextInt( 1000000 ) );
        Arrays.stream( arrayOfLong ).limit( 10 ).forEach(
                i -> System.out.print( i + " " ) );
        System.out.println();

        Arrays.parallelSort( arrayOfLong );
        Arrays.stream( arrayOfLong ).limit( 10 ).forEach(
                i -> System.out.print( i + " " ) );
        System.out.println();

    }

}
