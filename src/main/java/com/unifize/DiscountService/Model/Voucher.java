package com.unifize.DiscountService.Model;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Voucher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    private String code;
    private BigDecimal discountPercentage;
    private BigDecimal minOrderValue;
    private Set<String> eligibleCustomerTiers;
    @OneToMany
    private Set<Brand> excludedBrands;
    @OneToMany
    private Set<Category> applicableCategories;
    private LocalDate expiryDate;
    private LocalDate endDate;
    private boolean active;


}
