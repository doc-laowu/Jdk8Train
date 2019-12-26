package com.neusoft.sink;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;

import java.io.*;
import java.util.*;

import org.apache.log4j.Logger;

/**
 * @Copyright(C) 北京微吼时代科技有限公司
 * @WebSite http://www.vhall.com/
 * @ClassName SimpleKafkaConsumer
 * @Description 消费者
 * @Version V1.0.0
 * @Date 2019/3/28 14:30
 * @Author 黄庭华
 */
public class SimpleKafkaConsumer {


    private final static Logger logger = Logger.getLogger(SimpleKafkaConsumer.class);

    private final static Set<String> allSet = new HashSet<String>(Arrays.asList("270200,262001,163001,163002,172003,172004".split(",")));


    private static KafkaConsumer<String, String> consumer;
    // python 线上
    // play_statistical_analysis_metadata
    // services_data_topic


    // test_chao
    // test_collect
    // test_collect_sink
    //


    private final static String TOPIC = "data_sync_consumer_business_info";
    // test_source_new_client_ods test_source_new_service_ods
    // original_log_client_ods,original_log_service_ods
    // zhike_activityid_topic
    // data_collect_client_ods

    public SimpleKafkaConsumer() {
        Properties props = new Properties();
        // 192.168.16.19:9092,192.168.16.20:9092,192.168.16.21:9092
        // 192.168.21.37:9092,192.168.21.38:9092,192.168.121.39:9092
        // 192.168.16.7:9092,192.168.16.8:9092,192.168.16.9:9092
        props.put("bootstrap.servers", "192.168.16.7:9092,192.168.16.8:9092,192.168.16.9:9092");
        //每个消费者分配独立的组号
        props.put("group.id", "m4525uh4ki2l22g55123214");
        //如果value合法，则自动提交偏移量
        props.put("enable.auto.commit", "false");
        //设置多久一次更新被消费消息的偏移量
        props.put("auto.commit.interval.ms", "1000");
        //设置会话响应的时间，超过这个时间kafka可以选择放弃消费或者消费下一条消息
        props.put("session.timeout.ms", "30000");
        //自动重置offset
        props.put("auto.offset.reset", "earliest");    // latest earliest
        props.put("key.deserializer",
                "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer",
                "org.apache.kafka.common.serialization.StringDeserializer");
        consumer = new KafkaConsumer<String, String>(props);


/*        Properties props1 = new Properties();
        props.put("bootstrap.servers", "192.168.16.19:9092,192.168.16.20:9092,192.168.16.21:9092");
        props.put("acks", "1");
        props.put("retries", 1);
        props.put("batch.size", 36384);
        props.put("linger.ms", 1000);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        //设置分区类,根据key进行数据分区
        producer = new KafkaProducer<String, String>(props1);*/

//        TopicPartition p = new TopicPartition("tes752786625", 2);


/*
        TopicPartition p1 = new TopicPartition("test_data_collect_for_pdt", 0);
        TopicPartition p2 = new TopicPartition("test_data_collect_for_pdt", 1);
        TopicPartition p3 = new TopicPartition("test_data_collect_for_pdt", 2);
        consumer.assign(Arrays.asList(p1,p2,p3));
        consumer.seek(p1, 10212);
        consumer.seek(p2, 10212);
        consumer.seek(p3, 10212);*/


//        TopicPartition p2 = new TopicPartition("tes752786625", 1);
//        指定消费topic的那个分区

//        指定从topic的分区的某个offset开始消费
//        consumer.seekToBeginning(Arrays.asList(p));
//        consumer.seek(p, 80000);

//        consumer.seek(p2, 80000);
    }

    public void consume() {

        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/vhall/data_sync_consumer_business_info.txt")));

            long counter = 0;
            consumer.subscribe(Arrays.asList(TOPIC));
            while (true) {

//                if(counter == 162068)
//                    break;

                ConsumerRecords<String, String> records = consumer.poll(1000);
                for (ConsumerRecord<String, String> record : records) {
                    String offset = record.offset() + "";
                    String topic = record.topic() + "";
                    String partition = record.partition() + "";
                    String value = record.value();

                    //将消息细写入文件
                    out.write(value);
                    out.newLine();
//                    System.out.println(value);
                    System.out.println("counter is -----------> " + (++counter));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.flush();
                out.close();
                consumer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private static KafkaProducer<String, String> producer;

    public static void main(String[] args) {



        new SimpleKafkaConsumer().consume();


    }


}
