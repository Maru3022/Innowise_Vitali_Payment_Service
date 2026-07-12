package com.example.innowise_vitali_payment_service.repository;

import com.example.innowise_vitali_payment_service.entity.Payment;
import com.example.innowise_vitali_payment_service.entity.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PaymentRepositoryCustomImpl implements PaymentRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public List<Payment> findPaymentsByCriteria(String userId, String orderId, PaymentStatus status) {
        Query query = new Query();

        if (userId != null && !userId.isBlank()) {
            query.addCriteria(Criteria.where("user_id").is(userId));
        }

        if (orderId != null && !orderId.isBlank()) {
            query.addCriteria(Criteria.where("order_id").is(orderId));
        }

        if (status != null) {
            query.addCriteria(Criteria.where("status").is(status));
        }

        return mongoTemplate.find(query, Payment.class);
    }

    @Override
    public BigDecimal sumPaymentAmountByUserIdAndTimestampBetween(String userId, LocalDateTime from, LocalDateTime to) {
        Query query = new Query(
                Criteria.where("user_id").is(userId)
                        .and("timestamp").gte(from).lte(to)
        );

        return sumPaymentAmounts(query);
    }

    @Override
    public BigDecimal sumPaymentAmountByTimestampBetween(LocalDateTime from, LocalDateTime to) {
        Query query = new Query(
                Criteria.where("timestamp").gte(from).lte(to)
        );

        return sumPaymentAmounts(query);
    }

    private BigDecimal sumPaymentAmounts(Query query) {
        List<Payment> payments = mongoTemplate.find(query, Payment.class);

        return payments.stream()
                .map(Payment::getPaymentAmount)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}