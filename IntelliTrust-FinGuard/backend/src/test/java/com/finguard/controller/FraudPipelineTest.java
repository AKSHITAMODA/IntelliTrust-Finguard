package com.finguard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finguard.dto.auth.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end check of the hybrid fraud pipeline: a run of small, local, evenly
 * spaced transactions establishes a normal baseline, then one large transaction
 * from far away should be flagged as fraud with a high risk score.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FraudPipelineTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    private String registerAndLogin(String email) throws Exception {
        RegisterRequest register = new RegisterRequest("Fraud Test User", email, "Secret123");
        var result = mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    @Test
    void flagsLargeFarAwayTransactionAsFraud() throws Exception {
        String token = registerAndLogin("fraud.test@example.com");

        mockMvc.perform(post("/api/location")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("latitude", 12.9716, "longitude", 77.5946, "accuracy", 15))))
                .andExpect(status().isOk());

        for (int i = 0; i < 6; i++) {
            Map<String, Object> normalTxn = Map.of(
                    "txnDate", LocalDateTime.now().minusDays(10 - i).toString(),
                    "description", "Swiggy order",
                    "merchant", "Swiggy",
                    "category", "FOOD",
                    "amount", 450 + i * 10,
                    "txnType", "DEBIT",
                    "latitude", 12.9716,
                    "longitude", 77.5946,
                    "city", "Bengaluru",
                    "country", "India"
            );
            mockMvc.perform(post("/api/transactions")
                            .header("Authorization", "Bearer " + token)
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(normalTxn)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.fraud").value(false));
        }

        Map<String, Object> suspiciousTxn = Map.of(
                "txnDate", LocalDateTime.now().toString(),
                "description", "High value electronics purchase",
                "merchant", "Unknown Electronics Wholesaler",
                "category", "SHOPPING",
                "amount", 500000,
                "txnType", "DEBIT",
                "latitude", 40.7128,
                "longitude", -74.0060,
                "city", "Unknown",
                "country", "Unknown"
        );

        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(suspiciousTxn)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fraud").value(true))
                .andExpect(jsonPath("$.fraudReason").isNotEmpty());
    }
}
