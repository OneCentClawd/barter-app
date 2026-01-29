package com.barter.repository;

import com.barter.entity.Conversation;
import com.barter.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    @Query("SELECT c FROM Conversation c WHERE " +
           "(c.user1 = :user1 AND c.user2 = :user2) OR " +
           "(c.user1 = :user2 AND c.user2 = :user1)")
    Optional<Conversation> findByUsers(@Param("user1") User user1, @Param("user2") User user2);

    @Query("SELECT c FROM Conversation c WHERE c.user1 = :user OR c.user2 = :user ORDER BY c.lastMessageAt DESC")
    Page<Conversation> findByUser(@Param("user") User user, Pageable pageable);
}
