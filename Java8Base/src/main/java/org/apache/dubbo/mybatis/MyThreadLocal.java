package org.apache.dubbo.mybatis;

import java.util.Map;

/**
 * @Title: MyThreadLocal
 * @ProjectName Jdk8Train
 * @Description: TODO
 * @Author yisheng.wu
 * @Date 2020/6/214:40
 */
public class MyThreadLocal {

    private static final ThreadLocal<Map<String, String>> userThreadLocal = new ThreadLocal();

    public static void set(Map<String, String> query) {
        userThreadLocal.set(query);
    }

    public static void put(String key, String value) {
        userThreadLocal.get().put(key, value);
    }

    public static void unset() {
        userThreadLocal.remove();
    }

    public static Map<String, String> get() {
        return userThreadLocal.get();
    }

}
