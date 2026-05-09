package com.zbank.cardservice.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreditScoreRequest {

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "Annual salary is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Annual salary must be positive")
    private BigDecimal annualSalary;

    @NotNull(message = "Existing credit cards count is required")
    @Min(value = 0, message = "Existing credit cards count cannot be negative")
    private Integer existingCreditCards;

    private Integer existingCreditScore;
}
