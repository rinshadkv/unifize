package com.unifize.DiscountService.Repository;

import com.unifize.DiscountService.Model.BrandDiscount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface BrandDiscountRepository extends JpaRepository< BrandDiscount,Long> {
    BrandDiscount findByBrandId(Long brandId);
}
