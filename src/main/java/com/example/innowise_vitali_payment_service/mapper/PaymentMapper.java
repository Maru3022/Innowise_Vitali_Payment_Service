package com.example.innowise_vitali_payment_service.mapper;

import com.example.innowise_vitali_payment_service.dto.CreatePaymentRequest;
import com.example.innowise_vitali_payment_service.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "timestamp", ignore = true)
    Payment toEntity(CreatePaymentRequest request);

    com.example.paymentservice.dto.PaymentResponse toResponse(Payment payment);
}