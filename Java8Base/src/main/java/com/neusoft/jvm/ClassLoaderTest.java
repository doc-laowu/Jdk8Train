package com.neusoft.jvm;

import java.io.IOException;
import java.io.InputStream;

/**
 * @Title: ClassLoaderTest
 * @ProjectName Jdk8Train
 * @Description: TODO
 * @Author yisheng.wu
 * @Date 2020/9/229:34
 */
public class ClassLoaderTest {

    public static void main(String[] args) throws Exception {

        Class.class.getClassLoader();

        ClassLoader myLoader = new ClassLoader() {
            @Override
            public Class<?> loadClass(String name) throws ClassNotFoundException {
                try {
                    String fileName = name.substring(name.lastIndexOf(".") + 1)+".class";
                    InputStream is = getClass().getResourceAsStream(fileName);
                    if (is == null) {
                        return super.loadClass(name);
                    }
                    byte[] b = new byte[is.available()];
                    is.read(b);
                    return defineClass(name, b, 0, b.length);
                } catch (IOException e) {
                    throw new ClassNotFoundException(name);
                }
            }
        };
        Object obj = myLoader.loadClass("com.neusoft.jvm.ClassLoaderTest").newInstance();
        System.out.println(obj.getClass());
        System.out.println(obj instanceof ClassLoaderTest);
    }

}
