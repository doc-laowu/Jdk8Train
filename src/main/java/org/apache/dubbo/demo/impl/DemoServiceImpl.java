package org.apache.dubbo.demo.impl;

import org.apache.dubbo.demo.DemoService;

/**
 * @Title: DemoServiceImpl
 * @ProjectName Jdk8Train
 * @Description: TODO
 * @Author yisheng.wu
 * @Date 2019/12/1615:16
 */
public class DemoServiceImpl implements DemoService {
    public String sayHello(String name) {
        return "Hello " + name;
    }
}
