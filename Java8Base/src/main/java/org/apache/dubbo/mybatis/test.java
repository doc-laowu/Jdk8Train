package org.apache.dubbo.mybatis;

import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.*;

/**
 * @Title: test
 * @ProjectName Jdk8Train
 * @Description: TODO
 * @Author yisheng.wu
 * @Date 2020/5/2812:42
 */
public class test {

    public static void main(String[] args) throws Exception {
        //读取配置文件
        InputStream is = Resources.getResourceAsStream("mapper/mybatis-config.xml");
        //初始化mybatis，创建SqlSessionFactory类实例
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(is);
        //创建Session实例
        SqlSession session = sqlSessionFactory.openSession();
        empMapper mapper = session.getMapper(empMapper.class);

        HashMap<String, String> query = new HashMap<>();
        MyThreadLocal.set(query);

        emp emp = new emp();
        emp.setFirstname("lebron");
        emp.setLastname("floyed");
        emp.setPosition("祖安派出所所长");

        emp emp2 = new emp();
        emp2.setFirstname("yamaha");
        emp2.setLastname("nanja");
        emp2.setPosition("祖安歌神");

        emp emp3 = new emp();
        emp3.setFirstname("蔡许昆");
        emp3.setLastname("wdnmd");
        emp3.setPosition("阿斯达卡是的");

        //插入数据
//        mapper.inertEmp(emp);

        List<org.apache.dubbo.mybatis.emp> emps = Arrays.asList(emp, emp2, emp3);

        mapper.insertBatch(emps);

//        mapper.updateEmp(emp);

//        emp ret = mapper.getEmpById(emp);

        System.out.println(emp);

        //提交事务
        session.commit();
        //关闭Session
        session.close();

        Map<String, String> stringStringMap = MyThreadLocal.get();
        Set<String> strings = stringStringMap.keySet();
        for (String string : strings) {
            System.out.println(string + ":" + stringStringMap.get(string));
        }

        MyThreadLocal.unset();
    }

    @Test
    public void test(){

        JSONObject obj = new JSONObject();
        String key = obj.getString("key");
        if(key == null){
            System.out.println(key);
        }
    }

}
