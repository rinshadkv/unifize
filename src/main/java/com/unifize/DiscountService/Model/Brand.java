package com.unifize.DiscountService.Model;

import com.unifize.DiscountService.Config.BrandTier;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Brand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    private BrandTier brandTier;

    private String name;

}
