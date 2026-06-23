package com.example.innowise_vitali_payment_service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentRequest {

    @NotBlank
    private String orderId;

    @NotBlank
    private String userId;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal paymentAmount;
}