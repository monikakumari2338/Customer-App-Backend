package com.deepanshu.controller;

import com.deepanshu.request.ExpressDeliveryRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.deepanshu.exception.ProductException;
import com.deepanshu.exception.UserException;
import com.deepanshu.modal.Cart;
import com.deepanshu.modal.User;
import com.deepanshu.request.AddItemRequest;
import com.deepanshu.response.ApiResponse;
import com.deepanshu.service.CartService;
import com.deepanshu.service.UserService;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "https://localhost:8081")
public class CartController {

    private CartService cartService;
    private UserService userService;

    public CartController(CartService cartService, UserService userService) {
        this.cartService = cartService;
        this.userService = userService;
    }

    @GetMapping("/")
    public ResponseEntity<Cart> findUserCartHandler(@RequestHeader("Authorization") String jwt) throws UserException {

        User user = userService.findUserProfileByJwt(jwt);

        Cart cart = cartService.findUserCart(user.getId());

        System.out.println("cart - " + cart.getUser().getEmail());

        return new ResponseEntity<Cart>(cart, HttpStatus.OK);
    }

    @PutMapping("/add")
    public ResponseEntity<ApiResponse> addItemToCart(@RequestBody AddItemRequest req, @RequestHeader("Authorization") String jwt) throws UserException, ProductException {

        User user = userService.findUserProfileByJwt(jwt);

        cartService.addCartItem(user.getId(), req);

        ApiResponse res = new ApiResponse("Item Added To Cart Successfully", true);

        return new ResponseEntity<ApiResponse>(res, HttpStatus.ACCEPTED);

    }

    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse> clearCart(@RequestHeader("Authorization") String jwt) throws UserException {
        User user = userService.findUserProfileByJwt(jwt);

        cartService.clearCart(user.getId());

        ApiResponse response = new ApiResponse("Cart cleared successfully", true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/{userId}/express-delivery")
    public ResponseEntity<String> setExpressDelivery(@PathVariable Long userId, @RequestBody ExpressDeliveryRequest request) {
        cartService.setExpressDelivery(userId, request.isExpressDelivery());
        return ResponseEntity.ok("Express delivery option updated.");
    }
}
