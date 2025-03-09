package com.unifize.DiscountService.VM;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
public class DiscountedPriceVM {


    private BigDecimal originalPrice;
    private BigDecimal finalPrice;
    private Map<String, BigDecimal> appliedDiscounts;
    private String message;
}
