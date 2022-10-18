package com.example.ocistreaming;

import example.Consumer;
import example.Producer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OCIStreamingController {
    @GetMapping("/messages")
    public String getMessage() {
        return "Hello from OCI!";
    }

    @GetMapping("/publish")
    public String publish() {
        Producer producer = new Producer();
        producer.publish();
        return "Message published Successfully!";
    }

    @GetMapping("/consume")
    public String consume() {
        Consumer consumer = new Consumer();
        consumer.consume();
        return "Message consumed Successfully!";
    }
}
