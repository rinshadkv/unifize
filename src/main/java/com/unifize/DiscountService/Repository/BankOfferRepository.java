package com.unifize.DiscountService.Repository;

import com.unifize.DiscountService.Model.BankOffer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface BankOfferRepository extends JpaRepository< BankOffer,Long> {

    BankOffer findByBankNameAndCardType(String bankName, String cardType);
}
