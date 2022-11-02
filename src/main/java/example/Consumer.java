package example;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;


import java.util.Collections;
import java.util.Properties;

public class Consumer {

    static String bootstrapServers = "5mqywrhsxdwq.streaming.us-ashburn-1.oci.oraclecloud.com:9092";
    static String tenancyName = "oftprism";
    static String username = "shan.duraipandian@oracle.com";

    //Public Stream Pool
    //static String streamPoolId = "ocid1.streampool.oc1.iad.amaaaaaa4nuaawaa72i3titgycuhdbstjrw5se4liprqov47nik6jgxvfa3a";
    static String streamPoolId = "ocid1.streampool.oc1.iad.amaaaaaa4nuaawaaqz7n5rvjjzmvjatpc4yalg6ammfp7jd75mqywrhsxdwq";
    static String authToken = "kk.P3CmiOHG4D5KL2<e<"; // from step 8 of Prerequisites section
    static String streamOrKafkaTopicName = "cards-dev-pci-stream"; // from step 2 of Prerequisites section

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

    public int consume(){
        final KafkaConsumer<Integer, String> consumer = new KafkaConsumer<>(getKafkaProperties());
        consumer.subscribe(Collections.singletonList(streamOrKafkaTopicName));
        ConsumerRecords<Integer, String> records = consumer.poll(10000);

        System.out.println("size of records polled is "+ records.count());
        for (ConsumerRecord<Integer, String> record : records) {
            System.out.println("Received message: (" + record.key() + ", " + record.value() + ") at offset " + record.offset());
        }

        consumer.commitSync();
        consumer.close();

        return records.count();
    }

    public static void main(String[] args) {
        Consumer consumer = new Consumer();
        consumer.consume();
    }
}

