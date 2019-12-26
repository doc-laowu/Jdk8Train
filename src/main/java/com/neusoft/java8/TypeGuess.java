package com.neusoft.java8;

/**
 * @Title: TypeGuess
 * @ProjectName MyJdbcSink
 * @Description: TODO
 * @Author yisheng.wu
 * @Date 2019/8/3010:51
 */
public class TypeGuess {

    public static class Value< T > {
        public static< T > T defaultValue() {
            return null;
        }

        public T getOrDefault( T value, T defaultValue ) {
            return ( value != null ) ? value : defaultValue;
        }
    }

    public static void main(String[] args) {

        final Value< String > value = new Value<>();
        // java 8 之前 必须 Value.<String>defaultValue()
        System.out.println(value.getOrDefault( "22", Value.defaultValue() ));
    }

}
