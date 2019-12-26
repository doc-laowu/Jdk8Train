package com.neusoft.sink;

import org.apache.flume.*;
import org.apache.flume.conf.Configurable;
import org.apache.flume.sink.AbstractSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MyJdbcSink extends AbstractSink implements Configurable {

    //配置日志打印
    private static final Logger logger = LoggerFactory.getLogger(MyJdbcSink.class);

    //定义我们需要的配置信息
    private String JdbcUrl;
    private String JdbcSql;

    //设置sink配置文件的前缀
    private static final String JDBC_PREFIX = "myJdbc.";

    //数据库连接
    private Connection conn = null;

    public void configure(Context context) {
        this.JdbcUrl = context.getString(JDBC_PREFIX + "JdbcUrl");
        this.JdbcSql = context.getString(JDBC_PREFIX + "JdbcSql");
    }

    @Override
    public synchronized void start() {
        //初始化Jdbc连接
        try {
            Class.forName("com.mysql.jdbc.Driver"); // 加载驱动
            conn = DriverManager.getConnection(JdbcUrl); // 获取数据库连接
            conn.setAutoCommit(false);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        super.start();
    }

    public Status process() throws EventDeliveryException {
        Status status = null;
        Event event = null;
        PreparedStatement ps = null;

        // Start transaction
        Channel ch = getChannel();
        Transaction txn = ch.getTransaction();
        txn.begin();
        try {
            // This try clause includes whatever Channel operations you want to do
            event = ch.take();
            logger.info("get event succeful");
            //获取到event的内容
            String body = new String(event.getBody(), "utf-8");
            logger.info("get event body succeful");
            logger.info("message："+body);

            // Send the Event to the external repository.
            // storeSomeData(e);
            String[] strarr = body.split(" ");
            ps = conn.prepareStatement(JdbcSql);
            ps.setString(1, strarr[0]);
            ps.setString(2, strarr[1]);
            ps.setInt(3, Integer.parseInt(strarr[2]));
            ps.execute();
            conn.commit();
            logger.info("insert msg into db succeful");
            txn.commit();
            status = Status.READY;

        } catch (Throwable t) {
            txn.rollback();
            try {
                conn.rollback();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            // Log exception, handle individual exceptions as needed

            status = Status.BACKOFF;

            // re-throw all Errors
            if (t instanceof Error) {
                throw (Error)t;
            }
        }finally {
            txn.close();
        }
        return status;
    }

    @Override
    public synchronized void stop() {
        //关闭数据库连接
        try {
            if (null != conn) {
                conn.close();
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
        super.stop();
    }

}
