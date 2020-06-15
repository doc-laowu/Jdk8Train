package com.neusoft.sink;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * @Title: JSONObject2Bean
 * @ProjectName MyJdbcSink
 * @Description: TODO
 * @Author yisheng.wu
 * @Date 2019/4/1916:30
 */
public class JSONObject2Bean {

    public <T> T JSONObj2Bean(JSONObject obj, Class<?> clazz){

        String SETTER_PREFIX = "set";
        String GETTER_PREFIX = "get";

        Object object = null;

        try {
            Set<String> jsonFields = obj.keySet();
            Field[] beanFields = clazz.getDeclaredFields();

            object = clazz.newInstance();

            String fieldName = null;

            for(Field O : beanFields){

                fieldName = O.getName();

                System.out.println("set" + setMethodName(fieldName));

                Method m = null;

                switch (O.getGenericType().toString()){
                    case "class java.lang.String":

                        m = (Method) object.getClass().getMethod("set" + setMethodName(fieldName), String.class);

                        if (jsonFields.contains(fieldName)) {
                            m.invoke(object, obj.getString(fieldName));
                        } else {
                            m.invoke(object, "");
                        }
                        break;
                    case "class java.lang.Integer":

                        m = (Method) object.getClass().getMethod("set" + setMethodName(fieldName), Integer.class);

                        if (jsonFields.contains(fieldName)) {
                            m.invoke(object, obj.getInteger(fieldName));
                        } else {
                            m.invoke(object, 0);
                        }
                        break;
                    case "class java.lang.Double":

                        m = (Method) object.getClass().getMethod("set" + setMethodName(fieldName), Double.class);

                        if (jsonFields.contains(fieldName)) {
                            m.invoke(object, obj.getDouble(fieldName));
                        } else {
                            m.invoke(object, 0.0D);
                        }
                        break;
                    case "class java.lang.Long":

                        m = (Method) object.getClass().getMethod("set" + setMethodName(fieldName), Long.class);

                        if (jsonFields.contains(fieldName)) {
                            m.invoke(object, obj.getLong(fieldName));
                        } else {
                            m.invoke(object, 0L);
                        }
                        break;
                    default:
                        throw new RuntimeException("the out of bound object type!");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return (T)object;
    }


    // 把一个字符串的第一个字母大写、效率是最高的、
    private String setMethodName(String fildeName) {
        byte[] items = fildeName.getBytes();
        items[0] = (byte) ((char) items[0] - 'a' + 'A');
        return new String(items);
    }

    public static void main(String[] args) throws IllegalAccessException, InstantiationException {

//        String json = "{\"id\":10086, \"age\":9527, \"name\":\"吴易升\", \"city\":\"北京\"}";

        String json = "{\"id\":10086, \"name\":\"吴易升\", \"city\":\"北京\"}";

        JSONObject obj = JSON.parseObject(json);

        Student stu = (Student)(new JSONObject2Bean().JSONObj2Bean(obj, Student.class));

        System.out.println(stu);

    }

}

class Student{

    private Integer id;

    private Long age;

    private String name;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Long getAge() {
        return age;
    }

    public void setAge(Long age) {
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Student{" +
                "id=" + id +
                ", age=" + age +
                ", name='" + name + '\'' +
                '}';
    }
}