package com.zbank.cardservice.dto.response;

import com.zbank.cardservice.model.enums.CardStatus;
import com.zbank.cardservice.model.enums.CardType;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class CardActivationResponse {
    private Long cardId;
    private Long customerId;
    private String cardNumber;
    private CardType cardType;
    private BigDecimal creditLimit;
    private CardStatus status;
    private String firstTimePin;
    private String message;
}
