package com.example.websocketredis.service;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DeleteTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.concurrent.ExecutionException;

@Service
public class KafkaListenerService {

//    @Autowired
//    private KafkaAdmin kafkaAdmin;

    @Autowired
    private AdminClient adminClient;

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaListenerService.class);

    @KafkaListener(groupId = "groupId1", topics = "${kafka.topic}")
    public void listen(String message) throws ExecutionException, InterruptedException {
        LOGGER.error("Received message1: {}", message);
    }

    @KafkaListener(groupId = "groupId2", topics = "${kafka.topic}")
    public void listen2(String message) {
        LOGGER.error("Received message2: {}", message);
    }

    public void createTopic(String topicName, int partitions, int replicationFactor) throws ExecutionException, InterruptedException {
//        NewTopic newTopic = new NewTopic(topicName, partitions, (short) replicationFactor);
//        Map<String, String> config = new HashMap<>();
//        config.put(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, "2");
//        config.put(TopicConfig.RETENTION_MS_CONFIG, "604800000"); // 7 days
//        newTopic.configs(config);
//        kafkaAdmin.createTopics(Collections.singleton(newTopic));

        NewTopic newTopic = new NewTopic(topicName, partitions, (short) replicationFactor);
        adminClient.createTopics(Collections.singleton(newTopic)).all().get();
    }

    public void deleteTopic(String topicName) throws ExecutionException, InterruptedException {
//        Properties properties = new Properties();
//        properties.setProperty(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
//        AdminClient adminClient = AdminClient.create(properties);
        DeleteTopicsResult result = adminClient.deleteTopics(Collections.singleton(topicName));
        result.values().get(topicName).get();
    }
}