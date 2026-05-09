package com.zbank.cardservice.repository;

import com.zbank.cardservice.model.CreditCard;
import com.zbank.cardservice.model.enums.CardStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CreditCardRepository extends JpaRepository<CreditCard, Long> {
    Optional<CreditCard> findByCardNumber(String cardNumber);
    List<CreditCard> findByCustomerId(Long customerId);
    List<CreditCard> findByCustomerIdAndStatus(Long customerId, CardStatus status);
    boolean existsByCardNumber(String cardNumber);
}
