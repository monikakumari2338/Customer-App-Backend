package com.deepanshu.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.deepanshu.modal.Order;
import com.deepanshu.modal.User;

public interface OrderRepository extends JpaRepository<Order, Long> {
	@Query("SELECT o FROM Order o WHERE o.user.id = :userId")
	public List<Order> getUsersOrders(@Param("userId") Long userId);

	// List<Order> findTop5ByOrderByCreatedAtDesc();

	public List<Order> findTop5ByUserOrderByCreatedAtDesc(User user);

}