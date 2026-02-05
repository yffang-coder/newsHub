package com.newshub.backend.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.newshub.backend.domain.model.Article;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@Profile("!local")
public class KafkaProducerService implements NewsProducer {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;

    private static final String TOPIC = "news-crawler-topic";

    @Override
    public void sendNews(Article article) {
        try {
            String message = objectMapper.writeValueAsString(article);
            log.info("Producing news to Kafka: {}", article.getTitle());
            kafkaTemplate.send(TOPIC, message);
        } catch (JsonProcessingException e) {
            log.error("Error serializing article", e);
        }
    }
}

