package com.neusoft.cglib;

import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 自定义实现的aop切面
 */
public class JdkAopCustomer {

    public interface target {
        double div(int a, int b);
    }

    public static class MyDiv implements target{
        @Override
        public double div(int a, int b) {
            return a/b;
        }
    }

    // 这个就是需要传入的参数对象的抽象接口，同时传入了目标方法
    public interface Advice {
        void beforeMethod(Method method);
        void afterMethod(Method method);
    }

    // 参数接口的实例化对象
    public class MyAdvice implements Advice {

        long beginTime = 0;
        long endTime = 0;

        @Override
        public void beforeMethod(Method method) {
            System.out.println("Advice中的方法开始执行了。。。");
            beginTime = System.currentTimeMillis();
        }

        @Override
        public void afterMethod(Method method) {
            endTime = System.currentTimeMillis();
            System.out.println(method.getName() + "running time:" + (endTime - beginTime));
            System.out.println("Advice中的方法结束执行了。。。");
        }

    }


    // 改造之后的方法
    private static Object getProxy(final Object target, final Advice advice) {
        Object proxy3 = Proxy.newProxyInstance(target.getClass().getClassLoader(),
                target.getClass().getInterfaces(), new InvocationHandler() {

                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

                        // 这里就是Collection类或者是子类中的方法执行 也就是调用了add()方法 同时我还做了一些其他事情。计算程序运行时间
                        advice.beforeMethod(method);
                        Object retVal = method.invoke(target, args);
                        advice.afterMethod(method);

                        return retVal;

                    }
                });
        return proxy3;
    }

    // 实际调用，验证正确性
    @Test
    public void test(){
//        final List target = new ArrayList();
//        // 直接一步到位 其实本质还是一样的，只不过写法不一样
//        Collection proxy3 = (Collection) getProxy(target, new MyAdvice());
//        proxy3.add("aaa");
//        proxy3.add("bbb");
//        proxy3.add("ccc");
//
//        System.out.println(proxy3.size());

        target proxy3 = (target) getProxy(new MyDiv(), new MyAdvice());
        double div = proxy3.div(5, 2);
        System.out.println(div);


    }

}
