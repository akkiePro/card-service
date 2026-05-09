package com.zbank.cardservice.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreditScoreResponse {
    private Long customerId;
    private Integer creditScore;
    private String scoreSource;
}
