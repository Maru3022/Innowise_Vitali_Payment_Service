package com.example.innowise_vitali_payment_service.kafka;

import com.example.innowise_vitali_payment_service.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEvent {

    private String paymentId;
    private String orderId;
    private String userId;
    private PaymentStatus status;
}