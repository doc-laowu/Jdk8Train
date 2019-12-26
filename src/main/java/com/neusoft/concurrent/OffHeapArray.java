package com.neusoft.concurrent;

import scala.Int;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * @Title: OffHeapArray
 * @ProjectName MyJdbcSink
 * @Description: TODO
 * @Author yisheng.wu
 * @Date 2019/10/3111:01
 */
public class OffHeapArray {

    private static final int INT = 4;
    private long size;
    private long address;

    private static Unsafe unsafe;
    static {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            unsafe = (Unsafe) f.get(null);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    // 构造方法分配堆外内存
    public OffHeapArray(long size) {
        this.size = size;
        address = unsafe.allocateMemory(size * INT);
    }

    // 获取元素
    public int get(long index){
        return unsafe.getInt(address + index * INT);
    }

    // 设置元素
    public void set(long index, int value){
        unsafe.putInt(address + index * INT, value);
    }

    // 获取数组长度
    public long size(){
        return this.size;
    }

    // 释放内存空间
    public void freeMemory(){
        unsafe.freeMemory(address);
    }

    public static void main(String[] args) {

        OffHeapArray offHeapArray = new OffHeapArray(4);
        offHeapArray.set(0, 2);
        offHeapArray.set(1, 4);
        offHeapArray.set(2, 8);
        offHeapArray.set(3, 16);
        offHeapArray.set(0, 16);

        for (int i=0; i < 4; i++){
            System.out.println(offHeapArray.get(i));
        }

        offHeapArray.freeMemory();
    }

}
