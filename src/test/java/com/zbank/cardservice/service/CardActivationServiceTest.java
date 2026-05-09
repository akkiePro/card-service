package com.zbank.cardservice.service;

import com.zbank.cardservice.dto.request.CardActivationRequest;
import com.zbank.cardservice.dto.response.CardActivationResponse;
import com.zbank.cardservice.exception.BusinessException;
import com.zbank.cardservice.model.CreditCard;
import com.zbank.cardservice.model.enums.CardStatus;
import com.zbank.cardservice.model.enums.CardType;
import com.zbank.cardservice.repository.CreditCardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardActivationServiceTest {

    @Mock
    private CreditCardRepository creditCardRepository;

    @InjectMocks
    private CardActivationService cardActivationService;

    @BeforeEach
    void setUp() {
        when(creditCardRepository.existsByCardNumber(anyString())).thenReturn(false);
    }

    @Test
    void activateCard_returnsPlatinum_forScore500() {
        CardActivationRequest request = new CardActivationRequest();
        request.setCustomerId(1L);
        request.setCreditScore(500);

        CreditCard savedCard = CreditCard.builder()
                .id(1L).customerId(1L).cardNumber("1234567890123456")
                .cardType(CardType.PLATINUM).creditLimit(new BigDecimal("40000"))
                .status(CardStatus.ACTIVE).pin("123456").build();
        when(creditCardRepository.save(any(CreditCard.class))).thenReturn(savedCard);

        CardActivationResponse response = cardActivationService.activateCard(request);

        assertThat(response.getCardType()).isEqualTo(CardType.PLATINUM);
        assertThat(response.getCreditLimit()).isEqualByComparingTo(new BigDecimal("40000"));
        assertThat(response.getStatus()).isEqualTo(CardStatus.ACTIVE);
    }

    @Test
    void activateCard_returnsGold_forScore300() {
        CardActivationRequest request = new CardActivationRequest();
        request.setCustomerId(1L);
        request.setCreditScore(300);

        CreditCard savedCard = CreditCard.builder()
                .id(1L).customerId(1L).cardNumber("1234567890123456")
                .cardType(CardType.GOLD).creditLimit(new BigDecimal("20000"))
                .status(CardStatus.ACTIVE).pin("654321").build();
        when(creditCardRepository.save(any(CreditCard.class))).thenReturn(savedCard);

        CardActivationResponse response = cardActivationService.activateCard(request);

        assertThat(response.getCardType()).isEqualTo(CardType.GOLD);
        assertThat(response.getCreditLimit()).isEqualByComparingTo(new BigDecimal("20000"));
    }

    @Test
    void activateCard_returnsVisa_forScore150() {
        CardActivationRequest request = new CardActivationRequest();
        request.setCustomerId(1L);
        request.setCreditScore(150);

        CreditCard savedCard = CreditCard.builder()
                .id(1L).customerId(1L).cardNumber("1234567890123456")
                .cardType(CardType.VISA).creditLimit(new BigDecimal("10000"))
                .status(CardStatus.ACTIVE).pin("111111").build();
        when(creditCardRepository.save(any(CreditCard.class))).thenReturn(savedCard);

        CardActivationResponse response = cardActivationService.activateCard(request);

        assertThat(response.getCardType()).isEqualTo(CardType.VISA);
        assertThat(response.getCreditLimit()).isEqualByComparingTo(new BigDecimal("10000"));
    }

    @Test
    void activateCard_returnsPendingDocs_forScore50() {
        CardActivationRequest request = new CardActivationRequest();
        request.setCustomerId(1L);
        request.setCreditScore(50);

        CreditCard savedCard = CreditCard.builder()
                .id(1L).customerId(1L).cardNumber("1234567890123456")
                .cardType(null).creditLimit(BigDecimal.ZERO)
                .status(CardStatus.PENDING_DOCUMENTS).pin("222222").build();
        when(creditCardRepository.save(any(CreditCard.class))).thenReturn(savedCard);

        CardActivationResponse response = cardActivationService.activateCard(request);

        assertThat(response.getStatus()).isEqualTo(CardStatus.PENDING_DOCUMENTS);
        assertThat(response.getFirstTimePin()).isNull();
    }

    @Test
    void activateCard_throwsException_forInvalidScore() {
        CardActivationRequest request = new CardActivationRequest();
        request.setCustomerId(1L);
        request.setCreditScore(999);

        assertThatThrownBy(() -> cardActivationService.activateCard(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Invalid credit score");
    }

    @Test
    void changePinFirstTime_success() {
        CreditCard card = CreditCard.builder()
                .id(1L).customerId(1L).cardNumber("1234567890123456")
                .cardType(CardType.PLATINUM).creditLimit(new BigDecimal("40000"))
                .status(CardStatus.ACTIVE).pin("123456").pinChanged(false).build();
        when(creditCardRepository.findByCardNumber("1234567890123456")).thenReturn(Optional.of(card));
        when(creditCardRepository.save(any(CreditCard.class))).thenReturn(card);

        CardActivationResponse response = cardActivationService.changePinFirstTime(
                "1234567890123456", "123456", "DOCID001", "654321", "654321");

        assertThat(response.getMessage()).contains("PIN changed successfully");
    }

    @Test
    void changePinFirstTime_throwsException_whenPinMismatch() {
        assertThatThrownBy(() -> cardActivationService.changePinFirstTime(
                "1234567890123456", "123456", "DOCID001", "654321", "111111"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("do not match");
    }

    @Test
    void changePinFirstTime_throwsException_whenPinAlreadyChanged() {
        CreditCard card = CreditCard.builder()
                .id(1L).cardNumber("1234567890123456")
                .status(CardStatus.ACTIVE).pin("123456").pinChanged(true).build();
        when(creditCardRepository.findByCardNumber("1234567890123456")).thenReturn(Optional.of(card));

        assertThatThrownBy(() -> cardActivationService.changePinFirstTime(
                "1234567890123456", "123456", "DOCID001", "654321", "654321"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already been changed");
    }
}
