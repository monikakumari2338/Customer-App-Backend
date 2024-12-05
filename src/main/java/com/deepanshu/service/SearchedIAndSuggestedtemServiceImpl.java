package com.deepanshu.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.deepanshu.modal.Order;
import com.deepanshu.modal.OrderItem;
import com.deepanshu.modal.Product;
import com.deepanshu.modal.SearchedItem;
import com.deepanshu.modal.User;
import com.deepanshu.modal.WishlistItem;
import com.deepanshu.repository.OrderItemRepository;
import com.deepanshu.repository.OrderRepository;
import com.deepanshu.repository.ProductRepository;
import com.deepanshu.repository.SearchedItemRepository;
import com.deepanshu.repository.UserRepository;
import com.deepanshu.repository.WishlistItemRepository;

@Service
public class SearchedIAndSuggestedtemServiceImpl implements SearchedIAndSuggestedtemService {

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private SearchedItemRepository searchedItemRepository;

	@Autowired
	private WishlistItemRepository wishlistItemRepository;

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private OrderItemRepository orderItemRepository;

	@Autowired
	private UserRepository userRepository;

	@Override
	public String saveSearchedItems(String param, Long userId) {

		LocalDateTime dateTime = LocalDateTime.now();
		List<Product> products = productRepository.findByDescriptionContaining(param);
		User user = userRepository.findById(userId).get();
		products.forEach(product -> {
			SearchedItem searchedItem = new SearchedItem(user, product, dateTime);
			searchedItemRepository.save(searchedItem);
		});
		return "Searched items save successfully";
	}

//	@Override
//	public List<Product> getSuggestedProducts(Long userId) {
//
//		User user = userRepository.findById(userId).get();
//		// Fetch latest 5 products for the user based on date and time
//		List<SearchedItem> searchItems = searchedItemRepository.findTop5ByUserOrderByDateTimeDesc(user);
//		System.out.println("SearchedItem : " + searchItems);
//		List<WishlistItem> wishListItems = wishlistItemRepository.findAllByUserId(userId);
//
//		// Step 1: Fetch the latest 5 orders
//		List<Order> latestOrders = orderRepository.findTop5ByUserOrderByCreatedAtDesc(user);
//		System.out.println("latestOrders : " + latestOrders);
//		// Step 2: Extract order IDs
//		List<Long> orderIds = latestOrders.stream().map(Order::getId).collect(Collectors.toList());
//		// Step 3: Fetch order items based on the order IDs
//		List<OrderItem> orderedProducts = orderItemRepository.findAllByOrderIds(orderIds);
//
//		// Store product priorities
//		Map<Long, Integer> productPriorities = new HashMap<>();
//		// Calculate priorities for each list
//		calculatePriorities(searchItems, SEARCH_PRIORITY, productPriorities, wishListItems, orderedProducts);
//		calculatePriorities(wishListItems, WISHLIST_PRIORITY, productPriorities, searchItems, orderedProducts);
//		calculatePriorities(orderedProducts, ORDER_PRIORITY, productPriorities, searchItems, wishListItems);
//		// Sort and return the top five products by priority
//
//		System.out.println("productPriorities : " + productPriorities);
//		return productPriorities.entrySet().stream().sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
//				.limit(10).map(entry -> findProductById(entry.getKey())).collect(Collectors.toList());
//	}
//
//	private void calculatePriorities(List<?> items, int basePriority, Map<Long, Integer> productPriorities,
//			List<?> listA, List<?> listB) {
//		for (Object item : items) {
//			Long productId = extractProductId(item);
//			int priority = productPriorities.getOrDefault(productId, 0) + basePriority;
//			if (listA.stream().anyMatch(other -> extractProductId(other).equals(productId))) {
//				priority += WISHLIST_PRIORITY;
//			}
//			if (listB.stream().anyMatch(other -> extractProductId(other).equals(productId))) {
//				priority += ORDER_PRIORITY;
//			}
//			productPriorities.put(productId, priority);
//		}
//	}
//
//	private Long extractProductId(Object item) {
//		if (item instanceof SearchedItem) {
//			return ((SearchedItem) item).getProduct().getId();
//		} else if (item instanceof WishlistItem) {
//			return ((WishlistItem) item).getProduct().getId();
//		} else if (item instanceof OrderItem) {
//			return ((OrderItem) item).getProduct().getId();
//		}
//		throw new IllegalArgumentException("Unknown item type");
//	}

	private Product findProductById(Long productId) {
		// Replace this with a repository call to fetch the Product by ID
		return productRepository.findById(productId).orElseThrow(() -> new NoSuchElementException("Product not found"));
	}

	public List<Product> getSuggestedProducts(Long userId) {
		// Priority values
		int searchPriority = 5;
		int wishlistPriority = 10;
		int orderedPriority = 15;

		User user = userRepository.findById(userId).get();

		List<SearchedItem> searchedItems = searchedItemRepository.findTop5ByUserOrderByDateTimeDesc(user);
		List<WishlistItem> wishListItems = wishlistItemRepository.findAllByUserId(userId);

		List<Order> latestOrders = orderRepository.findTop5ByUserOrderByCreatedAtDesc(user);
		List<Long> orderIds = latestOrders.stream().map(Order::getId).collect(Collectors.toList());
		List<OrderItem> orderedProducts = orderItemRepository.findAllByOrderIds(orderIds);

		Map<Long, Integer> priorityScores = new HashMap<>();

		for (OrderItem productId : orderedProducts) {
			priorityScores.put(productId.getProduct().getId(),
					priorityScores.getOrDefault(productId.getProduct().getId(), 0) + orderedPriority);
		}
		// Process the "wishlist" list
		for (WishlistItem productId : wishListItems) {
			priorityScores.put(productId.getProduct().getId(),
					priorityScores.getOrDefault(productId.getProduct().getId(), 0) + wishlistPriority);
		}
		// Process the "searched" list
		for (SearchedItem productId : searchedItems) {
			priorityScores.put(productId.getProduct().getId(),
					priorityScores.getOrDefault(productId.getProduct().getId(), 0) + searchPriority);
		}

		System.out.println("priorityScores  " + priorityScores);
		return priorityScores.entrySet().stream().sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
				.limit(10).map(entry -> findProductById(entry.getKey())).collect(Collectors.toList());
	}

}
