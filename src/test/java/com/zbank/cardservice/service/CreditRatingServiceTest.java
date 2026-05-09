package com.zbank.cardservice.service;

import com.zbank.cardservice.dto.request.CreditScoreRequest;
import com.zbank.cardservice.dto.response.CreditScoreResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class CreditRatingServiceTest {

    private CreditRatingService creditRatingService;

    @BeforeEach
    void setUp() {
        creditRatingService = new CreditRatingService();
    }

    @Test
    void calculateScore_returnsExistingScore_whenAvailable() {
        CreditScoreRequest request = new CreditScoreRequest();
        request.setCustomerId(1L);
        request.setAnnualSalary(new BigDecimal("100000"));
        request.setExistingCreditCards(0);
        request.setExistingCreditScore(750);

        CreditScoreResponse response = creditRatingService.calculateCreditScore(request);

        assertThat(response.getCreditScore()).isEqualTo(750);
        assertThat(response.getScoreSource()).isEqualTo("EXISTING");
    }

    @Test
    void calculateScore_returns500_forHighSalaryNoCards() {
        CreditScoreRequest request = buildRequest(new BigDecimal("300000"), 0, null);
        CreditScoreResponse response = creditRatingService.calculateCreditScore(request);

        assertThat(response.getCreditScore()).isEqualTo(500);
        assertThat(response.getScoreSource()).isEqualTo("CALCULATED");
    }

    @Test
    void calculateScore_returns150_forMidSalaryNoCards() {
        CreditScoreRequest request = buildRequest(new BigDecimal("100000"), 0, null);
        CreditScoreResponse response = creditRatingService.calculateCreditScore(request);

        assertThat(response.getCreditScore()).isEqualTo(150);
    }

    @Test
    void calculateScore_returns50_forLowSalaryNoCards() {
        CreditScoreRequest request = buildRequest(new BigDecimal("30000"), 0, null);
        CreditScoreResponse response = creditRatingService.calculateCreditScore(request);

        assertThat(response.getCreditScore()).isEqualTo(50);
    }

    @Test
    void calculateScore_caps300_whenHighSalaryAnd2PlusCards() {
        CreditScoreRequest request = buildRequest(new BigDecimal("300000"), 2, null);
        CreditScoreResponse response = creditRatingService.calculateCreditScore(request);

        assertThat(response.getCreditScore()).isEqualTo(300);
    }

    @Test
    void calculateScore_returns150_whenMidSalaryAnd2PlusCards() {
        CreditScoreRequest request = buildRequest(new BigDecimal("100000"), 3, null);
        CreditScoreResponse response = creditRatingService.calculateCreditScore(request);

        assertThat(response.getCreditScore()).isEqualTo(150);
    }

    @Test
    void calculateScore_returns50_whenLowSalaryAnd2PlusCards() {
        CreditScoreRequest request = buildRequest(new BigDecimal("25000"), 2, null);
        CreditScoreResponse response = creditRatingService.calculateCreditScore(request);

        assertThat(response.getCreditScore()).isEqualTo(50);
    }

    private CreditScoreRequest buildRequest(BigDecimal salary, int cards, Integer existingScore) {
        CreditScoreRequest request = new CreditScoreRequest();
        request.setCustomerId(1L);
        request.setAnnualSalary(salary);
        request.setExistingCreditCards(cards);
        request.setExistingCreditScore(existingScore);
        return request;
    }
}
