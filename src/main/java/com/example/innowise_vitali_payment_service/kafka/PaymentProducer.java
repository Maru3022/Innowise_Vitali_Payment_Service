package com.example.innowise_vitali_payment_service.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentProducer {

    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    @Value("${kafka.topic.payment}")
    private String topic;

    public void sendPaymentEvent(PaymentEvent event) {
        kafkaTemplate.send(topic, event.getPaymentId(), event);
    }
}