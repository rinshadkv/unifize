package com.unifize.DiscountService.Repository;

import com.unifize.DiscountService.Model.CategoryDiscount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface CategoryDiscountRepository extends JpaRepository< CategoryDiscount,Long> {
    CategoryDiscount findByCategoryId(Long categoryId);
}
