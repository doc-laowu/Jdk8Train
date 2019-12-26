import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

public class WriteKafkaMsg {
    private static final String TOPIC = "test_sink_new_client_ods_latest";
    private static final String BOOTSTRAP_SERVERS = "192.168.16.19:9092, 192.168.16.20:9092, 192.168.16.21:9092";

    public void writeMsg(String json){
        try{
            Properties props = new Properties();
            props.put("bootstrap.servers", BOOTSTRAP_SERVERS);
            props.put("acks", "all");
            props.put("retries", 0);
            props.put("batch.size", 16384);
            props.put("linger.ms", 1);
            props.put("buffer.memory", 33554432);
            props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
            props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

            Producer<String, String> producer = new KafkaProducer<String,String>(props);
            producer.send(new ProducerRecord<String, String>(TOPIC, json));
            producer.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
