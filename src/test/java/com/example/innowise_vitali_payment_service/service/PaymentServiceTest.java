package com.example.innowise_vitali_payment_service.service;

import com.example.innowise_vitali_payment_service.client.RandomNumberClient;
import com.example.innowise_vitali_payment_service.dto.CreatePaymentRequest;
import com.example.innowise_vitali_payment_service.dto.PaymentResponse;
import com.example.innowise_vitali_payment_service.dto.PaymentSumResponse;
import com.example.innowise_vitali_payment_service.entity.Payment;
import com.example.innowise_vitali_payment_service.entity.PaymentStatus;
import com.example.events.PaymentEvent;
import com.example.innowise_vitali_payment_service.kafka.PaymentProducer;
import com.example.innowise_vitali_payment_service.mapper.PaymentMapper;
import com.example.innowise_vitali_payment_service.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private RandomNumberClient randomNumberClient;

    @Mock
    private PaymentProducer paymentProducer;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void createPayment_whenRandomNumberIsEven_thenStatusIsSuccess() {
        CreatePaymentRequest request = buildRequest("order-1", "user-1", BigDecimal.TEN);
        Payment payment = new Payment();
        Payment saved = buildPayment("pay-1", "order-1", "user-1", PaymentStatus.SUCCESS, BigDecimal.TEN);
        PaymentResponse response = buildResponse("pay-1", PaymentStatus.SUCCESS);

        when(paymentMapper.toEntity(request)).thenReturn(payment);
        when(randomNumberClient.getRandomNumber()).thenReturn(4);
        when(paymentRepository.save(any())).thenReturn(saved);
        when(paymentMapper.toResponse(saved)).thenReturn(response);

        PaymentResponse result = paymentService.createPayment(request);

        assertEquals(PaymentStatus.SUCCESS, result.getStatus());
        verify(paymentProducer).sendPaymentEvent(any(PaymentEvent.class));
        verify(paymentRepository).save(any());
    }

    @Test
    void createPayment_whenRandomNumberIsOdd_thenStatusIsFailed() {
        CreatePaymentRequest request = buildRequest("order-2", "user-2", BigDecimal.TEN);
        Payment payment = new Payment();
        Payment saved = buildPayment("pay-2", "order-2", "user-2", PaymentStatus.FAILED, BigDecimal.TEN);
        PaymentResponse response = buildResponse("pay-2", PaymentStatus.FAILED);

        when(paymentMapper.toEntity(request)).thenReturn(payment);
        when(randomNumberClient.getRandomNumber()).thenReturn(3);
        when(paymentRepository.save(any())).thenReturn(saved);
        when(paymentMapper.toResponse(saved)).thenReturn(response);

        PaymentResponse result = paymentService.createPayment(request);

        assertEquals(PaymentStatus.FAILED, result.getStatus());
        verify(paymentProducer).sendPaymentEvent(any(PaymentEvent.class));
    }

    @Test
    void createPayment_whenRandomNumberIsZero_thenStatusIsSuccess() {
        CreatePaymentRequest request = buildRequest("order-3", "user-3", BigDecimal.TEN);
        Payment payment = new Payment();
        Payment saved = buildPayment("pay-3", "order-3", "user-3", PaymentStatus.SUCCESS, BigDecimal.TEN);
        PaymentResponse response = buildResponse("pay-3", PaymentStatus.SUCCESS);

        when(paymentMapper.toEntity(request)).thenReturn(payment);
        when(randomNumberClient.getRandomNumber()).thenReturn(0);
        when(paymentRepository.save(any())).thenReturn(saved);
        when(paymentMapper.toResponse(saved)).thenReturn(response);

        PaymentResponse result = paymentService.createPayment(request);

        assertEquals(PaymentStatus.SUCCESS, result.getStatus());
    }

    @Test
    void createPayment_whenExternalApiThrows_thenExceptionPropagates() {
        CreatePaymentRequest request = buildRequest("order-4", "user-4", BigDecimal.TEN);
        Payment payment = new Payment();

        when(paymentMapper.toEntity(request)).thenReturn(payment);
        when(randomNumberClient.getRandomNumber()).thenThrow(new RuntimeException("External API unavailable"));

        assertThrows(RuntimeException.class, () -> paymentService.createPayment(request));
        verify(paymentRepository, never()).save(any());
        verify(paymentProducer, never()).sendPaymentEvent(any());
    }

    @Test
    void createPayment_setsTimestampBeforeSaving() {
        CreatePaymentRequest request = buildRequest("order-5", "user-5", BigDecimal.TEN);
        Payment payment = new Payment();
        Payment saved = buildPayment("pay-5", "order-5", "user-5", PaymentStatus.SUCCESS, BigDecimal.TEN);
        PaymentResponse response = buildResponse("pay-5", PaymentStatus.SUCCESS);

        when(paymentMapper.toEntity(request)).thenReturn(payment);
        when(randomNumberClient.getRandomNumber()).thenReturn(2);
        when(paymentRepository.save(any())).thenReturn(saved);
        when(paymentMapper.toResponse(saved)).thenReturn(response);

        paymentService.createPayment(request);

        assertNotNull(payment.getTimestamp());
        assertTrue(payment.getTimestamp().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void createPayment_sendsKafkaEventWithCorrectData() {
        CreatePaymentRequest request = buildRequest("order-6", "user-6", new BigDecimal("99.99"));
        Payment payment = new Payment();
        Payment saved = buildPayment("pay-6", "order-6", "user-6", PaymentStatus.SUCCESS, new BigDecimal("99.99"));
        PaymentResponse response = buildResponse("pay-6", PaymentStatus.SUCCESS);

        when(paymentMapper.toEntity(request)).thenReturn(payment);
        when(randomNumberClient.getRandomNumber()).thenReturn(2);
        when(paymentRepository.save(any())).thenReturn(saved);
        when(paymentMapper.toResponse(saved)).thenReturn(response);

        paymentService.createPayment(request);

        verify(paymentProducer).sendPaymentEvent(argThat(event ->
                "pay-6".equals(event.getPaymentId()) &&
                        "order-6".equals(event.getOrderId()) &&
                        "user-6".equals(event.getUserId()) &&
                        PaymentStatus.SUCCESS.equals(event.getStatus())
        ));
    }

    @Test
    void getPaymentsByUserId_returnsListOfResponses() {
        Payment p = buildPayment("1", "order-1", "user-1", PaymentStatus.SUCCESS, BigDecimal.TEN);
        PaymentResponse r = buildResponse("1", PaymentStatus.SUCCESS);

        when(paymentRepository.findByUserId("user-1")).thenReturn(List.of(p));
        when(paymentMapper.toResponse(p)).thenReturn(r);

        List<PaymentResponse> result = paymentService.getPaymentsByUserId("user-1");

        assertEquals(1, result.size());
        assertEquals("1", result.get(0).getId());
    }

    @Test
    void getPaymentsByUserId_whenNoPayments_returnsEmptyList() {
        when(paymentRepository.findByUserId("unknown-user")).thenReturn(Collections.emptyList());

        List<PaymentResponse> result = paymentService.getPaymentsByUserId("unknown-user");

        assertTrue(result.isEmpty());
        verify(paymentMapper, never()).toResponse(any());
    }

    @Test
    void getPaymentsByUserId_returnsMultiplePayments() {
        Payment p1 = buildPayment("1", "order-1", "user-1", PaymentStatus.SUCCESS, BigDecimal.TEN);
        Payment p2 = buildPayment("2", "order-2", "user-1", PaymentStatus.FAILED, BigDecimal.ONE);
        PaymentResponse r1 = buildResponse("1", PaymentStatus.SUCCESS);
        PaymentResponse r2 = buildResponse("2", PaymentStatus.FAILED);

        when(paymentRepository.findByUserId("user-1")).thenReturn(List.of(p1, p2));
        when(paymentMapper.toResponse(p1)).thenReturn(r1);
        when(paymentMapper.toResponse(p2)).thenReturn(r2);

        List<PaymentResponse> result = paymentService.getPaymentsByUserId("user-1");

        assertEquals(2, result.size());
    }

    @Test
    void getPaymentsByOrderId_returnsCorrectPayments() {
        Payment p = buildPayment("1", "order-1", "user-1", PaymentStatus.SUCCESS, BigDecimal.TEN);
        PaymentResponse r = buildResponse("1", PaymentStatus.SUCCESS);

        when(paymentRepository.findByOrderId("order-1")).thenReturn(List.of(p));
        when(paymentMapper.toResponse(p)).thenReturn(r);

        List<PaymentResponse> result = paymentService.getPaymentsByOrderId("order-1");

        assertEquals(1, result.size());
    }

    @Test
    void getPaymentsByOrderId_whenNoPayments_returnsEmptyList() {
        when(paymentRepository.findByOrderId("nonexistent-order")).thenReturn(Collections.emptyList());

        List<PaymentResponse> result = paymentService.getPaymentsByOrderId("nonexistent-order");

        assertTrue(result.isEmpty());
    }

    @Test
    void getPaymentsByStatus_returnsSuccessPayments() {
        Payment p = buildPayment("1", "order-1", "user-1", PaymentStatus.SUCCESS, BigDecimal.TEN);
        PaymentResponse r = buildResponse("1", PaymentStatus.SUCCESS);

        when(paymentRepository.findByStatus(PaymentStatus.SUCCESS)).thenReturn(List.of(p));
        when(paymentMapper.toResponse(p)).thenReturn(r);

        List<PaymentResponse> result = paymentService.getPaymentsByStatus(PaymentStatus.SUCCESS);

        assertEquals(1, result.size());
        assertEquals(PaymentStatus.SUCCESS, result.get(0).getStatus());
    }

    @Test
    void getPaymentsByStatus_returnsFailedPayments() {
        Payment p = buildPayment("2", "order-2", "user-2", PaymentStatus.FAILED, BigDecimal.ONE);
        PaymentResponse r = buildResponse("2", PaymentStatus.FAILED);

        when(paymentRepository.findByStatus(PaymentStatus.FAILED)).thenReturn(List.of(p));
        when(paymentMapper.toResponse(p)).thenReturn(r);

        List<PaymentResponse> result = paymentService.getPaymentsByStatus(PaymentStatus.FAILED);

        assertEquals(1, result.size());
        assertEquals(PaymentStatus.FAILED, result.get(0).getStatus());
    }

    @Test
    void getPaymentsByStatus_whenNoPayments_returnsEmptyList() {
        when(paymentRepository.findByStatus(PaymentStatus.SUCCESS)).thenReturn(Collections.emptyList());

        List<PaymentResponse> result = paymentService.getPaymentsByStatus(PaymentStatus.SUCCESS);

        assertTrue(result.isEmpty());
    }

    @Test
    void getTotalForCurrentUser_returnsCorrectSum() {
        LocalDateTime from = LocalDateTime.now().minusDays(7);
        LocalDateTime to = LocalDateTime.now();

        when(paymentRepository.sumPaymentAmountByUserIdAndTimestampBetween("user-1", from, to))
                .thenReturn(new BigDecimal("250.00"));

        PaymentSumResponse result = paymentService.getTotalForCurrentUser("user-1", from, to);

        assertEquals(new BigDecimal("250.00"), result.getTotal());
    }

    @Test
    void getTotalForCurrentUser_whenResultIsNull_returnsZero() {
        LocalDateTime from = LocalDateTime.now().minusDays(7);
        LocalDateTime to = LocalDateTime.now();

        when(paymentRepository.sumPaymentAmountByUserIdAndTimestampBetween("user-1", from, to))
                .thenReturn(null);

        PaymentSumResponse result = paymentService.getTotalForCurrentUser("user-1", from, to);

        assertEquals(BigDecimal.ZERO, result.getTotal());
    }

    @Test
    void getTotalForCurrentUser_whenNoPaymentsInRange_returnsZero() {
        LocalDateTime from = LocalDateTime.of(2020, 1, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2020, 1, 2, 0, 0);

        when(paymentRepository.sumPaymentAmountByUserIdAndTimestampBetween("user-1", from, to))
                .thenReturn(null);

        PaymentSumResponse result = paymentService.getTotalForCurrentUser("user-1", from, to);

        assertEquals(BigDecimal.ZERO, result.getTotal());
    }

    @Test
    void getTotalForAllUsers_returnsCorrectSum() {
        LocalDateTime from = LocalDateTime.now().minusDays(30);
        LocalDateTime to = LocalDateTime.now();

        when(paymentRepository.sumPaymentAmountByTimestampBetween(from, to))
                .thenReturn(new BigDecimal("1500.00"));

        PaymentSumResponse result = paymentService.getTotalForAllUsers(from, to);

        assertEquals(new BigDecimal("1500.00"), result.getTotal());
    }

    @Test
    void getTotalForAllUsers_whenResultIsNull_returnsZero() {
        LocalDateTime from = LocalDateTime.now().minusDays(30);
        LocalDateTime to = LocalDateTime.now();

        when(paymentRepository.sumPaymentAmountByTimestampBetween(from, to))
                .thenReturn(null);

        PaymentSumResponse result = paymentService.getTotalForAllUsers(from, to);

        assertEquals(BigDecimal.ZERO, result.getTotal());
    }

    private CreatePaymentRequest buildRequest(String orderId, String userId, BigDecimal amount) {
        return CreatePaymentRequest.builder()
                .orderId(orderId)
                .userId(userId)
                .paymentAmount(amount)
                .build();
    }

    private Payment buildPayment(String id, String orderId, String userId, PaymentStatus status, BigDecimal amount) {
        return Payment.builder()
                .id(id)
                .orderId(orderId)
                .userId(userId)
                .status(status)
                .paymentAmount(amount)
                .timestamp(LocalDateTime.now())
                .build();
    }

    private PaymentResponse buildResponse(String id, PaymentStatus status) {
        return PaymentResponse.builder()
                .id(id)
                .status(status)
                .build();
    }
}