package com.barter.repository;

import com.barter.entity.CreditRecord;
import com.barter.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CreditRecordRepository extends JpaRepository<CreditRecord, Long> {
    Page<CreditRecord> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
}
