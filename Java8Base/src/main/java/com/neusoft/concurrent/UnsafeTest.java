package com.neusoft.concurrent;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * @Title: UnsafeTest
 * @ProjectName MyJdbcSink
 * @Description: TODO
 * @Author yisheng.wu
 * @Date 2019/10/3110:53
 */
public class UnsafeTest {

    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException {

        Field f = Unsafe.class.getDeclaredField("theUnsafe");
        f.setAccessible(true);
        Unsafe unsafe = (Unsafe)f.get(null);

        User usr = new User();
        System.out.println(usr.getAge());

        Field age = usr.getClass().getDeclaredField("age");
        unsafe.putInt(usr, unsafe.objectFieldOffset(age), 20);

        System.out.println(usr.getAge());
    }

}

class User{

    private int age;

    public User() {
        this.age = 10;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}