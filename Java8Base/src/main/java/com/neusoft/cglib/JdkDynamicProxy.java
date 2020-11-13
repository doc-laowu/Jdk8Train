package com.neusoft.cglib;


import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Jdk的动态代理
 */
public class JdkDynamicProxy {

    interface IHello{
        void sayHello();
    }

    static class Hello implements IHello{
        public void sayHello() {
            System.out.println("hello world");
        }
    }

    /**
     * 1. 通过实现 InvocationHandler 接口创建自己的调用处理器；
     * 2. 通过为 Proxy 类指定 ClassLoader 对象和一组 interface 来创建动态代理类；
     * 3. 通过反射机制获得动态代理类的构造函数，其唯一参数类型是调用处理器接口类型；
     * 4. 通过构造函数创建动态代理类实例，构造时调用处理器对象作为参数被传入。
     */
    static class DynamicProxyTest implements InvocationHandler {

        private Object originalObj;

        public Object bind(Object originalObj){
            this.originalObj = originalObj;
            return Proxy.newProxyInstance(originalObj.getClass().getClassLoader(),
                    originalObj.getClass().getInterfaces(),this);
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            System.out.println("Welcome");
            return method.invoke(originalObj, args);
        }

    }

    @Test
    public void test(){
        //设置这个值，在程序运行完成后，可以生成代理类
        System.getProperties().put("sun.misc.ProxyGenerator.saveGeneratedFiles","true");
        IHello hello = (IHello) new DynamicProxyTest().bind(new Hello());
        hello.sayHello();
    }

}
