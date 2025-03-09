package com.unifize.DiscountService.Model;


import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    private  String name;

    private String email;

    private String tier;

    @OneToMany(mappedBy = "customer")
    @ToString.Exclude
    private List<Cart> cartItems;

}
