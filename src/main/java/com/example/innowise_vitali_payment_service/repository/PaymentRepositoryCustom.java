package com.example.innowise_vitali_payment_service.repository;

import com.example.innowise_vitali_payment_service.entity.Payment;
import com.example.innowise_vitali_payment_service.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface PaymentRepositoryCustom {

    List<Payment> findPaymentsByCriteria(String userId, String orderId, PaymentStatus status);

    BigDecimal sumPaymentAmountByUserIdAndTimestampBetween(String userId, LocalDateTime from, LocalDateTime to);

    BigDecimal sumPaymentAmountByTimestampBetween(LocalDateTime from, LocalDateTime to);
}