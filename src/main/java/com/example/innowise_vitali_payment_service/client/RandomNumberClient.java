package com.example.innowise_vitali_payment_service.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class RandomNumberClient {

    private final RestTemplate restTemplate;
    private final String randomApiUrl;

    public RandomNumberClient(
            RestTemplate restTemplate,
            @Value("${external.random-api.url}") String randomApiUrl
    ) {
        this.restTemplate = restTemplate;
        this.randomApiUrl = randomApiUrl;
    }

    public int getRandomNumber() {
        int[] response = restTemplate.getForObject(randomApiUrl, int[].class);
        return response[0];
    }
}