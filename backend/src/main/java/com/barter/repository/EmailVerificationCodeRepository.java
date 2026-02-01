package com.barter.repository;

import com.barter.entity.EmailVerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.Optional;

public interface EmailVerificationCodeRepository extends JpaRepository<EmailVerificationCode, Long> {
    Optional<EmailVerificationCode> findByEmailAndCodeAndUsedFalseAndExpiresAtAfter(
            String email, String code, LocalDateTime now);
    
    long countByEmailAndCreatedAtAfter(String email, LocalDateTime after);
    
    void deleteByExpiresAtBefore(LocalDateTime before);
}
