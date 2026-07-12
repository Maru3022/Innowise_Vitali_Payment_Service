package com.example.innowise_vitali_payment_service.kafka;

import com.example.events.PaymentEvent;
import com.example.events.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentProducer {

    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    @Value("${kafka.topic.payment-result}")
    private String topic;

    public void sendPaymentEvent(PaymentEvent event) {
        kafkaTemplate.send(topic, event.getPaymentId(), event);
    }

    public PaymentEvent createPaymentEvent(String paymentId, String orderId, String userId,
            com.example.innowise_vitali_payment_service.entity.PaymentStatus status) {
        PaymentEvent avroEvent = new PaymentEvent();
        avroEvent.setPaymentId(paymentId);
        avroEvent.setOrderId(orderId);
        avroEvent.setUserId(userId);
        avroEvent.setStatus(PaymentStatus.valueOf(status.name()));
        return avroEvent;
    }
}