package com.example.innowise_vitali_payment_service.repository;

import com.example.innowise_vitali_payment_service.entity.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends MongoRepository<Payment, String>, PaymentRepositoryCustom {
}