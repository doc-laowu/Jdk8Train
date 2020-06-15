package com.neusoft.sink;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.errors.AuthorizationException;
import org.apache.kafka.common.errors.OutOfOrderSequenceException;
import org.apache.kafka.common.errors.ProducerFencedException;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Title: MultiConsumer
 * @ProjectName Jdk8Train
 * @Description: TODO
 * @Author yisheng.wu
 * @Date 2019/12/1614:39
 */
public class MultiConsumer {

    /**
      * @Author: yisheng.wu
      * @Description TODO 多线程消费者
      * @Date 14:44 2019/12/16
      * @Param
      * @return
      **/
    public static class KafkaConsumerRunner implements Runnable{

        private final AtomicBoolean closed = new AtomicBoolean(false);
        private final KafkaConsumer consumer;

        public KafkaConsumerRunner(KafkaConsumer consumer) {
            this.consumer = consumer;
        }

        @Override
        public void run() {
            try {
                consumer.subscribe(Arrays.asList("topic"));
                while (!closed.get()) {
                    ConsumerRecords records = consumer.poll(Duration.ofMillis(10000).toMillis());
                    // Handle new records
                }
            } catch (WakeupException e) {
                // Ignore exception if closing
                if (!closed.get()) throw e;
            } finally {
                consumer.close();
            }
        }

        // Shutdown hook which can be called from a separate thread
        public void shutdown() {
            closed.set(true);
            consumer.wakeup();
        }
    }

    /**
      * @Author: yisheng.wu
      * @Description TODO 生产者幂等和事务
      * @Date 15:01 2019/12/16
      * @Param
      * @return
      **/
    public static class KafkaProducerRunner{

        public void produce(){

            Properties props = new Properties();
            props.put("bootstrap.servers", "localhost:9092");
            props.put("transactional.id", "my-transactional-id");
            Producer<String, String> producer = new KafkaProducer<>(props, new StringSerializer(), new StringSerializer());

            producer.initTransactions();

            try {
                producer.beginTransaction();
                for (int i = 0; i < 100; i++)
                    producer.send(new ProducerRecord<>("my-topic", Integer.toString(i), Integer.toString(i)));
                producer.commitTransaction();
            } catch (ProducerFencedException | OutOfOrderSequenceException | AuthorizationException e) {
                // We can't recover from these exceptions, so our only option is to close the producer and exit.
                producer.close();
            } catch (KafkaException e) {
                // For all other exceptions, just abort the transaction and try again.
                producer.abortTransaction();
            }
            producer.close();

        }

    }

}
