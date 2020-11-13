package com.neusoft.cglib;

import net.sf.cglib.proxy.*;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

/**
  * @Author: yisheng.wu
  * @Description TODO 使用cglib实现代理
  * @Date 11:29 2020/10/10
  * @Param
  * @return
  **/
public class SampleClass {

    public void test(){
        System.out.println("hello world");
    }

    public String test(String input){
        return "hello world";
    }

    @Test
    public void testFixedValue(){
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(SampleClass.class);
        // FixedValue用来对所有拦截的方法返回相同的值
        enhancer.setCallback(new FixedValue() {
            @Override
            public Object loadObject() throws Exception {
                return "Hello cglib";
            }
        });
        SampleClass proxy = (SampleClass) enhancer.create();
        System.out.println(proxy.test(null));
        System.out.println(proxy.toString());
        System.out.println(proxy.getClass());
        System.out.println(proxy.hashCode());
    }

    /**
     * Enhancer既能够代理普通的class，也能够代理接口。Enhancer创建一个被代理对象的子类并且拦截所有的方法调用（包括从Object中继承的toString和hashCode方法）。
     * Enhancer不能够拦截final方法，例如Object.getClass()方法，这是由于Java final方法语义决定的。基于同样的道理，Enhancer也不能对fianl类进行代理操作。
     */
    @Test
    public void testEnhancer() {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(SampleClass.class);
        enhancer.setCallback(new MethodInterceptor() {
            @Override
            public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
                System.out.println("before method run...");
                Object result = proxy.invokeSuper(obj, args);
                System.out.println("after method run...");
                return result;
            }
        });
        SampleClass sample = (SampleClass) enhancer.create();
        sample.test();
    }

    @Test
    public void testInvocationHandler() throws Exception{
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(SampleClass.class);
        enhancer.setCallback(new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if(method.getDeclaringClass() != Object.class && method.getReturnType() == String.class){
                    return "hello cglib";
                }else{
                    throw new RuntimeException("Do not know what to do");
                }
            }
        });
        SampleClass proxy = (SampleClass) enhancer.create();
        System.out.println(("hello cglib".equals(proxy.test(null))));
        System.out.println(("Hello cglib".equals(proxy.toString())));
    }

    /**
     * 拦截指定的方法
     * @throws Exception
     */
    @Test
    public void testCallbackFilter() throws Exception{
        Enhancer enhancer = new Enhancer();
        CallbackHelper callbackHelper = new CallbackHelper(SampleClass.class, new Class[0]) {
            @Override
            protected Object getCallback(Method method) {
                if(method.getDeclaringClass() != Object.class && method.getReturnType() == String.class){
                    return new FixedValue() {
                        @Override
                        public Object loadObject() throws Exception {
                            return "Hello cglib";
                        }
                    };
                }else{
                    return NoOp.INSTANCE;
                }
            }
        };
        enhancer.setSuperclass(SampleClass.class);
        enhancer.setCallbackFilter(callbackHelper);
        enhancer.setCallbacks(callbackHelper.getCallbacks());
        SampleClass proxy = (SampleClass) enhancer.create();
        System.out.println("Hello cglib".equals(proxy.test(null)));
        System.out.println("Hello cglib".equals(proxy.toString()));
        System.out.println(proxy.hashCode());
    }

}