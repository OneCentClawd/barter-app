package com.barter.repository;

import com.barter.entity.LoginRecord;
import com.barter.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LoginRecordRepository extends JpaRepository<LoginRecord, Long> {
    
    Page<LoginRecord> findByUserOrderByLoginTimeDesc(User user, Pageable pageable);
    
    List<LoginRecord> findTop10ByUserOrderByLoginTimeDesc(User user);
}
