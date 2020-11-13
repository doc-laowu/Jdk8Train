package com.neusoft.rmi;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
/**
 * 客户端程序
 * @author wbh
 *
 */
public class Client {
    public static void main(String[] args) {
        try {
            Hello hello = (Hello) Naming.lookup("//127.0.0.1:8080/wbh");//获取远程对象
            User user = new User();
            user.setName("james");
            System.out.println(hello.sayHello(user));
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NotBoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
