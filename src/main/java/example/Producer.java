package example;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class Producer {

    static String bootstrapServers = "cell-1.streaming.us-sanjose-1.oci.oraclecloud.com:9092";
    static String tenancyName = "dshan";
    static String username = "dsraja79@yahoo.com";
    //Public Stream Pool
    //static String streamPoolId = "ocid1.streampool.oc1.iad.amaaaaaa4nuaawaa72i3titgycuhdbstjrw5se4liprqov47nik6jgxvfa3a";
    static String streamPoolId = "ocid1.streampool.oc1.us-sanjose-1.amaaaaaa2e3hxrqasmcgsqo7ntcktbl32w537hmf6ecydo4zkiw2zxffe7dq";

    static String authToken = "pT>TC5yfAx5.yKnYgJ7s"; // from step 8 of Prerequisites section
    static String streamOrKafkaTopicName = "ocilabs-stream"; // from step 2 of Prerequisites section

    private static Properties getKafkaProperties() {
        Properties properties = new Properties();
        properties.put("bootstrap.servers", bootstrapServers);
        properties.put("security.protocol", "SASL_SSL");
        properties.put("sasl.mechanism", "PLAIN");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        final String value = "org.apache.kafka.common.security.plain.PlainLoginModule required username=\""
                + tenancyName + "/"
                + username + "/"
                + streamPoolId + "\" "
                + "password=\""
                + authToken + "\";";
        properties.put("sasl.jaas.config", value);
        properties.put("retries", 3); // retries on transient errors and load balancing disconnection
        properties.put("max.request.size", 1024 * 1024); // limit request size to 1MB
        return properties;
    }

    public void publish(){
        try {
            Properties properties = getKafkaProperties();
            KafkaProducer producer = new KafkaProducer<>(properties);

            for(int i=0;i<10;i++) {
                ProducerRecord<String, String> record = new ProducerRecord<>(streamOrKafkaTopicName, "message-key" + i, "message-value" + i);
                producer.send(record, (md, ex) -> {
                    if (ex != null) {
                        System.out.println("****************Producer Error:*******************");
                        System.err.println("***************exception occurred in producer for review :" + record.value()
                                + ", exception is " + ex);
                        ex.printStackTrace();
                    } else {
                        System.err.println("Sent msg to " + md.partition() + " with offset " + md.offset() + " at " + md.timestamp());
                    }
                });
            }

            // producer.send() is async, to make sure all messages are sent we use producer.flush()
            producer.flush();
            producer.close();
        } catch (Exception e) {
            System.err.println("Error: exception " + e);
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {
        Producer producer = new Producer();
        producer.publish();
    }

}
