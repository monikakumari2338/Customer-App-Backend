package com.deepanshu.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.deepanshu.modal.*;
import com.deepanshu.repository.*;
import com.deepanshu.request.CancelItemRequest;
import com.deepanshu.request.ExchangeItemRequest;
import com.deepanshu.request.ReturnItemRequest;
import com.deepanshu.user.domain.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.deepanshu.exception.OrderException;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class OrderServiceImplementation implements OrderService {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private CartService cartService;
    @Autowired
    private WishlistService wishlistService;
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OrderItemService orderItemService;
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private RewardService rewardService;
    @Autowired
    private SubscriptionService subscriptionService;
    @Autowired
    private StorePickupService storePickupService;
    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private ProductService productService;
    @Autowired
    private TransactionRepository transactionRepository;

    public OrderServiceImplementation(OrderRepository orderRepository, CartService cartService, WishlistService wishlistService, AddressRepository addressRepository, UserRepository userRepository, OrderItemService orderItemService, OrderItemRepository orderItemRepository, RewardService rewardService, SubscriptionService subscriptionService, StorePickupService storePickupService, ProductService productService,TransactionRepository transactionRepository) {
        this.orderRepository = orderRepository;
        this.cartService = cartService;
        this.wishlistService = wishlistService;
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
        this.orderItemService = orderItemService;
        this.orderItemRepository = orderItemRepository;
        this.rewardService = rewardService;
        this.subscriptionService = subscriptionService;
        this.storePickupService = storePickupService;
        this.productService = productService;
        this.transactionRepository=transactionRepository;
    }

    @Override
    public Order createOrder(User user, Address shippAddress, StorePickup storePickup) {
        Subscription subscription = subscriptionService.getActiveSubscriptionForUser(user);
        shippAddress.setUser(user);
        Address address = addressRepository.save(shippAddress);
        user.getAddresses().add(address);
        userRepository.save(user);

        int redeemedPoints = 0;
        Cart cart = cartService.findUserCart(user.getId());
        if (cart != null && !cart.getCartItems().isEmpty()) {
            redeemedPoints = rewardService.calculateRedeemedPointsagain(user);
        }
        Wishlist wishlist = wishlistService.findUserWishlist(user.getId());
        List<OrderItem> orderItems = new ArrayList<>();


        for (CartItem item : cart.getCartItems()) {
            OrderItem orderItem = new OrderItem();

            orderItem.setPrice(item.getPrice());
            orderItem.setProduct(item.getProduct());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setSize(item.getSize());
            orderItem.setUserId(item.getUserId());
            orderItem.setDiscountedPrice(item.getDiscountedPrice());

            // Decrease inventory
            Product product = item.getProduct();
            product.decreaseInventory(item.getSize(), item.getQuantity());
            productService.save(product);  // Save the product to update the inventory in the database


            OrderItem createdOrderItem = orderItemRepository.save(orderItem);
            orderItems.add(createdOrderItem);
        }

        Order createdOrder = new Order();
        createdOrder.setUser(user);
        createdOrder.setOrderItems(orderItems);
        createdOrder.setTotalPrice(cart.getTotalPrice());
        createdOrder.setTotalDiscountedPrice(cart.getTotalDiscountedPrice());
        createdOrder.setDiscounte(cart.getDiscounte());
        createdOrder.setTotalItem(cart.getTotalItem());
        createdOrder.setShippingAddress(address);
        createdOrder.setOrderDate(LocalDateTime.now());
        createdOrder.setOrderStatus(OrderStatus.PENDING);
        createdOrder.getPaymentDetails().setStatus(PaymentStatus.PENDING);
        createdOrder.setCreatedAt(LocalDateTime.now());
        createdOrder.setSubscription(subscription);
        createdOrder.setStorePickup(storePickup);  //store pickup
        if (redeemedPoints > 0) {
            createdOrder.setRedeemedPoints(redeemedPoints);
        } else {
            createdOrder.setRedeemedPoints(0);
        }
        Order savedOrder = orderRepository.save(createdOrder);
        for (OrderItem item : orderItems) {
            item.setOrder(savedOrder);
            orderItemRepository.save(item);
        }
        return savedOrder;

    }

    @Override
    public Order placedOrder(Long orderId) throws OrderException {
        Order order = findOrderById(orderId);
        order.setOrderStatus(OrderStatus.PLACED);
        order.getPaymentDetails().setStatus(PaymentStatus.COMPLETED);
        return order;
    }

    @Transactional
    @Override
    public Order confirmedOrder(Long orderId) throws OrderException {
        Order order = findOrderById(orderId);
        order.setOrderStatus(OrderStatus.CONFIRMED);
        return orderRepository.save(order);
    }

    @Override
    public Order shippedOrder(Long orderId) throws OrderException {
        Order order = findOrderById(orderId);
        order.setOrderStatus(OrderStatus.SHIPPED);
        return orderRepository.save(order);
    }

    @Override
    public Order deliveredOrder(Long orderId) throws OrderException {
        Order order = findOrderById(orderId);
        order.setOrderStatus(OrderStatus.DELIVERED);
        int pointsEarned = (int) (order.getTotalDiscountedPrice() * 0.05); // Redemption points to be 5% of the total order value Reviewed by Pradeep Jain
        pointsEarned = Math.min(pointsEarned, 100); //Maximum points on an order to be 100 Reviewed by Pradeep Jain
        rewardService.earnReward(order.getUser(), pointsEarned);

        return orderRepository.save(order);
    }

    @Override
    public Order cancledOrder(Long orderId, List<CancelItemRequest> cancelItemRequests,CancellationReason cancellationReason, String comments) throws OrderException {
        Order order = findOrderById(orderId);
        List<OrderItem> itemsToCancel = new ArrayList<>();
        int totalRefundPoints = 0;

        for (CancelItemRequest cancelItemRequest : cancelItemRequests) {
            Long orderItemId = cancelItemRequest.getOrderItemId();

            Optional<OrderItem> orderItemOpt = order.getOrderItems().stream().filter(item -> item.getId().equals(orderItemId)).findFirst();

            if (orderItemOpt.isPresent()) {
                OrderItem orderItemToCancel = orderItemOpt.get();
                handleCancelOrderItem(order, orderItemToCancel);
                itemsToCancel.add(orderItemToCancel);

                double itemDiscountedPrice = orderItemToCancel.getDiscountedPrice() * orderItemToCancel.getQuantity();
                double totalDiscountedPrice = order.getTotalDiscountedPrice();
                int redeemedPoints = order.getRedeemedPoints();
                totalRefundPoints += (int) ((itemDiscountedPrice / totalDiscountedPrice) * redeemedPoints);

                orderItemToCancel.setCancelStatus(CancelStatus.CANCELLED);
                orderItemRepository.save(orderItemToCancel); // Save cancel status to the database
            } else {
                throw new OrderException("Order item not found in the order");
            }
        }

        // Calculate the new totals based on the remaining items in the order
        double newTotalPrice = 0;
        double newTotalDiscountedPrice = 0;
        int newTotalItem = 0;
        boolean allItemsCancelled = true;
        for (OrderItem item : order.getOrderItems()) {
            newTotalPrice += item.getPrice() * item.getQuantity();
            newTotalDiscountedPrice += item.getDiscountedPrice() * item.getQuantity();
            newTotalItem += item.getQuantity();

            if (item.getCancelStatus() != CancelStatus.CANCELLED) {
                allItemsCancelled = false;
            }
        }
        order.setTotalPrice(newTotalPrice);
        order.setTotalDiscountedPrice((int) newTotalDiscountedPrice);
        order.setTotalItem(newTotalItem);
        order.setDiscounte((int) (newTotalPrice - newTotalDiscountedPrice));
        order.setCancellationReason(cancellationReason);

        // Update the redeemed points
        order.setRedeemedPoints(order.getRedeemedPoints() - totalRefundPoints);

        // Set other order details
        if (allItemsCancelled) {
            order.setOrderStatus(OrderStatus.CANCELLED);
        } else {
            order.setOrderStatus(OrderStatus.PARTIAL_CANCELLED);
        }
        order.setComments(comments);

        return orderRepository.save(order);
    }

    private void handleCancelOrderItem(Order order, OrderItem orderItemToCancel) throws OrderException {
        User user = order.getUser();
        Product product = orderItemToCancel.getProduct();
        String sizeName = orderItemToCancel.getSize();
        if (sizeName != null && !sizeName.isEmpty()) {
            product.increaseInventory(sizeName, orderItemToCancel.getQuantity());
            productService.save(product);
        } else {
            throw new OrderException("Size information is missing for an order item");
        }

        Wallet userWallet = order.getUser().getWallet();
        BigDecimal refundAmount = BigDecimal.valueOf(orderItemToCancel.getDiscountedPrice() * orderItemToCancel.getQuantity());
        userWallet.setBalance(userWallet.getBalance().add(refundAmount));
        walletRepository.save(userWallet);

        // Create a transaction entry to reflect the frozen amount
        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setTransactionType(TransactionType.REFUND_AGAINST_ORDER_CANCELLATION);
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setAmount(refundAmount);
        transactionRepository.save(transaction);

        int redeemedPoints = order.getRedeemedPoints();
        double totalDiscountedPrice = order.getTotalDiscountedPrice();
        double itemDiscountedPrice = orderItemToCancel.getDiscountedPrice() * orderItemToCancel.getQuantity();
        int refundedPoints = (int) ((itemDiscountedPrice / totalDiscountedPrice) * redeemedPoints);
        rewardService.revertPoints(order.getUser(), refundedPoints);

        orderItemToCancel.setCancelStatus(CancelStatus.CANCELLED);
        orderItemRepository.save(orderItemToCancel);
    }


    @Override
    public Order findOrderById(Long orderId) throws OrderException {
        Optional<Order> opt = orderRepository.findById(orderId);
        if (opt.isPresent()) {
            Order order = opt.get();
            return order;
        }
        throw new OrderException("order not exist with id " + orderId);
    }

    @Override
    public List<Order> usersOrderHistory(Long userId) {
        List<Order> orders = orderRepository.getUsersOrders(userId);
        return orders;
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public void deleteOrder(Long orderId) throws OrderException {
        Order order = findOrderById(orderId);
        orderRepository.deleteById(orderId);
    }

    @Override
    public Order returnProduct(Long orderId, List<ReturnItemRequest> returnItemRequests, ReturnReason returnReason, String comments, MultipartFile file) throws OrderException {
        Order order = findOrderById(orderId);
        List<OrderItem> itemsToReturn = new ArrayList<>();
        int totalRefundPoints = 0;

        for (ReturnItemRequest returnItemRequest : returnItemRequests) {
            Long orderItemId = returnItemRequest.getOrderItemId();

            Optional<OrderItem> orderItemOpt = order.getOrderItems().stream().filter(item -> item.getId().equals(orderItemId)).findFirst();

            if (orderItemOpt.isPresent()) {
                OrderItem orderItemToReturn = orderItemOpt.get();
                handleReturnOrderItem(order, orderItemToReturn);
                itemsToReturn.add(orderItemToReturn);

                double itemDiscountedPrice = orderItemToReturn.getDiscountedPrice() * orderItemToReturn.getQuantity();
                double totalDiscountedPrice = order.getTotalDiscountedPrice();
                int redeemedPoints = order.getRedeemedPoints();
                totalRefundPoints += (int) ((itemDiscountedPrice / totalDiscountedPrice) * redeemedPoints);

                orderItemToReturn.setReturnStatus(ReturnStatus.RETURNED);
                orderItemRepository.save(orderItemToReturn); // Save return status to the database
            } else {
                throw new OrderException("Order item not found in the order");
            }
        }

        // Calculate the new totals based on the remaining items in the order
        double newTotalPrice = 0;
        double newTotalDiscountedPrice = 0;
        int newTotalItem = 0;
        boolean allItemsReturned=true;
        for (OrderItem item : order.getOrderItems()) {
            newTotalPrice += item.getPrice() * item.getQuantity();
            newTotalDiscountedPrice += item.getDiscountedPrice() * item.getQuantity();
            newTotalItem += item.getQuantity();

            if(item.getReturnStatus()!=ReturnStatus.RETURNED)
            {
                allItemsReturned=false;
            }
        }
        order.setTotalPrice(newTotalPrice);
        order.setTotalDiscountedPrice((int) newTotalDiscountedPrice);
        order.setTotalItem(newTotalItem);
        order.setDiscounte((int) (newTotalPrice - newTotalDiscountedPrice));

        // Update the redeemed points
        order.setRedeemedPoints(order.getRedeemedPoints() - totalRefundPoints);

        // Set other order details
        if (file != null && !file.isEmpty()) {
            String fileName = StringUtils.cleanPath(file.getOriginalFilename());
            order.setAttachment(fileName);
        }

        if (allItemsReturned) {
            order.setOrderStatus(OrderStatus.RETURNED);
        } else {
            order.setOrderStatus(OrderStatus.PARTIAL_RETURNED);
        }
        order.setReturnReason(returnReason);
        order.setComments(comments);

        return orderRepository.save(order);
    }


    private void handleReturnOrderItem(Order order, OrderItem orderItemToReturn) throws OrderException {
        User user = order.getUser();
        Product product = orderItemToReturn.getProduct();
        String sizeName = orderItemToReturn.getSize();
        if (sizeName != null && !sizeName.isEmpty()) {
            product.increaseInventory(sizeName, orderItemToReturn.getQuantity());
            productService.save(product);
        } else {
            throw new OrderException("Size information is missing for an order item");
        }

        Wallet userWallet = order.getUser().getWallet();
        BigDecimal refundAmount = BigDecimal.valueOf(orderItemToReturn.getDiscountedPrice() * orderItemToReturn.getQuantity());
        userWallet.setBalance(userWallet.getBalance().add(refundAmount));
        walletRepository.save(userWallet);

        // Create a transaction entry to reflect the frozen amount
        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setTransactionType(TransactionType.REFUND_AGAINST_ORDER_RETURNED);
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setAmount(refundAmount);
        transactionRepository.save(transaction);

        int redeemedPoints = order.getRedeemedPoints();
        double totalDiscountedPrice = order.getTotalDiscountedPrice();
        double itemDiscountedPrice = orderItemToReturn.getDiscountedPrice() * orderItemToReturn.getQuantity();
        int refundedPoints = (int) ((itemDiscountedPrice / totalDiscountedPrice) * redeemedPoints);
        rewardService.revertPoints(order.getUser(), refundedPoints);

        orderItemToReturn.setReturnStatus(ReturnStatus.RETURNED);
        orderItemRepository.save(orderItemToReturn);
    }



    @Override
    @Transactional
    public Order exchangeProduct(Long orderId, List<ExchangeItemRequest> exchangeItemRequests, ReturnReason returnReason, String comments) throws OrderException {
        Order originalOrder = findOrderById(orderId);

        // Create a new exchange order
        Order exchangeOrder = new Order();
        exchangeOrder.setUser(originalOrder.getUser());
        exchangeOrder.setOrderStatus(OrderStatus.EXCHANGED);
        exchangeOrder.setOrderDate(LocalDateTime.now());
        exchangeOrder.setCreatedAt(LocalDateTime.now());
        exchangeOrder.setReturnReason(returnReason);
        exchangeOrder.setComments(comments);
        exchangeOrder.setShippingAddress(originalOrder.getShippingAddress());

        List<OrderItem> exchangedOrderItems = new ArrayList<>();

        for (ExchangeItemRequest exchangeRequest : exchangeItemRequests) {
            Long orderItemId = exchangeRequest.getOrderItemId();
            Long newProductId = exchangeRequest.getNewProductId();
            int quantity = exchangeRequest.getQuantity();
            String newSize = exchangeRequest.getSize();

            // Find the original order item to be exchanged
            OrderItem originalOrderItem = originalOrder.getOrderItems().stream().filter(item -> item.getId().equals(orderItemId)).findFirst().orElseThrow(() -> new OrderException("Order item not found in the order"));

            // Remove the original order item from the original order
            originalOrderItem.setExchangeStatus(ExchangeStatus.COMPLETED); // Update exchange status

            // Create a new order item for the exchange order
            OrderItem exchangedOrderItem = new OrderItem();
            exchangedOrderItem.setProduct(productService.getProductById(newProductId)); // Assuming productService.getProductById exists and retrieves the product
            exchangedOrderItem.setQuantity(quantity);
            exchangedOrderItem.setPrice(originalOrderItem.getPrice()); // Copy price from the original order item
            exchangedOrderItem.setDiscountedPrice(originalOrderItem.getDiscountedPrice()); // Copy discounted price from the original order item
            exchangedOrderItem.setSize(newSize);
            exchangedOrderItem.setExchangeStatus(ExchangeStatus.REQUESTED); // Set exchange status

            // Handle inventory update for the original order item size
            Product originalProduct = originalOrderItem.getProduct();
            Size originalSize = originalProduct.getSizes().stream().filter(size -> size.getName().equals(originalOrderItem.getSize())).findFirst().orElseThrow(() -> new OrderException("Size not found in the original product"));

            originalSize.increaseQuantity(originalOrderItem.getQuantity()); // Restore original size quantity
            productService.save(originalProduct); // Save changes to the original product

            // Handle inventory update for the exchanged order item size
            Product exchangedProduct = exchangedOrderItem.getProduct();
            Size exchangedSize = exchangedProduct.getSizes().stream().filter(size -> size.getName().equals(newSize)).findFirst().orElseThrow(() -> new OrderException("Size not found in the exchanged product"));

            if (exchangedSize.getQuantity() < exchangedOrderItem.getQuantity()) {
                throw new OrderException("Insufficient stock for exchanged size " + exchangedSize.getName());
            }

            exchangedSize.decreaseQuantity(exchangedOrderItem.getQuantity()); // Decrease exchanged size quantity
            productService.save(exchangedProduct); // Save changes to the exchanged product

            // Set the exchanged order item in the exchange order
            exchangedOrderItem.setOrder(exchangeOrder);
            exchangedOrderItems.add(exchangedOrderItem);
        }

        // Set exchanged order items in the exchange order
        exchangeOrder.setOrderItems(exchangedOrderItems);
        exchangeOrder.setReturnReason(returnReason);
        exchangeOrder.setComments(comments);

        // Save the exchange order
        Order savedExchangeOrder = orderRepository.save(exchangeOrder);

        // Update the original order in the database
        orderRepository.save(originalOrder);

        return savedExchangeOrder;
    }

    @Override
    public Order createOrderForSubscriptionDelivery(Subscription subscription) {
        Order order = new Order();

        // Set relevant details for the order
        order.setUser(subscription.getUser());
        order.setSubscription(subscription);
        order.setOrderStatus(OrderStatus.PENDING);
        order.setOrderDate(LocalDateTime.now());

        // Assuming the product is associated with the subscription
        Product product = subscription.getProduct();

        // Create order items for each delivery day
        List<OrderItem> orderItems = new ArrayList<>();
        for (LocalDate deliveryDay : subscription.getDeliveryDays()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(1); // Assuming one quantity for simplicity
            orderItem.setPrice(product.getPrice()); // Convert BigDecimal to double
            orderItem.setDiscountedPrice(product.getDiscountedPrice()); // Convert BigDecimal to double
            orderItem.setDeliveryDate(deliveryDay.atStartOfDay());
            orderItems.add(orderItem);
        }

        // Set the order items in the order
        order.setOrderItems(orderItems);

        // Calculate and set total price and total discounted price
        double totalPrice = orderItems.stream().mapToDouble(OrderItem::getPrice).sum();
        double totalDiscountedPrice = orderItems.stream().mapToDouble(OrderItem::getDiscountedPrice).sum();

        order.setTotalPrice(totalPrice);
        order.setTotalDiscountedPrice((int) totalDiscountedPrice);

        // Save the order in the database
        Order savedOrder = orderRepository.save(order);

        // You can perform additional operations like sending confirmation emails, updating inventory, etc.

        return savedOrder;
    }

}