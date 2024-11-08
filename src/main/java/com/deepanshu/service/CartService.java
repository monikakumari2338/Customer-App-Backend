package com.deepanshu.service;

import com.deepanshu.exception.ProductException;
import com.deepanshu.modal.Cart;
import com.deepanshu.modal.User;
import com.deepanshu.request.AddItemRequest;

public interface CartService {

    public Cart createCart(User user);

    public String addCartItem(Long userId, AddItemRequest req) throws ProductException;

    public Cart findUserCart(Long userId);

    void clearCart(Long id);

    void applyDiscountToUserCart(Long userId, int discountPercentage);

    void setExpressDelivery(Long userId, boolean expressDelivery);

}
