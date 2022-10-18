package example;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Collections;
import java.util.Properties;

public class Consumer {

    static String bootstrapServers = "cell-1.streaming.us-ashburn-1.oci.oraclecloud.com:9092";
    static String tenancyName = "oftprism";
    static String username = "shan.duraipandian@oracle.com";
    static String streamPoolId = "ocid1.streampool.oc1.iad.amaaaaaa4nuaawaa72i3titgycuhdbstjrw5se4liprqov47nik6jgxvfa3a";
    static String authToken = "kk.P3CmiOHG4D5KL2<e<"; // from step 8 of Prerequisites section
    static String streamOrKafkaTopicName = "cyc-cards-test-stream"; // from step 2 of Prerequisites section

    static String consumerGroupName = "oss-test-consumer-group";

    private static Properties getKafkaProperties(){
        Properties props = new Properties();
        props.put("bootstrap.servers", bootstrapServers);
        props.put("group.id", consumerGroupName);
        props.put("enable.auto.commit", "false");
        props.put("session.timeout.ms", "30000");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("security.protocol", "SASL_SSL");
        props.put("sasl.mechanism", "PLAIN");
        props.put("auto.offset.reset", "earliest");
        final String value = "org.apache.kafka.common.security.plain.PlainLoginModule required username=\""
                + tenancyName + "/"
                + username + "/"
                + streamPoolId + "\" "
                + "password=\""
                + authToken + "\";";
        props.put("sasl.jaas.config", value);
        return props;
    }

    public void consume(){
        final KafkaConsumer<Integer, String> consumer = new KafkaConsumer<>(getKafkaProperties());;
        consumer.subscribe(Collections.singletonList(streamOrKafkaTopicName));
        ConsumerRecords<Integer, String> records = consumer.poll(10000);

        System.out.println("size of records polled is "+ records.count());
        for (ConsumerRecord<Integer, String> record : records) {
            System.out.println("Received message: (" + record.key() + ", " + record.value() + ") at offset " + record.offset());
        }

        consumer.commitSync();
        consumer.close();
    }

    public static void main(String[] args) {
        Consumer consumer = new Consumer();
        consumer.consume();
    }
}

