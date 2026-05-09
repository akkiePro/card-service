package com.zbank.cardservice.service;

import com.zbank.cardservice.dto.request.CreditScoreRequest;
import com.zbank.cardservice.dto.response.CreditScoreResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class CreditRatingService {

    private static final BigDecimal HIGH_SALARY_THRESHOLD = new BigDecimal("200000");
    private static final BigDecimal MID_SALARY_THRESHOLD = new BigDecimal("50000");
    private static final int HIGH_SALARY_SCORE = 500;
    private static final int MID_SALARY_SCORE = 150;
    private static final int LOW_SALARY_SCORE = 50;
    private static final int MULTI_CARD_SCORE = 300;

    public CreditScoreResponse calculateCreditScore(CreditScoreRequest request) {
        if (request.getExistingCreditScore() != null && request.getExistingCreditScore() > 0) {
            return CreditScoreResponse.builder()
                    .customerId(request.getCustomerId())
                    .creditScore(request.getExistingCreditScore())
                    .scoreSource("EXISTING")
                    .build();
        }

        int score = calculateSalaryScore(request.getAnnualSalary());

        // Holding 2+ credit cards caps score at 300 (risk factor)
        if (request.getExistingCreditCards() >= 2) {
            score = Math.min(score, MULTI_CARD_SCORE);
        }

        return CreditScoreResponse.builder()
                .customerId(request.getCustomerId())
                .creditScore(score)
                .scoreSource("CALCULATED")
                .build();
    }

    private int calculateSalaryScore(BigDecimal annualSalary) {
        if (annualSalary.compareTo(HIGH_SALARY_THRESHOLD) > 0) {
            return HIGH_SALARY_SCORE;
        } else if (annualSalary.compareTo(MID_SALARY_THRESHOLD) > 0) {
            return MID_SALARY_SCORE;
        } else {
            return LOW_SALARY_SCORE;
        }
    }
}
