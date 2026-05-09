package com.zbank.cardservice.service;

import com.zbank.cardservice.dto.request.CardActivationRequest;
import com.zbank.cardservice.dto.response.CardActivationResponse;
import com.zbank.cardservice.exception.BusinessException;
import com.zbank.cardservice.exception.ResourceNotFoundException;
import com.zbank.cardservice.model.CreditCard;
import com.zbank.cardservice.model.enums.CardStatus;
import com.zbank.cardservice.model.enums.CardType;
import com.zbank.cardservice.repository.CreditCardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class CardActivationService {

    private final CreditCardRepository creditCardRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    private static final int PLATINUM_SCORE = 500;
    private static final int GOLD_SCORE = 300;
    private static final int VISA_SCORE = 150;
    private static final int DOCS_REQUIRED_SCORE = 50;

    public CardActivationResponse activateCard(CardActivationRequest request) {
        int score = request.getCreditScore();

        if (score == PLATINUM_SCORE) {
            return createCard(request.getCustomerId(), CardType.PLATINUM, new BigDecimal("40000"), CardStatus.ACTIVE, score);
        } else if (score == GOLD_SCORE) {
            return createCard(request.getCustomerId(), CardType.GOLD, new BigDecimal("20000"), CardStatus.ACTIVE, score);
        } else if (score == VISA_SCORE) {
            return createCard(request.getCustomerId(), CardType.VISA, new BigDecimal("10000"), CardStatus.ACTIVE, score);
        } else if (score == DOCS_REQUIRED_SCORE) {
            return createCard(request.getCustomerId(), null, BigDecimal.ZERO, CardStatus.PENDING_DOCUMENTS, score);
        } else {
            throw new BusinessException("Invalid credit score: " + score + ". Cannot determine card type.");
        }
    }

    public CardActivationResponse changePinFirstTime(String cardNumber, String firstTimePin, String documentId, String newPin, String confirmNewPin) {
        if (!newPin.equals(confirmNewPin)) {
            throw new BusinessException("New PIN and confirm PIN do not match");
        }

        CreditCard card = creditCardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found with number: " + cardNumber));

        if (card.isPinChanged()) {
            throw new BusinessException("PIN has already been changed for this card");
        }

        if (!card.getPin().equals(firstTimePin)) {
            throw new BusinessException("Invalid first time PIN");
        }

        if (card.getStatus() != CardStatus.ACTIVE) {
            throw new BusinessException("Card is not in active status. Current status: " + card.getStatus());
        }

        card.setPin(newPin);
        card.setPinChanged(true);
        creditCardRepository.save(card);

        return CardActivationResponse.builder()
                .cardId(card.getId())
                .customerId(card.getCustomerId())
                .cardNumber(card.getCardNumber())
                .cardType(card.getCardType())
                .creditLimit(card.getCreditLimit())
                .status(card.getStatus())
                .message("PIN changed successfully. First audit: PIN_GENERATED")
                .build();
    }

    private CardActivationResponse createCard(Long customerId, CardType cardType, BigDecimal limit, CardStatus status, int score) {
        String cardNumber = generateUniqueCardNumber();
        String firstTimePin = generatePin();
        String message = status == CardStatus.PENDING_DOCUMENTS
                ? "Credit score is " + score + ". Please submit additional documents for card approval."
                : "Card approved. Card type: " + cardType + " with limit $" + limit;

        CreditCard card = CreditCard.builder()
                .cardNumber(cardNumber)
                .customerId(customerId)
                .cardType(cardType)
                .creditLimit(limit)
                .pin(firstTimePin)
                .status(status)
                .build();

        CreditCard saved = creditCardRepository.save(card);

        return CardActivationResponse.builder()
                .cardId(saved.getId())
                .customerId(saved.getCustomerId())
                .cardNumber(saved.getCardNumber())
                .cardType(saved.getCardType())
                .creditLimit(saved.getCreditLimit())
                .status(saved.getStatus())
                .firstTimePin(status == CardStatus.ACTIVE ? firstTimePin : null)
                .message(message)
                .build();
    }

    private String generateUniqueCardNumber() {
        String cardNumber;
        do {
            long number = (long) (secureRandom.nextDouble() * 9_000_000_000_000_000L) + 1_000_000_000_000_000L;
            cardNumber = String.valueOf(number);
        } while (creditCardRepository.existsByCardNumber(cardNumber));
        return cardNumber;
    }

    public java.util.List<com.zbank.cardservice.model.CreditCard> getCardsByCustomerId(Long customerId) {
        return creditCardRepository.findByCustomerId(customerId);
    }

    private String generatePin() {
        int pin = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(pin);
    }
}
