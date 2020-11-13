package com.neusoft.cglib;

import net.sf.cglib.core.Signature;
import net.sf.cglib.proxy.InterfaceMaker;
import net.sf.cglib.proxy.Mixin;
import net.sf.cglib.reflect.*;
import net.sf.cglib.util.ParallelSorter;
import net.sf.cglib.util.StringSwitcher;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MixinInterfaceTest {

    interface Interface1{
        String first();
    }

    interface Interface2{
        String second();
    }

    class Class1 implements Interface1{
        @Override
        public String first() {
            return "first";
        }
    }

    class Class2 implements Interface2{
        @Override
        public String second() {
            return "second";
        }
    }

    interface MixinInterface extends Interface1, Interface2{

    }

    /**
     * Mixin能够让我们将多个对象整合到一个对象中去，前提是这些对象必须是接口的实现。
     * @throws Exception
     */
    @Test
    public void testMixin() throws Exception{
        Mixin mixin = Mixin.create(new Class[]{Interface1.class, Interface2.class, MixinInterface.class},
                new Object[]{new Class1(),new Class2()});
        MixinInterface mixinDelegate = (MixinInterface) mixin;
        assertEquals("first", mixinDelegate.first());
        assertEquals("second", mixinDelegate.second());
    }

    /** 用来模拟一个String到int类型的Map类型。如果在Java7以后的版本中，类似一个switch语句。
     * @throws Exception
     */
    @Test
    public void testStringSwitcher() throws Exception{
        String[] strings = new String[]{"one", "two"};
        int[] values = new int[]{10,20};
        StringSwitcher stringSwitcher = StringSwitcher.create(strings,values,true);
        assertEquals(10, stringSwitcher.intValue("one"));
        assertEquals(20, stringSwitcher.intValue("two"));
        assertEquals(-1, stringSwitcher.intValue("three"));
    }

    /**
     * Interface Maker用来创建一个新的Interface
     * @throws Exception
     */
    @Test
    public void testInterfaceMarker() throws Exception{
        Signature signature = new Signature("foo", Type.DOUBLE_TYPE, new Type[]{Type.INT_TYPE});
        InterfaceMaker interfaceMaker = new InterfaceMaker();
        interfaceMaker.add(signature, new Type[0]);
        Class iface = interfaceMaker.create();
        assertEquals(1, iface.getMethods().length);
        assertEquals("foo", iface.getMethods()[0].getName());
        assertEquals(double.class, iface.getMethods()[0].getReturnType());
    }

    interface BeanDelegate{
        String getValueFromDelegate();
    }

    /**
     * MethodDelegate主要用来对方法进行代理
     *
     * 关于Method.create的参数说明：
     * 1. 第二个参数为即将被代理的方法
     * 2. 第一个参数必须是一个无参数构造的bean。因此MethodDelegate.create并不是你想象的那么有用
     * 3. 第三个参数为只含有一个方法的接口。当这个接口中的方法被调用的时候，将会调用第一个参数所指向bean的第二个参数方法
     *
     * 缺点：
     * 1. 为每一个代理类创建了一个新的类，这样可能会占用大量的永久代堆内存
     * 2. 你不能代理需要参数的方法
     * 3. 如果你定义的接口中的方法需要参数，那么代理将不会工作，并且也不会抛出异常；如果你的接口中方法需要其他的返回类型，那么将抛出IllegalArgumentException
     *
     * @throws Exception
     */
    @Test
    public void testMethodDelegate()  throws Exception{
        SampleBean bean = new SampleBean();
        bean.setValue("Hello cglib");
        BeanDelegate delegate = (BeanDelegate) MethodDelegate.create(bean,"getValue", BeanDelegate.class);
        assertEquals("Hello cglib", delegate.getValueFromDelegate());
    }


    public interface DelegatationProvider {
        void setValue(String value);
    }

    public class SimpleMulticastBean implements DelegatationProvider {
        private String value;
        @Override
        public void setValue(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    /**
     * 1、多重代理和方法代理差不多，都是将代理类方法的调用委托给被代理类。使用前提是需要一个接口，以及一个类实现了该接口
     * 2、通过这种interface的继承关系，我们能够将接口上方法的调用分散给各个实现类上面去。
     * 3、多重代理的缺点是接口只能含有一个方法，如果被代理的方法拥有返回值，那么调用代理类的返回值为最后一个添加的被代理类的方法返回值
     *
     * @throws Exception
     */
    @Test
    public void testMulticastDelegate() throws Exception{
        MulticastDelegate multicastDelegate = MulticastDelegate.create(DelegatationProvider.class);
        SimpleMulticastBean first = new SimpleMulticastBean();
        SimpleMulticastBean second = new SimpleMulticastBean();
        multicastDelegate = multicastDelegate.add(first);
        multicastDelegate  = multicastDelegate.add(second);

        DelegatationProvider provider = (DelegatationProvider) multicastDelegate;
        provider.setValue("Hello world");

        assertEquals("Hello world", first.getValue());
        assertEquals("Hello world", second.getValue());
    }



    interface SampleBeanConstructorDelegate{
        Object newInstance(String value);
    }

    /**
     * 为了对构造函数进行代理，我们需要一个接口，这个接口只含有一个Object newInstance(…)方法，用来调用相应的构造函数
     * @throws Exception
     */
    @Test
    public void testConstructorDelegate() throws Exception{
        SampleBeanConstructorDelegate constructorDelegate = (SampleBeanConstructorDelegate) ConstructorDelegate.create(
                SampleBean.class, SampleBeanConstructorDelegate.class);
        SampleBean bean = (SampleBean) constructorDelegate.newInstance("Hello world");
        assertTrue(SampleBean.class.isAssignableFrom(bean.getClass()));
        System.out.println(bean.getValue());
    }


    /**
     * 能够对多个数组同时进行排序，目前实现的算法有归并排序和快速排序
     * @throws Exception
     */
    @Test
    public void testParallelSorter() throws Exception{
        Integer[][] value = {
                {4, 3, 9, 0},
                {2, 1, 6, 0}
        };
        ParallelSorter.create(value).mergeSort(0);
        for(Integer[] row : value){
            int former = -1;
            for(int val : row){
                assertTrue(former < val);
                former = val;
            }
        }
    }

    /**
     * FastClass就是对Class对象进行特定的处理，比如通过数组保存method引用，因此FastClass引出了一个index下标的新概念，
     * 比如getIndex(String name, Class[] parameterTypes)就是以前的获取method的方法。
     * 通过数组存储method,constructor等class信息，从而将原先的反射调用，转化为class.index的直接调用，从而体现所谓的FastClass。
     * @throws Exception
     */
    @Test
    public void testFastClass() throws Exception{
        FastClass fastClass = FastClass.create(SampleBean.class);
        FastMethod fastMethod = fastClass.getMethod("getValue",new Class[0]);
        SampleBean bean = new SampleBean();
        bean.setValue("Hello world");
        assertEquals("Hello world",fastMethod.invoke(bean, new Object[0]));
    }

}
