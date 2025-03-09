package com.unifize.DiscountService.Config;

import com.unifize.DiscountService.Model.*;
import com.unifize.DiscountService.Repository.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Configuration
public class DataInitializer {

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private BankOfferRepository bankOfferRepository;

    @Autowired
    private BrandDiscountRepository brandDiscountRepository;

    @Autowired
    private CategoryDiscountRepository categoryDiscountRepository;

    @Autowired
    private VoucherRepository voucherRepository;

    @Autowired
    private CartRepository cartRepository;

    public DataInitializer(BrandRepository brandRepository) {
        this.brandRepository = brandRepository;
    }

    @PostConstruct
    @Transactional
    public void initializeData() {
        // Initialize brands with different tiers
        Brand puma = new Brand(null, BrandTier.PREMIUM, "PUMA");
        Brand nike = new Brand(null, BrandTier.PREMIUM, "NIKE");
        Brand adidas = new Brand(null, BrandTier.PREMIUM, "ADIDAS");
        Brand levis = new Brand(null, BrandTier.REGULAR, "LEVIS");
        Brand hm = new Brand(null, BrandTier.REGULAR, "H&M");
        Brand zara = new Brand(null, BrandTier.REGULAR, "ZARA");

        List<Brand> brands = brandRepository.saveAll(Arrays.asList(puma, nike, adidas, levis, hm, zara));

        // Initialize categories
        Category tshirts = new Category(null, "T-SHIRTS");
        Category jeans = new Category(null, "JEANS");
        Category shoes = new Category(null, "SHOES");
        Category accessories = new Category(null, "ACCESSORIES");
        Category sportswear = new Category(null, "SPORTSWEAR");

        List<Category> categories = categoryRepository.saveAll(Arrays.asList(tshirts, jeans, shoes, accessories, sportswear));

        // Initialize products
        Product pumaTshirt = new Product(null, "PUMA Sport T-Shirt", puma, tshirts, new BigDecimal("1999.00"));
        Product pumaShoes = new Product(null, "PUMA Running Shoes", puma, shoes, new BigDecimal("4999.00"));
        Product nikeTshirt = new Product(null, "NIKE Dri-FIT T-Shirt", nike, tshirts, new BigDecimal("2499.00"));
        Product adidasShoes = new Product(null, "ADIDAS Ultraboost", adidas, shoes, new BigDecimal("12999.00"));
        Product levisJeans = new Product(null, "LEVIS 501 Original", levis, jeans, new BigDecimal("3499.00"));
        Product hmTshirt = new Product(null, "H&M Basic Tee", hm, tshirts, new BigDecimal("799.00"));
        Product zaraTshirt = new Product(null, "ZARA Printed T-Shirt", zara, tshirts, new BigDecimal("1299.00"));
        Product nikeJacket = new Product(null, "NIKE Windrunner Jacket", nike, sportswear, new BigDecimal("5499.00"));

        List<Product> products = productRepository.saveAll(Arrays.asList(
                pumaTshirt, pumaShoes, nikeTshirt, adidasShoes,
                levisJeans, hmTshirt, zaraTshirt, nikeJacket));

        // Initialize customers with different tiers
        Customer platinumCustomer = new Customer(null, "John Premium", "john@example.com", "PLATINUM", new ArrayList<>());
        Customer goldCustomer = new Customer(null, "Jane Gold", "jane@example.com", "GOLD", new ArrayList<>());
        Customer regularCustomer = new Customer(null, "Bob Regular", "bob@example.com", "REGULAR", new ArrayList<>());

        List<Customer> customers = customerRepository.saveAll(Arrays.asList(platinumCustomer, goldCustomer, regularCustomer));

        // Initialize bank offers (as per requirement)
        BankOffer iciciOffer = new BankOffer(null, "ICICI", "CREDIT", new BigDecimal("10.00"), new BigDecimal("1000.00"));
        BankOffer hdfcOffer = new BankOffer(null, "HDFC", "DEBIT", new BigDecimal("5.00"), new BigDecimal("2000.00"));
        BankOffer sbiOffer = new BankOffer(null, "SBI", "CREDIT", new BigDecimal("7.50"), new BigDecimal("1500.00"));
        BankOffer axisOffer = new BankOffer(null, "AXIS", "CREDIT", new BigDecimal("8.00"), new BigDecimal("2500.00"));

        List<BankOffer> bankOffers = bankOfferRepository.saveAll(Arrays.asList(iciciOffer, hdfcOffer, sbiOffer, axisOffer));

        // Initialize brand discounts (as per requirement - "Min 40% off on PUMA")
        BrandDiscount pumaDiscount = new BrandDiscount(null, puma, new BigDecimal("40.00"));
        BrandDiscount nikeDiscount = new BrandDiscount(null, nike, new BigDecimal("30.00"));
        BrandDiscount adidasDiscount = new BrandDiscount(null, adidas, new BigDecimal("25.00"));
        BrandDiscount levisDiscount = new BrandDiscount(null, levis, new BigDecimal("20.00"));
        BrandDiscount hmDiscount = new BrandDiscount(null, hm, new BigDecimal("15.00"));

        List<BrandDiscount> brandDiscounts = brandDiscountRepository.saveAll(Arrays.asList(
                pumaDiscount, nikeDiscount, adidasDiscount, levisDiscount, hmDiscount));

        // Initialize category discounts (as per requirement - "Extra 10% off on T-shirts")
        CategoryDiscount tshirtsDiscount = new CategoryDiscount(null, tshirts, new BigDecimal("10.00"));
        CategoryDiscount shoesDiscount = new CategoryDiscount(null, shoes, new BigDecimal("5.00"));
        CategoryDiscount sportswearDiscount = new CategoryDiscount(null, sportswear, new BigDecimal("15.00"));

        List<CategoryDiscount> categoryDiscounts = categoryDiscountRepository.saveAll(Arrays.asList(
                tshirtsDiscount, shoesDiscount, sportswearDiscount));

        // Initialize vouchers (as per requirement - "SUPER69" for 69% off)
        Set<String> allTiers = new HashSet<>(Arrays.asList("PLATINUM", "GOLD", "REGULAR"));
        Set<String> premiumTiers = new HashSet<>(Arrays.asList("PLATINUM", "GOLD"));

        Voucher super69 = new Voucher(
                null,
                "SUPER69",
                new BigDecimal("69.00"),
                new BigDecimal("1999.00"),
                allTiers,
                new HashSet<>(), // No excluded brands
                new HashSet<>(), // Applicable to all categories
                LocalDate.now().plusMonths(1),
                LocalDate.now().plusMonths(1),
                true
        );

        Voucher summer30 = new Voucher(
                null,
                "SUMMER30",
                new BigDecimal("30.00"),
                new BigDecimal("2999.00"),
                allTiers,
                new HashSet<>(Collections.singletonList(puma)), // Exclude PUMA
                new HashSet<>(Arrays.asList(tshirts, sportswear)), // Only for t-shirts and sportswear
                LocalDate.now().plusMonths(3),
                LocalDate.now().plusMonths(3),
                true
        );

        Voucher premium20 = new Voucher(
                null,
                "PREMIUM20",
                new BigDecimal("20.00"),
                new BigDecimal("5000.00"),
                premiumTiers, // Only for PLATINUM and GOLD customers
                new HashSet<>(),
                new HashSet<>(),
                LocalDate.now().plusMonths(2),
                LocalDate.now().plusMonths(2),
                true
        );

        List<Voucher> vouchers = voucherRepository.saveAll(Arrays.asList(super69, summer30, premium20));

        // Initialize cart items for testing the specific scenario mentioned in requirements
        // - PUMA T-shirt with "Min 40% off"
        // - Additional 10% off on T-shirts category
        // - ICICI bank offer of 10% instant discount

        // John's cart - Platinum customer with PUMA T-shirt & Nike Jacket
        Cart johnCart1 = new Cart(null, platinumCustomer, pumaTshirt, 2, pumaTshirt.getPrice(), pumaTshirt.getPrice());
        Cart johnCart2 = new Cart(null, platinumCustomer, nikeJacket, 1, nikeJacket.getPrice(), nikeJacket.getPrice());

        // Jane's cart - Gold customer with NIKE T-shirt & ADIDAS Shoes
        Cart janeCart1 = new Cart(null, goldCustomer, nikeTshirt, 1, nikeTshirt.getPrice(), nikeTshirt.getPrice());
        Cart janeCart2 = new Cart(null, goldCustomer, adidasShoes, 1, adidasShoes.getPrice(), adidasShoes.getPrice());

        // Bob's cart - Regular customer with H&M T-shirt & LEVIS Jeans
        Cart bobCart1 = new Cart(null, regularCustomer, hmTshirt, 3, hmTshirt.getPrice(), hmTshirt.getPrice());
        Cart bobCart2 = new Cart(null, regularCustomer, levisJeans, 1, levisJeans.getPrice(), levisJeans.getPrice());

        List<Cart> cartItems = cartRepository.saveAll(Arrays.asList(
                johnCart1, johnCart2, janeCart1, janeCart2, bobCart1, bobCart2));

        System.out.println("E-commerce fashion database initialized with test data!");
        System.out.println("Initialized scenario for testing:");
        System.out.println("- PUMA T-shirt with 40% brand discount");
        System.out.println("- Additional 10% off on T-shirts category");
        System.out.println("- ICICI bank offer of 10% instant discount");
    }
}