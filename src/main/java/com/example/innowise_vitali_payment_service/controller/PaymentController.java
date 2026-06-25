package com.example.innowise_vitali_payment_service.controller;

import com.example.innowise_vitali_payment_service.dto.CreatePaymentRequest;
import com.example.innowise_vitali_payment_service.dto.PaymentResponse;
import com.example.innowise_vitali_payment_service.dto.PaymentSumResponse;
import com.example.innowise_vitali_payment_service.entity.PaymentStatus;
import com.example.innowise_vitali_payment_service.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        return paymentService.createPayment(request);
    }

    @GetMapping("/user/{userId}")
    public List<PaymentResponse> getByUserId(@PathVariable String userId) {
        return paymentService.getPaymentsByUserId(userId);
    }

    @GetMapping("/order/{orderId}")
    public List<PaymentResponse> getByOrderId(@PathVariable String orderId) {
        return paymentService.getPaymentsByOrderId(orderId);
    }

    @GetMapping("/status/{status}")
    public List<PaymentResponse> getByStatus(@PathVariable PaymentStatus status) {
        return paymentService.getPaymentsByStatus(status);
    }

    @GetMapping("/sum/user/{userId}")
    public PaymentSumResponse getSumForUser(
            @PathVariable String userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        return paymentService.getTotalForCurrentUser(userId, from, to);
    }

    @GetMapping("/sum/admin")
    public PaymentSumResponse getSumForAllUsers(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        return paymentService.getTotalForAllUsers(from, to);
    }
}