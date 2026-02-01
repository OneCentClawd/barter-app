package com.barter.repository;

import com.barter.entity.WalletTransaction;
import com.barter.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
    Page<WalletTransaction> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
}
