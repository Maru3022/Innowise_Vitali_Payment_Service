package com.example.innowise_vitali_payment_service.kafka;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${schema.registry.url:}")
    private String schemaRegistryUrl;

    @Value("${spring.kafka.producer.properties.auto.register.schemas:false}")
    private boolean autoRegisterSchemas;

    @Bean
    public ProducerFactory<String, com.example.events.PaymentEvent> paymentEventProducerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);

        // Only set schema registry URL if it's provided
        if (!schemaRegistryUrl.isEmpty()) {
            config.put("schema.registry.url", schemaRegistryUrl);
        }

        // Producer reliability settings
        config.put(ProducerConfig.ACKS_CONFIG, "all");
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        config.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
        config.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);

        // Schema registry settings
        config.put("auto.register.schemas", autoRegisterSchemas);
        config.put("use.latest.version", true);

        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, com.example.events.PaymentEvent> paymentEventKafkaTemplate() {
        return new KafkaTemplate<>(paymentEventProducerFactory());
    }
}
