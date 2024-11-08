package com.deepanshu.service;

import com.deepanshu.modal.*;
import org.springframework.stereotype.Service;

import com.deepanshu.exception.ProductException;
import com.deepanshu.repository.CartRepository;
import com.deepanshu.request.AddItemRequest;

@Service
public class CartServiceImplementation implements CartService {

    private CartRepository cartRepository;
    private CartItemService cartItemService;
    private ProductService productService;

    private UserService userService;


    public CartServiceImplementation(CartRepository cartRepository, CartItemService cartItemService,
                                     ProductService productService, UserService userService) {
        this.cartRepository = cartRepository;
        this.productService = productService;
        this.cartItemService = cartItemService;
        this.userService = userService;
    }

    @Override
    public Cart createCart(User user) {

        Cart cart = new Cart();
        cart.setUser(user);
        Cart createdCart = cartRepository.save(cart);
        return createdCart;
    }

    @Override
    public void setExpressDelivery(Long userId, boolean expressDelivery) {     //express delivery
        Cart cart = cartRepository.findByUserId(userId);
        if (cart != null) {
            cart.setExpressDelivery(expressDelivery);
            cartRepository.save(cart);
        } else {
            throw new IllegalArgumentException("Cart not found for user ID: " + userId);
        }
    }

    public Cart findUserCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId);
        int totalPrice = 0;
        int totalDiscountedPrice = 0;
        int totalItem = 0;
        for (CartItem cartsItem : cart.getCartItems()) {
            totalPrice += cartsItem.getPrice();
            totalDiscountedPrice += cartsItem.getDiscountedPrice();
            totalItem += cartsItem.getQuantity();
        }

        cart.setTotalPrice(totalPrice);
        cart.setTotalItem(cart.getCartItems().size());
        cart.setTotalDiscountedPrice(totalDiscountedPrice + (cart.isExpressDelivery()?100:0));
        cart.setDiscounte(totalPrice - totalDiscountedPrice);

        return cartRepository.save(cart);

    }

    @Override
    public String addCartItem(Long userId, AddItemRequest req) throws ProductException {
        Cart cart = cartRepository.findByUserId(userId);
        Product product = productService.findProductById(req.getProductId());

        CartItem isPresent = cartItemService.isCartItemExist(cart, product, req.getSize(), userId);

        if (isPresent == null) {
            CartItem cartItem = new CartItem();
            cartItem.setProduct(product);
            cartItem.setCart(cart);
            cartItem.setQuantity(req.getQuantity());
            cartItem.setUserId(userId);
            cartItem.setCategory(req.getCategory());
            cartItem.setColor(req.getColor());

            int price = req.getQuantity() * product.getDiscountedPrice();
            cartItem.setPrice(price);
            cartItem.setSize(req.getSize());

            CartItem createdCartItem = cartItemService.createCartItem(cartItem);
            cart.getCartItems().add(createdCartItem);
        }


        return "Item Added To Cart";
    }

    public void clearCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId);

        if (cart != null) {
            User user = cart.getUser();
            if (user != null) {
                user.setDiscountApplied(false);
                userService.saveUser(user);
            }
            cart.getCartItems().clear();
            cart.setExpressDelivery(false);
            cartRepository.save(cart);
        }
    }

    @Override
    public void applyDiscountToUserCart(Long userId, int discountPercentage) {
        User user = userService.getUserById(userId);
        if (user != null && !user.isDiscountApplied()) {
            Cart cart = cartRepository.findByUserId(userId);
            if (cart != null) {
                int totalCartValue = calculateTotalCartValue(cart);
                Tier tier = determineTier(totalCartValue);
                if (tier != null) {
                    int threshold = determineThreshold(tier);
                    if (totalCartValue >= threshold) {
                        applyDiscounts(cart, discountPercentage);
                        user.setDiscountApplied(true);
                        userService.saveUser(user);
                    }
                }
            }
        }
    }

    private Tier determineTier(int totalCartValue) {
        if (totalCartValue >= 5000) {
            return Tier.PLATINUM;
        } else if (totalCartValue >= 3000) {
            return Tier.GOLD;
        } else if (totalCartValue >= 1000) {
            return Tier.SILVER;
        } else {
            return null;
        }
    }

    private int determineThreshold(Tier tier) {
        switch (tier) {
            case SILVER:
                return 1000;
            case GOLD:
                return 3000;
            case PLATINUM:
                return 5000;
            default:
                return 0;
        }
    }


    private int calculateTotalCartValue(Cart cart) {
        int totalCartValue = 0;
        for (CartItem cartItem : cart.getCartItems()) {
            totalCartValue += cartItem.getDiscountedPrice();
        }
        return totalCartValue;
    }

    private void applyDiscounts(Cart cart, int discountPercentage) {
        for (CartItem cartItem : cart.getCartItems()) {
            int discountedPrice = (int) (cartItem.getDiscountedPrice() * (1 - (discountPercentage / 100.0)));
            cartItem.setDiscountedPrice(discountedPrice);
        }
        cartRepository.save(cart);
    }

}

