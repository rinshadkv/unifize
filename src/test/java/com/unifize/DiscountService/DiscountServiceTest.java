package com.unifize.DiscountService;

import com.unifize.DiscountService.Model.*;
import com.unifize.DiscountService.Repository.*;
import com.unifize.DiscountService.Service.DiscountService;
import com.unifize.DiscountService.VM.DiscountedPriceVM;
import com.unifize.DiscountService.VM.PaymentInfoVM;
import com.unifize.DiscountService.exception.DiscountCalculationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DiscountServiceTest {

    @Mock
    private BrandDiscountRepository brandDiscountRepository;

    @Mock
    private CategoryDiscountRepository categoryDiscountRepository;

    @Mock
    private BankOfferRepository bankOfferRepository;

    @Mock
    private VoucherRepository voucherRepository;

    @Mock
    private CartRepository cartRepository;

    @InjectMocks
    private DiscountService discountService;

    private Customer customer;
    private Brand pumaBrand;
    private Brand nikeBrand;
    private Category tshirtCategory;
    private Category shoesCategory;
    private List<Cart> cartItems;
    private Voucher validVoucher;
    private Voucher expiredVoucher;
    private Voucher brandExclusionVoucher;
    private Voucher highMinOrderVoucher;
    private BrandDiscount pumaBrandDiscount;
    private CategoryDiscount tshirtCategoryDiscount;
    private BankOffer iciciBankOffer;

    @BeforeEach
    void setUp() {
        // Set up customer
        customer = new Customer();
        customer.setId(1L);
        customer.setName("Test Customer");
        customer.setEmail("test@example.com");

        // Set up brands
        pumaBrand = new Brand();
        pumaBrand.setId(1L);
        pumaBrand.setName("PUMA");

        nikeBrand = new Brand();
        nikeBrand.setId(2L);
        nikeBrand.setName("NIKE");

        // Set up categories
        tshirtCategory = new Category();
        tshirtCategory.setId(1L);
        tshirtCategory.setName("T-shirts");

        shoesCategory = new Category();
        shoesCategory.setId(2L);
        shoesCategory.setName("Shoes");

        // Set up products
        Product pumaTshirt = new Product();
        pumaTshirt.setId(1L);
        pumaTshirt.setName("PUMA Sports T-shirt");
        pumaTshirt.setBrand(pumaBrand);
        pumaTshirt.setCategory(tshirtCategory);
        pumaTshirt.setPrice(new BigDecimal("999"));

        Product nikeShoes = new Product();
        nikeShoes.setId(2L);
        nikeShoes.setName("NIKE Running Shoes");
        nikeShoes.setBrand(nikeBrand);
        nikeShoes.setCategory(shoesCategory);
        nikeShoes.setPrice(new BigDecimal("3999"));

        // Set up cart items
        Cart pumaTshirtCartItem = new Cart();
        pumaTshirtCartItem.setId(1L);
        pumaTshirtCartItem.setProduct(pumaTshirt);
        pumaTshirtCartItem.setQuantity(2);
        pumaTshirtCartItem.setCustomer(customer);

        Cart nikeShoesCartItem = new Cart();
        nikeShoesCartItem.setId(2L);
        nikeShoesCartItem.setProduct(nikeShoes);
        nikeShoesCartItem.setQuantity(1);
        nikeShoesCartItem.setCustomer(customer);

        cartItems = Arrays.asList(pumaTshirtCartItem, nikeShoesCartItem);

        // Set up vouchers
        validVoucher = new Voucher();
        validVoucher.setId(1L);
        validVoucher.setCode("SUMMER50");
        validVoucher.setDiscountPercentage(new BigDecimal("50"));
        validVoucher.setMinOrderValue(new BigDecimal("1000"));
        validVoucher.setExpiryDate(LocalDate.now().plusDays(30));
        Set<Category> applicableCategories = new HashSet<>();
        applicableCategories.add(tshirtCategory);
        applicableCategories.add(shoesCategory);
        validVoucher.setApplicableCategories(applicableCategories);

        expiredVoucher = new Voucher();
        expiredVoucher.setId(2L);
        expiredVoucher.setCode("EXPIRED20");
        expiredVoucher.setDiscountPercentage(new BigDecimal("20"));
        expiredVoucher.setMinOrderValue(new BigDecimal("500"));
        expiredVoucher.setExpiryDate(LocalDate.now().minusDays(10));
        expiredVoucher.setApplicableCategories(applicableCategories);

        brandExclusionVoucher = new Voucher();
        brandExclusionVoucher.setId(3L);
        brandExclusionVoucher.setCode("NONIKE30");
        brandExclusionVoucher.setDiscountPercentage(new BigDecimal("30"));
        brandExclusionVoucher.setMinOrderValue(new BigDecimal("800"));
        brandExclusionVoucher.setExpiryDate(LocalDate.now().plusDays(15));
        brandExclusionVoucher.setApplicableCategories(applicableCategories);
        Set<Brand> excludedBrands = new HashSet<>();
        excludedBrands.add(nikeBrand);
        brandExclusionVoucher.setExcludedBrands(excludedBrands);

        highMinOrderVoucher = new Voucher();
        highMinOrderVoucher.setId(4L);
        highMinOrderVoucher.setCode("BIG70");
        highMinOrderVoucher.setDiscountPercentage(new BigDecimal("70"));
        highMinOrderVoucher.setMinOrderValue(new BigDecimal("10000"));
        highMinOrderVoucher.setExpiryDate(LocalDate.now().plusDays(5));
        highMinOrderVoucher.setApplicableCategories(applicableCategories);
        
        // Set up discounts
        pumaBrandDiscount = new BrandDiscount();
        pumaBrandDiscount.setId(1L);
        pumaBrandDiscount.setBrand(pumaBrand);
        pumaBrandDiscount.setPercentage(new BigDecimal("40"));
        
        tshirtCategoryDiscount = new CategoryDiscount();
        tshirtCategoryDiscount.setId(1L);
        tshirtCategoryDiscount.setCategory(tshirtCategory);
        tshirtCategoryDiscount.setPercentage(new BigDecimal("10"));
        
        iciciBankOffer = new BankOffer();
        iciciBankOffer.setId(1L);
        iciciBankOffer.setBankName("ICICI");
        iciciBankOffer.setCardType("CREDIT");
        iciciBankOffer.setPercentage(new BigDecimal("10"));
    }

    @Test
    void calculateCartDiscounts_EmptyCart_ThrowsException() {
        List<Cart> emptyCart = Collections.emptyList();

        assertThrows(DiscountCalculationException.class, () ->
                discountService.calculateCartDiscounts(emptyCart, customer, Optional.empty()));
    }

    @Test
    void calculateCartDiscounts_NullCart_ThrowsException() {
        assertThrows(DiscountCalculationException.class, () ->
                discountService.calculateCartDiscounts(null, customer, Optional.empty()));
    }

    @Test
    void calculateCartDiscounts_OnlyBrandDiscount() throws DiscountCalculationException {
        // Given
        when(brandDiscountRepository.findByBrandId(pumaBrand.getId())).thenReturn(pumaBrandDiscount);
        when(brandDiscountRepository.findByBrandId(nikeBrand.getId())).thenReturn(null);
        when(categoryDiscountRepository.findByCategoryId(anyLong())).thenReturn(null);
        when(voucherRepository.findAll()).thenReturn(Collections.emptyList());
        when(cartRepository.save(any(Cart.class))).thenReturn(null);

        DiscountedPriceVM result = discountService.calculateCartDiscounts(cartItems, customer, Optional.empty());

        assertNotNull(result);

        assertEquals(new BigDecimal("5997"), result.getOriginalPrice());

        assertEquals(new BigDecimal("5197.8"), result.getFinalPrice());

        assertEquals(1, result.getAppliedDiscounts().size());
        assertTrue(result.getAppliedDiscounts().containsKey("Brand Discount - " + pumaBrand));

        
        verify(cartRepository, times(2)).save(any(Cart.class));
    }

    @Test
    void calculateCartDiscounts_BrandAndCategoryDiscounts() throws DiscountCalculationException {
     
        when(brandDiscountRepository.findByBrandId(pumaBrand.getId())).thenReturn(pumaBrandDiscount);
        when(brandDiscountRepository.findByBrandId(nikeBrand.getId())).thenReturn(null);
        when(categoryDiscountRepository.findByCategoryId(tshirtCategory.getId())).thenReturn(tshirtCategoryDiscount);
        when(categoryDiscountRepository.findByCategoryId(shoesCategory.getId())).thenReturn(null);
        when(voucherRepository.findAll()).thenReturn(Collections.emptyList());
        when(cartRepository.save(any(Cart.class))).thenReturn(null);

      
        DiscountedPriceVM result = discountService.calculateCartDiscounts(cartItems, customer, Optional.empty());

      
        assertNotNull(result);

       
        assertEquals(new BigDecimal("5997"), result.getOriginalPrice());

        assertEquals(new BigDecimal("4998.0"), result.getFinalPrice());

        assertEquals(2, result.getAppliedDiscounts().size());
        assertTrue(result.getAppliedDiscounts().containsKey("Brand Discount - " + pumaBrand));
        assertTrue(result.getAppliedDiscounts().containsKey("Category Discount - " + tshirtCategory));
    }

    @Test
    void calculateCartDiscounts_WithValidVoucher() throws DiscountCalculationException {
        
        when(brandDiscountRepository.findByBrandId(pumaBrand.getId())).thenReturn(pumaBrandDiscount);
        when(brandDiscountRepository.findByBrandId(nikeBrand.getId())).thenReturn(null);
        when(categoryDiscountRepository.findByCategoryId(anyLong())).thenReturn(null);
        when(voucherRepository.findAll()).thenReturn(Collections.singletonList(validVoucher));
        when(voucherRepository.findByCode("SUMMER50")).thenReturn(Optional.of(validVoucher));
        when(cartRepository.save(any(Cart.class))).thenReturn(null);

        DiscountedPriceVM result = discountService.calculateCartDiscounts(cartItems, customer, Optional.empty());

        
        assertNotNull(result);

        assertEquals(new BigDecimal("5997"), result.getOriginalPrice());

        assertEquals(new BigDecimal("2199.3"), result.getFinalPrice());

        assertEquals(2, result.getAppliedDiscounts().size());
        assertTrue(result.getAppliedDiscounts().containsKey("Brand Discount - " + pumaBrand));
        assertTrue(result.getAppliedDiscounts().containsKey("Voucher Discount - SUMMER50"));
    }

    @Test
    void calculateCartDiscounts_WithBankOffer() throws DiscountCalculationException {
       
        PaymentInfoVM paymentInfo = new PaymentInfoVM();
        paymentInfo.setBankName("ICICI");
        paymentInfo.setCardType("CREDIT");

        when(brandDiscountRepository.findByBrandId(pumaBrand.getId())).thenReturn(pumaBrandDiscount);
        when(brandDiscountRepository.findByBrandId(nikeBrand.getId())).thenReturn(null);
        when(categoryDiscountRepository.findByCategoryId(anyLong())).thenReturn(null);
        when(voucherRepository.findAll()).thenReturn(Collections.emptyList());
        when(bankOfferRepository.findByBankNameAndCardType("ICICI", "CREDIT")).thenReturn(iciciBankOffer);
        when(cartRepository.save(any(Cart.class))).thenReturn(null);

        
        DiscountedPriceVM result = discountService.calculateCartDiscounts(cartItems, customer, Optional.of(paymentInfo));

        
        assertNotNull(result);

    
        assertEquals(new BigDecimal("5997"), result.getOriginalPrice());

        assertEquals(new BigDecimal("4678.02"), result.getFinalPrice());

        assertEquals(2, result.getAppliedDiscounts().size());
        assertTrue(result.getAppliedDiscounts().containsKey("Brand Discount - " + pumaBrand));
        assertTrue(result.getAppliedDiscounts().containsKey("Bank Offer - ICICI"));
    }

    @Test
    void calculateCartDiscounts_AllDiscountsApplied() throws DiscountCalculationException {
      
        PaymentInfoVM paymentInfo = new PaymentInfoVM();
        paymentInfo.setBankName("ICICI");
        paymentInfo.setCardType("CREDIT");

        when(brandDiscountRepository.findByBrandId(pumaBrand.getId())).thenReturn(pumaBrandDiscount);
        when(brandDiscountRepository.findByBrandId(nikeBrand.getId())).thenReturn(null);
        when(categoryDiscountRepository.findByCategoryId(tshirtCategory.getId())).thenReturn(tshirtCategoryDiscount);
        when(categoryDiscountRepository.findByCategoryId(shoesCategory.getId())).thenReturn(null);
        when(voucherRepository.findAll()).thenReturn(Collections.singletonList(validVoucher));
        when(voucherRepository.findByCode("SUMMER50")).thenReturn(Optional.of(validVoucher));
        when(bankOfferRepository.findByBankNameAndCardType("ICICI", "CREDIT")).thenReturn(iciciBankOffer);
        when(cartRepository.save(any(Cart.class))).thenReturn(null);

        DiscountedPriceVM result = discountService.calculateCartDiscounts(cartItems, customer, Optional.of(paymentInfo));

        assertNotNull(result);

       
        assertEquals(new BigDecimal("5997"), result.getOriginalPrice());

        assertEquals(new BigDecimal("1799.55"), result.getFinalPrice());

        assertEquals(4, result.getAppliedDiscounts().size());
        assertTrue(result.getAppliedDiscounts().containsKey("Brand Discount - " + pumaBrand));
        assertTrue(result.getAppliedDiscounts().containsKey("Category Discount - " + tshirtCategory));
        assertTrue(result.getAppliedDiscounts().containsKey("Voucher Discount - SUMMER50"));
        assertTrue(result.getAppliedDiscounts().containsKey("Bank Offer - ICICI"));
    }

    @Test
    void calculateCartDiscounts_VoucherValidationFails() throws DiscountCalculationException {
       
        when(brandDiscountRepository.findByBrandId(pumaBrand.getId())).thenReturn(pumaBrandDiscount);
        when(brandDiscountRepository.findByBrandId(nikeBrand.getId())).thenReturn(null);
        when(categoryDiscountRepository.findByCategoryId(anyLong())).thenReturn(null);
        when(voucherRepository.findAll()).thenReturn(Collections.singletonList(expiredVoucher));
        when(voucherRepository.findByCode("EXPIRED20")).thenReturn(Optional.of(expiredVoucher));
        when(cartRepository.save(any(Cart.class))).thenReturn(null);

        
        DiscountedPriceVM result = discountService.calculateCartDiscounts(cartItems, customer, Optional.empty());

        assertNotNull(result);

       
        assertEquals(new BigDecimal("5997"), result.getOriginalPrice());

     
        assertEquals(new BigDecimal("5197.8"), result.getFinalPrice());

        assertEquals(1, result.getAppliedDiscounts().size());
        assertTrue(result.getAppliedDiscounts().containsKey("Brand Discount - " + pumaBrand));
        assertFalse(result.getAppliedDiscounts().containsKey("Voucher Discount - EXPIRED20"));
    }

    @Test
    void calculateCartDiscounts_VoucherWithBrandExclusion() throws DiscountCalculationException {
     
        when(brandDiscountRepository.findByBrandId(pumaBrand.getId())).thenReturn(pumaBrandDiscount);
        when(brandDiscountRepository.findByBrandId(nikeBrand.getId())).thenReturn(null);
        when(categoryDiscountRepository.findByCategoryId(anyLong())).thenReturn(null);
        when(voucherRepository.findAll()).thenReturn(Collections.singletonList(brandExclusionVoucher));
        when(voucherRepository.findByCode("NONIKE30")).thenReturn(Optional.of(brandExclusionVoucher));
        when(cartRepository.save(any(Cart.class))).thenReturn(null);

       
        DiscountedPriceVM result = discountService.calculateCartDiscounts(cartItems, customer, Optional.empty());

       
        assertNotNull(result);

       
        assertEquals(1, result.getAppliedDiscounts().size());
        assertTrue(result.getAppliedDiscounts().containsKey("Brand Discount - " + pumaBrand));
        assertFalse(result.getAppliedDiscounts().containsKey("Voucher Discount - NONIKE30"));
    }

    @Test
    void calculateCartDiscounts_VoucherWithHighMinOrder() throws DiscountCalculationException {
        when(brandDiscountRepository.findByBrandId(pumaBrand.getId())).thenReturn(pumaBrandDiscount);
        when(brandDiscountRepository.findByBrandId(nikeBrand.getId())).thenReturn(null);
        when(categoryDiscountRepository.findByCategoryId(anyLong())).thenReturn(null);
        when(voucherRepository.findAll()).thenReturn(Collections.singletonList(highMinOrderVoucher));
        when(voucherRepository.findByCode("BIG70")).thenReturn(Optional.of(highMinOrderVoucher));
        when(cartRepository.save(any(Cart.class))).thenReturn(null);

        DiscountedPriceVM result = discountService.calculateCartDiscounts(cartItems, customer, Optional.empty());

        assertNotNull(result);

       
        assertEquals(1, result.getAppliedDiscounts().size());
        assertTrue(result.getAppliedDiscounts().containsKey("Brand Discount - " + pumaBrand));
        assertFalse(result.getAppliedDiscounts().containsKey("Voucher Discount - BIG70"));
    }

    @Test
    void calculateCartDiscounts_NoDiscountsApplicable() throws DiscountCalculationException {
        
        when(brandDiscountRepository.findByBrandId(anyLong())).thenReturn(null);
        when(categoryDiscountRepository.findByCategoryId(anyLong())).thenReturn(null);
        when(voucherRepository.findAll()).thenReturn(Collections.emptyList());
        when(cartRepository.save(any(Cart.class))).thenReturn(null);

        DiscountedPriceVM result = discountService.calculateCartDiscounts(cartItems, customer, Optional.empty());

        assertNotNull(result);
        assertEquals(new BigDecimal("5997"), result.getOriginalPrice());
        assertEquals(new BigDecimal("5997"), result.getFinalPrice());
        assertTrue(result.getAppliedDiscounts().isEmpty());
    }
}