package com.example.innowise_vitali_payment_service.repository;

import com.example.innowise_vitali_payment_service.entity.Payment;
import com.example.innowise_vitali_payment_service.entity.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.bson.types.Decimal128;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
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
        MatchOperation matchStage = Aggregation.match(
                Criteria.where("user_id").is(userId)
                        .and("timestamp").gte(from).lte(to)
        );

        GroupOperation groupStage = Aggregation.group().sum("payment_amount").as("totalAmount");

        Aggregation aggregation = Aggregation.newAggregation(matchStage, groupStage);
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, Payment.class, Document.class);

        Document uniqueResult = results.getUniqueMappedResult();

        return extractBigDecimal(uniqueResult, "totalAmount");
    }

    @Override
    public BigDecimal sumPaymentAmountByTimestampBetween(LocalDateTime from, LocalDateTime to) {
        MatchOperation matchStage = Aggregation.match(
                Criteria.where("timestamp").gte(from).lte(to)
        );

        GroupOperation groupStage = Aggregation.group().sum("payment_amount").as("totalAmount");

        Aggregation aggregation = Aggregation.newAggregation(matchStage, groupStage);
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, Payment.class, Document.class);

        Document uniqueResult = results.getUniqueMappedResult();

        return extractBigDecimal(uniqueResult, "totalAmount");
    }

    private BigDecimal extractBigDecimal(Document resultDoc, String fieldName) {
        if (resultDoc != null && resultDoc.get(fieldName) != null) {
            Object rawValue = resultDoc.get(fieldName);
            if (rawValue instanceof Decimal128) {
                return ((Decimal128) rawValue).bigDecimalValue();
            } else if (rawValue instanceof Number) {
                return BigDecimal.valueOf(((Number) rawValue).doubleValue());
            }
        }
        return BigDecimal.ZERO;
    }
}