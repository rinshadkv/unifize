package com.unifize.DiscountService.Controller;


import com.unifize.DiscountService.Model.Cart;
import com.unifize.DiscountService.Model.Customer;
import com.unifize.DiscountService.Repository.CartRepository;
import com.unifize.DiscountService.Repository.CustomerRepository;
import com.unifize.DiscountService.Repository.VoucherRepository;
import com.unifize.DiscountService.Service.DiscountService;
import com.unifize.DiscountService.VM.DiscountedPriceVM;
import com.unifize.DiscountService.VM.PaymentInfoVM;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/customer")
public class DiscountController {

    private final CartRepository cartRepository;
    private final CustomerRepository customerRepository;
    private final DiscountService discountService;
    private final VoucherRepository voucherRepository;

    public DiscountController(CartRepository cartRepository,
                              CustomerRepository customerRepository,
                              DiscountService discountService,
                              VoucherRepository voucherRepository) {
        this.cartRepository = cartRepository;
        this.customerRepository = customerRepository;
        this.discountService = discountService;

        this.voucherRepository = voucherRepository;
    }


    @PostMapping("/{customerId}/checkout")
    public DiscountedPriceVM calculateDiscounts(@PathVariable Long customerId, @RequestBody(required = false) PaymentInfoVM paymentInfoVM) {

        List<Cart> items = cartRepository.findAllByCustomerId(customerId);
        Optional<Customer> customer = Optional.ofNullable(customerRepository.findById(customerId).orElseThrow(() -> new EntityNotFoundException("Customer not found")));

        return discountService.calculateCartDiscounts(items, customer.get(), Optional.ofNullable(paymentInfoVM));

    }


}
