package com.unifize.DiscountService.VM;

import lombok.Builder;
import lombok.Data;

@Data
public class PaymentInfoVM {


    private String method; // CARD, UPI, etc
    private String bankName; // Optional
    private String cardType; // Optional: CREDIT, DEBIT


}
