package com.example.innowise_vitali_payment_service.service;

import com.example.events.PaymentEvent;
import com.example.innowise_vitali_payment_service.client.RandomNumberClient;
import com.example.innowise_vitali_payment_service.dto.CreatePaymentRequest;
import com.example.innowise_vitali_payment_service.dto.PaymentResponse;
import com.example.innowise_vitali_payment_service.dto.PaymentSumResponse;
import com.example.innowise_vitali_payment_service.entity.Payment;
import com.example.innowise_vitali_payment_service.entity.PaymentStatus;
import com.example.innowise_vitali_payment_service.kafka.PaymentProducer;
import com.example.innowise_vitali_payment_service.mapper.PaymentMapper;
import com.example.innowise_vitali_payment_service.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentProducer paymentProducer;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final RandomNumberClient randomNumberClient;

    public PaymentResponse createPayment(CreatePaymentRequest request) {
        Payment payment = paymentMapper.toEntity(request);
        payment.setTimestamp(LocalDateTime.now());

        int randomNumber = randomNumberClient.getRandomNumber();
        payment.setStatus(randomNumber % 2 == 0 ? PaymentStatus.SUCCESS : PaymentStatus.FAILED);

        Payment saved = paymentRepository.save(payment);

        PaymentEvent event = paymentProducer.createPaymentEvent(
                saved.getId(),
                saved.getOrderId(),
                saved.getUserId(),
                saved.getStatus()
        );
        paymentProducer.sendPaymentEvent(event);

        return paymentMapper.toResponse(saved);
    }

    public List<PaymentResponse> getPaymentsByUserId(String userId) {
        return paymentRepository.findByUserId(userId)
                .stream()
                .map(paymentMapper::toResponse)
                .toList();
    }

    public List<PaymentResponse> getPaymentsByOrderId(String orderId) {
        return paymentRepository.findByOrderId(orderId)
                .stream()
                .map(paymentMapper::toResponse)
                .toList();
    }

    public List<PaymentResponse> getPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status)
                .stream()
                .map(paymentMapper::toResponse)
                .toList();
    }

    public PaymentSumResponse getTotalForCurrentUser(String userId, LocalDateTime from, LocalDateTime to) {
        BigDecimal total = paymentRepository.sumPaymentAmountByUserIdAndTimestampBetween(userId, from, to);
        return PaymentSumResponse.builder()
                .total(total != null ? total : BigDecimal.ZERO)
                .build();
    }

    public PaymentSumResponse getTotalForAllUsers(LocalDateTime from, LocalDateTime to) {
        BigDecimal total = paymentRepository.sumPaymentAmountByTimestampBetween(from, to);
        return PaymentSumResponse.builder()
                .total(total != null ? total : BigDecimal.ZERO)
                .build();
    }
}