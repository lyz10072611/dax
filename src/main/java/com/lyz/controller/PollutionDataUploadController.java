package com.lyz.controller;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.lyz.config.RabbitConfig.UPLOAD_EXCHANGE;
import static com.lyz.config.RabbitConfig.UPLOAD_ROUTING_KEY;

@RestController
@RequestMapping("/pollution/upload")
public class PollutionDataUploadController {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostMapping
    public ResponseEntity<String> uploadData(@RequestBody Map<String, Object> data) {
        rabbitTemplate.convertAndSend(UPLOAD_EXCHANGE, UPLOAD_ROUTING_KEY, data);
        return ResponseEntity.ok("Upload task submitted successfully.");
    }
}