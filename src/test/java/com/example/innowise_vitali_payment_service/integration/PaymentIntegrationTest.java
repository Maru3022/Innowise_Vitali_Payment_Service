package com.example.innowise_vitali_payment_service.integration;

import com.example.innowise_vitali_payment_service.dto.CreatePaymentRequest;
import com.example.innowise_vitali_payment_service.entity.PaymentStatus;
import com.example.innowise_vitali_payment_service.repository.PaymentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class PaymentIntegrationTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer(
            DockerImageName.parse("mongo:7.0")
    );

    @Container
    static KafkaContainer kafkaContainer = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.6.0")
    );

    static WireMockServer wireMockServer;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PaymentRepository paymentRepository;

    @BeforeAll
    static void beforeAll() {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMockServer.start();
    }

    @AfterAll
    static void afterAll() {
        wireMockServer.stop();
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
        registry.add("kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
        registry.add("external.random-api.url", () ->
                "http://localhost:" + wireMockServer.port() + "/random");
    }

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();
        wireMockServer.resetAll();
    }

    @Test
    void createPayment_withEvenNumber_returnsSuccess() throws Exception {
        stubRandomApi("[4]");

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest("order-1", "user-1", "99.99"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(PaymentStatus.SUCCESS.name()))
                .andExpect(jsonPath("$.orderId").value("order-1"))
                .andExpect(jsonPath("$.userId").value("user-1"))
                .andExpect(jsonPath("$.paymentAmount").value(99.99))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void createPayment_withOddNumber_returnsFailed() throws Exception {
        stubRandomApi("[5]");

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest("order-2", "user-2", "50.00"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(PaymentStatus.FAILED.name()));
    }

    @Test
    void createPayment_persistsPaymentInDatabase() throws Exception {
        stubRandomApi("[2]");

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest("order-3", "user-3", "100.00"))))
                .andExpect(status().isCreated());

        assertEquals(1, paymentRepository.count());
        assertEquals("order-3", paymentRepository.findAll().get(0).getOrderId());
    }

    @Test
    void createPayment_withBlankOrderId_returns400() throws Exception {
        CreatePaymentRequest request = CreatePaymentRequest.builder()
                .orderId("")
                .userId("user-1")
                .paymentAmount(new BigDecimal("10.00"))
                .build();

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createPayment_withNullPaymentAmount_returns400() throws Exception {
        CreatePaymentRequest request = CreatePaymentRequest.builder()
                .orderId("order-1")
                .userId("user-1")
                .paymentAmount(null)
                .build();

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createPayment_withZeroAmount_returns400() throws Exception {
        CreatePaymentRequest request = CreatePaymentRequest.builder()
                .orderId("order-1")
                .userId("user-1")
                .paymentAmount(BigDecimal.ZERO)
                .build();

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getByUserId_returnsPaymentsForUser() throws Exception {
        stubRandomApi("[2]");

        mockMvc.perform(post("/api/v1/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buildRequest("order-1", "user-10", "50.00"))));

        mockMvc.perform(get("/api/v1/payments/user/user-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].userId").value("user-10"));
    }

    @Test
    void getByUserId_whenNoPayments_returnsEmptyList() throws Exception {
        mockMvc.perform(get("/api/v1/payments/user/nonexistent-user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getByOrderId_returnsPaymentsForOrder() throws Exception {
        stubRandomApi("[2]");

        mockMvc.perform(post("/api/v1/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buildRequest("order-99", "user-1", "50.00"))));

        mockMvc.perform(get("/api/v1/payments/order/order-99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].orderId").value("order-99"));
    }

    @Test
    void getByOrderId_whenNoPayments_returnsEmptyList() throws Exception {
        mockMvc.perform(get("/api/v1/payments/order/nonexistent-order"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getByStatus_returnsSuccessPayments() throws Exception {
        stubRandomApi("[2]");

        mockMvc.perform(post("/api/v1/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buildRequest("order-1", "user-1", "50.00"))));

        mockMvc.perform(get("/api/v1/payments/status/SUCCESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("SUCCESS"));
    }

    @Test
    void getSumForUser_returnsCorrectTotal() throws Exception {
        stubRandomApi("[2]");

        mockMvc.perform(post("/api/v1/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buildRequest("order-1", "user-sum", "100.00"))));

        mockMvc.perform(get("/api/v1/payments/sum/user/user-sum")
                        .param("from", "2020-01-01T00:00:00")
                        .param("to", "2099-01-01T00:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(100.00));
    }

    @Test
    void getSumForUser_withoutParams_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/payments/sum/user/user-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getSumForAdmin_returnsCorrectTotal() throws Exception {
        stubRandomApi("[2]");

        mockMvc.perform(post("/api/v1/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buildRequest("order-1", "user-1", "200.00"))));

        mockMvc.perform(get("/api/v1/payments/sum/admin")
                        .param("from", "2020-01-01T00:00:00")
                        .param("to", "2099-01-01T00:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(200.00));
    }

    private void stubRandomApi(String body) {
        wireMockServer.stubFor(WireMock.get(WireMock.urlPathEqualTo("/random"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(body)));
    }

    private CreatePaymentRequest buildRequest(String orderId, String userId, String amount) {
        return CreatePaymentRequest.builder()
                .orderId(orderId)
                .userId(userId)
                .paymentAmount(new BigDecimal(amount))
                .build();
    }
}