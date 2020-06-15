package com.neusoft.sink;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Copyright(C) 北京微吼时代科技有限公司
 * @WebSite http://www.vhall.com/
 * @ClassName SimpleKafkaProducer
 * @Description kafka生产者
 * @Version V1.0.0
 * @Date 2019/3/28 10:13
 * @Author 黄庭华
 */
public class SimpleKafkaProducer {



    private static KafkaProducer<String, String> producer;

    private final static String TOPIC = "data_sync_consumer_business_info";

    private final static String[] codeArr = {"0", "1", "2", "3"};

    private final static String[] sessionArr = {"1555583157172826", "1555583157172829", "15555831571713826", "1665583157172826",
            "1125583157172826", "2125583767172826", "212558376717282544", "236457324234", "485653423234",
            "1231534634234", "457567345235", "25346452434534", "123534654534645", "235434623341234", "25457568424234"};

    private final static String[] uidArr = {"caixukun123345", "jinitaimei1225", "awsl123235346", "nmsl3547567213", "qaqaasdfg232354", "jkjfgh3455"};

    private final static String[] buArr = {"0", "1", "2"};


    public SimpleKafkaProducer() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "192.168.16.7:9092,192.168.16.8:9092,192.168.16.9:9092");
        props.put("acks", "0");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        //设置分区类,根据key进行数据分区
        producer = new KafkaProducer<String, String>(props);
    }

    public void produce() {

        Long currentTime = MyTimeUtil.CurrentTimeLong();

        ExecutorService exec = Executors.newCachedThreadPool();

        for(int i=0; i<5; i++){

            exec.execute(new Runnable() {
                public void run() {



                    for(int i=0;i <10000; i++){

                        String key = String.valueOf(UUID.randomUUID());
                        String data = SendMsg2Bu_0_1_2();
                        producer.send(new ProducerRecord<String, String>(TOPIC, key, data));
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        System.out.println("the data num is " + i + "the data is :" + data);
                    }
                    producer.close();
                }
            });

        }
    }

    public static String SendMsg2Bu_0_1_2(){

        Map<String, String> map = new HashMap<String, String>();

        String s = sessionArr[new Random().nextInt(sessionArr.length-1) + 1];
        String uid = uidArr[new Random().nextInt(uidArr.length-1) + 1];
        String p= "caixukun";
        String aid = "caixuhun9572";
        String vid = "1008611";
        String app_id = "9527";
        String vfid = "16420839";
        String timestamp_1m = MyTimeUtil.Time1Min();
        String process_time = MyTimeUtil.CurrentTimeStr();
        String code = codeArr[new Random().nextInt(codeArr.length-1) + 1];
        String picture_quality = "same";
        String protocol= "rtmp";
        String sd = "t-vrt.e.vhall.com:8080";
        String pf = "5";
        String src_ip = "1.119.193.36";
        String isp = "铁通";
        String browser = "QQBroswer";
        String viewer_country = "中国";
        String viewer_province = "成都";
        String viewer_city = "成都";
        String tt = (new Random().nextInt(3000-2900) + 1) + "";
        String bitrate = (new Random().nextInt(1000-100) + 1) + "";
        String bc = "0";
        String bt = "0";
        String report_time = MyTimeUtil.CurrentTimeLong() + "";
        String fd = "";
        String flow_type = "1";
        String biz_role = "0";
        String biz_id = "0";
        String biz_des01 = "1";
        String biz_des02 = "2";
        String bu = buArr[new Random().nextInt(buArr.length-1) + 1];

        map.put("s", s);
        map.put("uid", uid);
        map.put("p", p);
        map.put("aid", aid);
        map.put("vid", vid);
        map.put("app_id", app_id);
        map.put("vfid", vfid);
        map.put("timestamp_1m", timestamp_1m);
        map.put("process_time", process_time);
        map.put("code", code);
        map.put("picture_quality", picture_quality);
        map.put("protocol", protocol);
        map.put("sd", sd);
        map.put("pf", pf);
        map.put("src_ip", src_ip);
        map.put("isp", isp);
        map.put("browser", browser);
        map.put("viewer_country", viewer_country);
        map.put("viewer_province", viewer_province);
        map.put("viewer_city", viewer_city);
        map.put("tt", tt);
        map.put("bitrate", bitrate);
        map.put("bc", bc);
        map.put("bt", bt);
        map.put("report_time", report_time);
        map.put("fd", fd);
        map.put("flow_type", flow_type);
        map.put("biz_role", biz_role);
        map.put("biz_id", biz_id);
        map.put("biz_des01", biz_des01);
        map.put("biz_des02", biz_des02);
        map.put("bu", bu);

        JSONObject itemJSONObj = JSONObject.parseObject(JSON.toJSONString(map));
        String str = JSON.toJSONString(itemJSONObj);

        System.out.println("json ===========================> " + str);

        return str;
    }

    public static void main(String[] args) {

        new SimpleKafkaProducer().produce();

       /* SimpleKafkaProducer simpleKafkaProducer = new SimpleKafkaProducer();

        List<PartitionInfo> target = producer.partitionsFor("test_sink_new_service_ods_latest");

        System.out.println(target.size());*/
    }


}
