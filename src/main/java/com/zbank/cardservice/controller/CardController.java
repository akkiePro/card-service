package com.zbank.cardservice.controller;

import com.zbank.cardservice.dto.request.CardActivationRequest;
import com.zbank.cardservice.dto.request.CreditScoreRequest;
import com.zbank.cardservice.dto.response.ApiResponse;
import com.zbank.cardservice.dto.response.CardActivationResponse;
import com.zbank.cardservice.dto.response.CreditScoreResponse;
import com.zbank.cardservice.service.CardActivationService;
import com.zbank.cardservice.service.CreditRatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {

    private final CreditRatingService creditRatingService;
    private final CardActivationService cardActivationService;

    @PostMapping("/credit-score")
    public ResponseEntity<ApiResponse<CreditScoreResponse>> calculateCreditScore(
            @Valid @RequestBody CreditScoreRequest request) {
        CreditScoreResponse response = creditRatingService.calculateCreditScore(request);
        return ResponseEntity.ok(ApiResponse.success("Credit score calculated successfully", response));
    }

    @PostMapping("/activate")
    public ResponseEntity<ApiResponse<CardActivationResponse>> activateCard(
            @Valid @RequestBody CardActivationRequest request) {
        CardActivationResponse response = cardActivationService.activateCard(request);
        HttpStatus status = response.getStatus().name().equals("ACTIVE")
                ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status)
                .body(ApiResponse.success(response.getMessage(), response));
    }

    @PostMapping("/change-pin")
    public ResponseEntity<ApiResponse<CardActivationResponse>> changePin(
            @RequestBody Map<String, String> request) {
        CardActivationResponse response = cardActivationService.changePinFirstTime(
                request.get("cardNumber"),
                request.get("firstTimePin"),
                request.get("documentId"),
                request.get("newPin"),
                request.get("confirmNewPin")
        );
        return ResponseEntity.ok(ApiResponse.success(response.getMessage(), response));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<java.util.List<com.zbank.cardservice.model.CreditCard>>> getCardsByCustomer(
            @PathVariable Long customerId) {
        var cards = cardActivationService.getCardsByCustomerId(customerId);
        return ResponseEntity.ok(ApiResponse.success("Cards retrieved successfully", cards));
    }
}
