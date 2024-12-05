package com.deepanshu.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.deepanshu.modal.User;

public interface UserRepository extends JpaRepository<User, Long> {

	public User findByEmail(String email);

	User findByReferralCode(String referralCode);

	//public Optional<User> findById(Long UserId);
}
