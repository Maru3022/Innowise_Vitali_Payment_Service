package com.example.innowise_vitali_payment_service.repository;

import com.example.innowise_vitali_payment_service.entity.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PaymentRepository extends MongoRepository<Payment, String>, PaymentRepositoryCustom {
    List<Payment> findByUserId(String userId);
    List<Payment> findByOrderId(String orderId);
    List<Payment> findByStatus(com.example.innowise_vitali_payment_service.entity.PaymentStatus status);
}