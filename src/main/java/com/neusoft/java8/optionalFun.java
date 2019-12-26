package com.neusoft.java8;

import java.util.Optional;

/**
 * @Title: optionalFun
 * @ProjectName MyJdbcSink
 * @Description: TODO
 * @Author yisheng.wu
 * @Date 2019/8/3011:02
 */
public class optionalFun {

    public static void main(String[] args) {

        Optional< String > fullName = Optional.ofNullable( null );
        System.out.println( "Full Name is set? " + fullName.isPresent() );
        System.out.println( "Full Name: " + fullName.orElseGet( () -> "[none]" ) );
        System.out.println( fullName.map( s -> "Hey " + s + "!" ).orElse( "Hey Stranger!" ) );

        Optional< String > firstName = Optional.of( "Tom" );
        System.out.println( "First Name is set? " + firstName.isPresent() );
        System.out.println( "First Name: " + firstName.orElseGet( () -> "[none]" ) );
        System.out.println( firstName.map( s -> "Hey " + s + "!" ).orElse( "Hey Stranger!" ) );
        System.out.println();

    }

}
