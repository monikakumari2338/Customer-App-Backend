package com.deepanshu.service;

import com.deepanshu.exception.CartItemException;
import com.deepanshu.exception.UserException;
import com.deepanshu.modal.Cart;
import com.deepanshu.modal.CartItem;
import com.deepanshu.modal.Product;

import java.util.List;

public interface CartItemService {

    public CartItem createCartItem(CartItem cartItem);

    public CartItem updateCartItem(Long userId, Long id, CartItem cartItem) throws CartItemException, UserException;

    public CartItem isCartItemExist(Cart cart, Product product, String size, Long userId);

    public void removeCartItem(Long userId, Long cartItemId) throws CartItemException, UserException;

    public CartItem findCartItemById(Long cartItemId) throws CartItemException;

    void updateDiscountedPriceInCartItems(Long userId, int pointsToDeduct);

    void updateCartValue(Long userId, int revertedPoints);
    int getDiscountedPrice(Long userId);
}