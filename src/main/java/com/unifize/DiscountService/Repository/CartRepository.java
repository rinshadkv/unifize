package com.unifize.DiscountService.Repository;

import com.unifize.DiscountService.Model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CartRepository extends JpaRepository< Cart,Long> {
    List<Cart> findAllByCustomerId(Long customerId);
}
