package com.deepanshu.repository;

import com.deepanshu.modal.SearchedItem;
import com.deepanshu.modal.Wishlist;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    @Query("SELECT c From Wishlist c where c.user.id=:userId")
    public Wishlist findByUserId(@Param("userId") Long userId);
    
//    @Query(value = "SELECT * FROM SearchedItem ORDER BY dateTime DESC LIMIT 5", nativeQuery = true)
//	List<SearchedItem> findLatestFive();
}