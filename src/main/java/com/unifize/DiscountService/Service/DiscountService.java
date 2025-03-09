package com.unifize.DiscountService.Service;

import com.unifize.DiscountService.Model.BrandDiscount;
import com.unifize.DiscountService.Model.Cart;
import com.unifize.DiscountService.Model.CategoryDiscount;
import com.unifize.DiscountService.Model.Customer;
import com.unifize.DiscountService.Model.Product;
import com.unifize.DiscountService.Model.Voucher;
import com.unifize.DiscountService.Repository.*;
import com.unifize.DiscountService.VM.DiscountedPriceVM;
import com.unifize.DiscountService.VM.PaymentInfoVM;
import com.unifize.DiscountService.exception.DiscountCalculationException;
import com.unifize.DiscountService.exception.DiscountValidationException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class DiscountService {

    private final BrandDiscountRepository brandDiscountRepository;
    private final CategoryDiscountRepository categoryDiscountRepository;
    private final BankOfferRepository bankOfferRepository;
    private final VoucherRepository voucherRepository;
    private final CartRepository cartRepository;

    public DiscountService(
            BrandDiscountRepository brandDiscountRepository,
            CategoryDiscountRepository categoryDiscountRepository,
            BankOfferRepository bankOfferRepository,
            VoucherRepository voucherRepository,
            CartRepository cartRepository) {
        this.brandDiscountRepository = brandDiscountRepository;
        this.categoryDiscountRepository = categoryDiscountRepository;
        this.bankOfferRepository = bankOfferRepository;
        this.voucherRepository = voucherRepository;
        this.cartRepository = cartRepository;
    }

    private static BigDecimal findTotalPrice(List<Cart> cartItems) {
        return cartItems.stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private boolean validateDiscountCode(String code, List<Cart> cartItems, Customer customer)
            throws DiscountValidationException {
        try {

            Voucher voucher = voucherRepository.findByCode(code)
                    .orElseThrow(() -> new DiscountValidationException("Invalid voucher code: " + code));

            if (voucher.getExpiryDate().isBefore(LocalDate.now())) {
                return false;
            }

            boolean validForCart = cartItems.stream()
                    .anyMatch(item -> voucher.getApplicableCategories().contains(item.getProduct().getCategory()));

            BigDecimal totalCartValue = findTotalPrice(cartItems);
            if (totalCartValue.compareTo(voucher.getMinOrderValue()) < 0) {
                return false;
            }
            if (voucher.getExcludedBrands() != null && !voucher.getExcludedBrands().isEmpty()) {
                boolean containsExcludedBrand = cartItems.stream()
                        .anyMatch(item -> voucher.getExcludedBrands().contains(item.getProduct().getBrand()));
                if (containsExcludedBrand) {
                    return false;
                }
            }

            if (!validForCart) {
                return false;
            }

        } catch (Exception e) {
            throw new DiscountValidationException("Error in validating Discount", e);
        }
        return true;
    }

    public DiscountedPriceVM calculateCartDiscounts(List<Cart> cartItems, Customer customer,
            Optional<PaymentInfoVM> paymentInfo)
            throws DiscountCalculationException {
        try {

            if (cartItems == null || cartItems.isEmpty()) {
                throw new DiscountCalculationException("Cart items cannot be empty");
            }
            BigDecimal originalPrice = findTotalPrice(cartItems);

            BigDecimal finalPrice = originalPrice;
            Map<String, BigDecimal> appliedDiscounts = new HashMap<>();

            // Apply Brand & Category Discounts
            for (Cart item : cartItems) {
                Product product = item.getProduct();
                BigDecimal discount = BigDecimal.ZERO;

                discount = findBrandDiscount(product, appliedDiscounts, discount);

                discount = findCategoryDiscount(product, appliedDiscounts, discount);

                System.out.println("category" + discount);
                BigDecimal currentPrice = product.getPrice()
                        .subtract(discount.multiply(BigDecimal.valueOf(item.getQuantity())));
                item.setCurrentPrice(currentPrice);

                finalPrice = finalPrice.subtract(discount.multiply(BigDecimal.valueOf(item.getQuantity())));
                cartRepository.save(item);
            }
            // Apply Voucher Discounts
            finalPrice = applyVouchers(cartItems, customer, originalPrice, appliedDiscounts, finalPrice);


            // Apply Bank Offers

            if (paymentInfo.isPresent()) {

                finalPrice = applyBankOffers(paymentInfo.get(), finalPrice, appliedDiscounts);
            }

            System.out.println(finalPrice);

            return DiscountedPriceVM.builder()
                    .originalPrice(originalPrice)
                    .finalPrice(finalPrice.max(BigDecimal.ZERO))
                    .appliedDiscounts(appliedDiscounts)
                    .message("Discounts applied successfully.")
                    .build();
        } catch (Exception e) {
            throw new DiscountCalculationException("Failed to calculate discounts", e);
        }
    }

    private BigDecimal applyBankOffers(PaymentInfoVM paymentInfo, BigDecimal finalPrice,
            Map<String, BigDecimal> appliedDiscounts) {

        BigDecimal bankDiscount = getBankOffer(paymentInfo);
        if (bankDiscount != null) {
            BigDecimal bankDiscountAmount = finalPrice.multiply((bankDiscount.divide(new BigDecimal(100))));
            String bank = paymentInfo.getBankName();
            appliedDiscounts.put("Bank Offer - " + bank, bankDiscountAmount);
            finalPrice = finalPrice.subtract(bankDiscountAmount);
        }
        return finalPrice;
    }

    private BigDecimal applyVouchers(List<Cart> cartItems, Customer customer, BigDecimal originalPrice,
            Map<String, BigDecimal> appliedDiscounts, BigDecimal finalPrice) {
        List<Voucher> vouchers = getAvailableVouchers();
        for (Voucher voucher : vouchers) {
            if (validateDiscountCode(voucher.getCode(), cartItems, customer)) {
                BigDecimal voucherDiscount = originalPrice
                        .multiply((voucher.getDiscountPercentage().divide(new BigDecimal(100))));
                appliedDiscounts.put("Voucher Discount - " + voucher.getCode(), voucherDiscount);
                finalPrice = finalPrice.subtract(voucherDiscount);
            }
        }
        return finalPrice;
    }

    private BigDecimal findCategoryDiscount(Product product, Map<String, BigDecimal> appliedDiscounts,
            BigDecimal discount) {
        Optional<BigDecimal> categoryDiscount = getCategoryDiscount(product.getCategory().getId());
        if (categoryDiscount.isPresent()) {
            BigDecimal categoryDiscountAmount = product.getPrice()
                    .multiply((categoryDiscount.get().divide(new BigDecimal(100))));
            appliedDiscounts.put("Category Discount - " + product.getCategory(), categoryDiscountAmount);
            discount = discount.add(categoryDiscountAmount);
        }
        return discount;
    }

    private BigDecimal findBrandDiscount(Product product, Map<String, BigDecimal> appliedDiscounts,
            BigDecimal discount) {
        Optional<BigDecimal> brandDiscount = getBrandDiscount(product.getBrand().getId());
        if (brandDiscount.isPresent()) {
            BigDecimal brandDiscountAmount = product.getPrice().multiply((brandDiscount.get().divide(new BigDecimal(100))));
            appliedDiscounts.put("Brand Discount - " + product.getBrand(), brandDiscountAmount);
            discount = discount.add(brandDiscountAmount);
        }
        return discount;
    }

    private Optional<BigDecimal> getBrandDiscount(Long brandId) {
        BrandDiscount discount = brandDiscountRepository.findByBrandId(brandId);
        return Optional.ofNullable(discount).map(BrandDiscount::getPercentage);
    }

    private Optional<BigDecimal> getCategoryDiscount(Long categoryId) {
    CategoryDiscount discount = categoryDiscountRepository.findByCategoryId(categoryId);
    return Optional.ofNullable(discount).map(CategoryDiscount::getPercentage);
}

    private BigDecimal getBankOffer(PaymentInfoVM paymentInfo) {
        return bankOfferRepository.findByBankNameAndCardType(paymentInfo.getBankName(), paymentInfo.getCardType())
                .getPercentage();
    }

    private List<Voucher> getAvailableVouchers() {
        return voucherRepository.findAll();
    }

    
}
