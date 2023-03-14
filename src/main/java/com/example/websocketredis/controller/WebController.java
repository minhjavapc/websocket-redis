package com.example.websocketredis.controller;

import com.example.websocketredis.dto.ChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Random;

@Controller
@RequestMapping("/")
public class WebController {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private KafkaTemplate kafkaTemplate;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private CacheManager cacheManager;

    @Value("${kafka.topic}")
    private String topic;

    @MessageMapping("/chat.sendMessage/{roomId}")
    @SendTo("/topic/{roomId}")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage
            , @DestinationVariable String roomId
    ) {
        redisTemplate.opsForList().rightPushAll(roomId, chatMessage);
        kafkaTemplate.send(topic, chatMessage.toString());
        return chatMessage;
    }

    @MessageMapping("/chat.addUser/{roomId}")
    @SendToUser("/queue/reply")
    public List<ChatMessage> addUser(
            @Payload ChatMessage chatMessage
            , @DestinationVariable String roomId
    ) {
        messagingTemplate.convertAndSend("/topic/" + roomId, chatMessage);
        List<ChatMessage> chatHistory = redisTemplate.opsForList().range(roomId, 0, -1);
        return chatHistory;
    }

    @GetMapping("/")
    public String main() {
        return "chat";
    }

    @GetMapping("/remove/{roomId}")
    @ResponseBody
    public void remove(@PathVariable(name = "roomId") String roomId) {
        redisTemplate.opsForList().trim(roomId, 1, 0); // This will remove all elements of the list.
    }

    @GetMapping("/get/{roomId}")
    @ResponseBody
    public List<ChatMessage> get(@PathVariable(name = "roomId") String roomId) {
        return redisTemplate.opsForList().range(roomId, 0, -1);
    }

    @GetMapping("/testCache/{roomId}")
    @ResponseBody
    @Cacheable(value = "testCache", key = "#roomId")
    public String testCache(@PathVariable(name = "roomId") String roomId) {
        Random rand = new Random();
        int n = rand.nextInt(50);
        System.out.println(n);
        return roomId + n; //5
    }

    @GetMapping("/testCache2/{roomId}")
    @ResponseBody
    @CachePut(value = "testCache", key = "#roomId")
    public String testCache2(@PathVariable(name = "roomId") String roomId) {
        Random rand = new Random();
        int n = rand.nextInt(50);
        System.out.println(n);
        return roomId + n;
    }

    @GetMapping("/getCache/{roomId}")
    @ResponseBody
    public String getCache(@PathVariable(name = "roomId") String roomId) {
        Cache cache = cacheManager.getCache("testCache");
        Cache.ValueWrapper valueWrapper = cache.get(roomId);
        if (valueWrapper != null) {
            return valueWrapper.get().toString();
        }
        return null;
    }
}