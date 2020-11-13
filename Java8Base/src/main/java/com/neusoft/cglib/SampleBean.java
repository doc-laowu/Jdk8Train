package com.neusoft.cglib;

import net.sf.cglib.beans.BeanGenerator;
import net.sf.cglib.beans.ImmutableBean;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SampleBean {
    private String value;

    public SampleBean() {
    }

//    public SampleBean(String value) {
//        this.value = value;
//    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    /**
     * ImmutableBean允许创建一个原来对象的包装类，这个包装类是不可变的，任何改变底层对象的包装类操作都会抛出IllegalStateException。
     * 但是我们可以通过直接操作底层对象来改变包装类对象。这有点类似于Guava中的不可变视图
     * @throws Exception
     */
    @Test
    public void testImmutableBean() throws Exception{
        SampleBean bean = new SampleBean();
        bean.setValue("Hello world");
        SampleBean immutableBean = (SampleBean) ImmutableBean.create(bean); //创建不可变类
        assertEquals("Hello world",immutableBean.getValue());
        bean.setValue("Hello world, again"); //可以通过底层对象来进行修改
        assertEquals("Hello world, again", immutableBean.getValue());
        immutableBean.setValue("Hello cglib"); //直接修改将throw exception
    }

    /**
     * cglib提供的一个操作bean的工具，使用它能够在运行时动态的创建一个bean。
     * @throws Exception
     */
    @Test
    public void testBeanGenerator() throws Exception{
        BeanGenerator beanGenerator = new BeanGenerator();
        beanGenerator.addProperty("value",String.class);
        Object myBean = beanGenerator.create();
        Method setter = myBean.getClass().getMethod("setValue",String.class);
        setter.invoke(myBean,"Hello cglib");

        Method getter = myBean.getClass().getMethod("getValue");
        assertEquals("Hello cglib",getter.invoke(myBean));
    }

}
