package com.deepanshu.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.deepanshu.modal.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

}
