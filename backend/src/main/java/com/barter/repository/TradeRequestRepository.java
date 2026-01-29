package com.barter.repository;

import com.barter.entity.TradeRequest;
import com.barter.entity.User;
import com.barter.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TradeRequestRepository extends JpaRepository<TradeRequest, Long> {

    // 我发起的交换请求
    Page<TradeRequest> findByRequester(User requester, Pageable pageable);

    // 别人向我发起的交换请求
    @Query("SELECT tr FROM TradeRequest tr WHERE tr.targetItem.owner = :owner")
    Page<TradeRequest> findByTargetItemOwner(@Param("owner") User owner, Pageable pageable);

    // 检查是否已经发起过请求
    boolean existsByRequesterAndTargetItemAndStatusIn(
        User requester, Item targetItem, java.util.List<TradeRequest.TradeStatus> statuses);
}
