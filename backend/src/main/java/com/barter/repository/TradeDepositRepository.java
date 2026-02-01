package com.barter.repository;

import com.barter.entity.TradeDeposit;
import com.barter.entity.TradeRequest;
import com.barter.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TradeDepositRepository extends JpaRepository<TradeDeposit, Long> {
    List<TradeDeposit> findByTradeRequest(TradeRequest tradeRequest);
    Optional<TradeDeposit> findByTradeRequestAndUser(TradeRequest tradeRequest, User user);
}
