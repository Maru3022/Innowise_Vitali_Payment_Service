package com.example.innowise_vitali_payment_service.kafka;

import com.example.events.OrderCreatedEvent;
import com.example.innowise_vitali_payment_service.dto.CreatePaymentRequest;
import com.example.innowise_vitali_payment_service.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreatedEventConsumer {

    private final PaymentService paymentService;

    @Value("${kafka.topic.payment}")
    private String paymentTopic;

    @KafkaListener(topics = "${kafka.topic.payment}", groupId = "payment-service-group")
    public void consumeOrderCreatedEvent(OrderCreatedEvent event) {
        log.info("Received OrderCreatedEvent from topic {}: orderId={}, userId={}, totalPrice={}",
                paymentTopic, event.getOrderId(), event.getUserId(), event.getTotalPrice());

        CreatePaymentRequest request = CreatePaymentRequest.builder()
                .orderId(event.getOrderId())
                .userId(event.getUserId())
                .paymentAmount(event.getTotalPrice())
                .build();

        try {
            paymentService.createPayment(request);
            log.info("Payment created for orderId={}", event.getOrderId());
        } catch (Exception e) {
            log.error("Failed to create payment for orderId={}", event.getOrderId(), e);
            throw e; // Re-throw to trigger retry mechanism
        }
    }
}