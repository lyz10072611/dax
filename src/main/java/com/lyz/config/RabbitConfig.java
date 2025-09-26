package com.lyz.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitConfig {
    public static final String EXCHANGE = "download.exchange";
    public static final String QUEUE = "download.queue";
    public static final String ROUTING_KEY = "download.task";

    public static final String UPLOAD_EXCHANGE = "upload.exchange";
    public static final String UPLOAD_QUEUE = "upload.queue";
    public static final String UPLOAD_ROUTING_KEY = "upload.task";

    @Bean
    public TopicExchange downloadExchange() { return new TopicExchange(EXCHANGE, true, false); }

    @Bean
    public Queue downloadQueue() { return new Queue(QUEUE, true); }

    @Bean
    public Binding binding() { return BindingBuilder.bind(downloadQueue()).to(downloadExchange()).with(ROUTING_KEY); }

    @Bean
    public TopicExchange uploadExchange() {
        return new TopicExchange(UPLOAD_EXCHANGE, true, false);
    }

    @Bean
    public Queue uploadQueue() {
        return new Queue(UPLOAD_QUEUE, true);
    }

    @Bean
    public Binding uploadBinding() {
        return BindingBuilder.bind(uploadQueue()).to(uploadExchange()).with(UPLOAD_ROUTING_KEY);
    }
}


