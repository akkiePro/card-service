package com.zbank.cardservice.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CardActivationRequest {

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "Credit score is required")
    private Integer creditScore;
}
