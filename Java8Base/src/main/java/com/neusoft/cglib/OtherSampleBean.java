package com.neusoft.cglib;

import net.sf.cglib.beans.BeanCopier;
import net.sf.cglib.beans.BeanGenerator;
import net.sf.cglib.beans.BeanMap;
import net.sf.cglib.beans.BulkBean;
import net.sf.cglib.core.KeyFactory;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OtherSampleBean {
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    /**
     * cglib提供的能够从一个bean复制到另一个bean中，而且其还提供了一个转换器，用来在转换的时候对bean的属性进行操作。
     * @throws Exception
     */
    @Test
    public void testBeanCopier() throws Exception{
        BeanCopier copier = BeanCopier.create(SampleBean.class, OtherSampleBean.class, false);//设置为true，则使用converter
        SampleBean myBean = new SampleBean();
        myBean.setValue("Hello cglib");
        OtherSampleBean otherBean = new OtherSampleBean();
        copier.copy(myBean, otherBean, null); //设置为true，则传入converter指明怎么进行转换
        assertEquals("Hello cglib", otherBean.getValue());
    }


    /**
     * 相比于BeanCopier，BulkBean将copy的动作拆分为getPropertyValues和setPropertyValues两个方法，允许自定义处理属性
     * @throws Exception
     */
    @Test
    public void testBulkBean() throws Exception{
        BulkBean bulkBean = BulkBean.create(SampleBean.class,
                new String[]{"getValue"},
                new String[]{"setValue"},
                new Class[]{String.class});
        SampleBean bean = new SampleBean();
        bean.setValue("Hello world");
        Object[] propertyValues = bulkBean.getPropertyValues(bean);
        assertEquals(1, bulkBean.getPropertyValues(bean).length);
        assertEquals("Hello world", bulkBean.getPropertyValues(bean)[0]);
        bulkBean.setPropertyValues(bean,new Object[]{"Hello cglib"});
        assertEquals("Hello cglib", bean.getValue());
    }

    /**
     * BeanMap类实现了Java Map，将一个bean对象中的所有属性转换为一个String-to-Obejct的Java Map
     * @throws Exception
     */
    @Test
    public void testBeanMap() throws Exception{
        BeanGenerator generator = new BeanGenerator();
        generator.addProperty("username",String.class);
        generator.addProperty("password",String.class);
        Object bean = generator.create();
        Method setUserName = bean.getClass().getMethod("setUsername", String.class);
        Method setPassword = bean.getClass().getMethod("setPassword", String.class);
        setUserName.invoke(bean, "admin");
        setPassword.invoke(bean,"password");
        BeanMap map = BeanMap.create(bean);
        assertEquals("admin", map.get("username"));
        assertEquals("password", map.get("password"));
    }

    /** keyFactory类用来动态生成接口的实例，接口需要只包含一个newInstance方法，返回一个Object。
     * keyFactory为构造出来的实例动态生成了Object.equals和Object.hashCode方法，能够确保相同的参数构造出的实例为单例的。
     */
    @Test
    public void testKeyFactory() throws Exception{
        SampleKeyFactory keyFactory = (SampleKeyFactory) KeyFactory.create(SampleKeyFactory.class);
        Object key = keyFactory.newInstance("foo", 42);
        Object key1 = keyFactory.newInstance("foo", 42);
        assertEquals(key,key1);//测试参数相同，结果是否相等
    }

}
