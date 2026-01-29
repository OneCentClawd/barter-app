package com.barter.service;

import com.barter.dto.ItemDto;
import com.barter.dto.TradeDto;
import com.barter.entity.Item;
import com.barter.entity.TradeRequest;
import com.barter.entity.User;
import com.barter.repository.ItemRepository;
import com.barter.repository.TradeRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class TradeService {

    private final TradeRequestRepository tradeRequestRepository;
    private final ItemRepository itemRepository;
    private final ItemService itemService;

    @Transactional
    public TradeDto.TradeResponse createTradeRequest(TradeDto.CreateRequest request, User requester) {
        Item targetItem = itemRepository.findById(request.getTargetItemId())
                .orElseThrow(() -> new RuntimeException("目标物品不存在"));

        Item offeredItem = itemRepository.findById(request.getOfferedItemId())
                .orElseThrow(() -> new RuntimeException("交换物品不存在"));

        // 验证
        if (targetItem.getOwner().getId().equals(requester.getId())) {
            throw new RuntimeException("不能与自己的物品交换");
        }

        if (!offeredItem.getOwner().getId().equals(requester.getId())) {
            throw new RuntimeException("只能用自己的物品进行交换");
        }

        if (targetItem.getStatus() != Item.ItemStatus.AVAILABLE) {
            throw new RuntimeException("目标物品不可交换");
        }

        if (offeredItem.getStatus() != Item.ItemStatus.AVAILABLE) {
            throw new RuntimeException("您的物品不可交换");
        }

        // 检查是否已有待处理的请求
        if (tradeRequestRepository.existsByRequesterAndTargetItemAndStatusIn(
                requester, targetItem,
                Arrays.asList(TradeRequest.TradeStatus.PENDING, TradeRequest.TradeStatus.ACCEPTED))) {
            throw new RuntimeException("已存在待处理的交换请求");
        }

        TradeRequest tradeRequest = new TradeRequest();
        tradeRequest.setRequester(requester);
        tradeRequest.setTargetItem(targetItem);
        tradeRequest.setOfferedItem(offeredItem);
        tradeRequest.setMessage(request.getMessage());

        tradeRequest = tradeRequestRepository.save(tradeRequest);
        return toTradeResponse(tradeRequest);
    }

    @Transactional
    public TradeDto.TradeResponse updateTradeStatus(Long id, TradeRequest.TradeStatus newStatus, User user) {
        TradeRequest tradeRequest = tradeRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("交换请求不存在"));

        // 验证权限
        boolean isTargetOwner = tradeRequest.getTargetItem().getOwner().getId().equals(user.getId());
        boolean isRequester = tradeRequest.getRequester().getId().equals(user.getId());

        if (!isTargetOwner && !isRequester) {
            throw new RuntimeException("无权操作此交换请求");
        }

        TradeRequest.TradeStatus currentStatus = tradeRequest.getStatus();

        // 状态流转逻辑
        switch (newStatus) {
            case ACCEPTED:
            case REJECTED:
                if (!isTargetOwner || currentStatus != TradeRequest.TradeStatus.PENDING) {
                    throw new RuntimeException("无法执行此操作");
                }
                if (newStatus == TradeRequest.TradeStatus.ACCEPTED) {
                    // 将相关物品标记为交换中
                    tradeRequest.getTargetItem().setStatus(Item.ItemStatus.PENDING);
                    tradeRequest.getOfferedItem().setStatus(Item.ItemStatus.PENDING);
                }
                break;

            case COMPLETED:
                if (currentStatus != TradeRequest.TradeStatus.ACCEPTED) {
                    throw new RuntimeException("只有已接受的请求才能完成");
                }
                // 将物品标记为已交换
                tradeRequest.getTargetItem().setStatus(Item.ItemStatus.TRADED);
                tradeRequest.getOfferedItem().setStatus(Item.ItemStatus.TRADED);
                break;

            case CANCELLED:
                if (!isRequester || currentStatus != TradeRequest.TradeStatus.PENDING) {
                    throw new RuntimeException("只有请求方可以取消待处理的请求");
                }
                break;

            default:
                throw new RuntimeException("无效的状态");
        }

        tradeRequest.setStatus(newStatus);
        tradeRequest.setUpdatedAt(LocalDateTime.now());
        tradeRequest = tradeRequestRepository.save(tradeRequest);

        return toTradeResponse(tradeRequest);
    }

    public Page<TradeDto.TradeResponse> getSentRequests(User user, Pageable pageable) {
        return tradeRequestRepository.findByRequester(user, pageable)
                .map(this::toTradeResponse);
    }

    public Page<TradeDto.TradeResponse> getReceivedRequests(User user, Pageable pageable) {
        return tradeRequestRepository.findByTargetItemOwner(user, pageable)
                .map(this::toTradeResponse);
    }

    public TradeDto.TradeResponse getTradeRequest(Long id, User user) {
        TradeRequest tradeRequest = tradeRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("交换请求不存在"));

        boolean isTargetOwner = tradeRequest.getTargetItem().getOwner().getId().equals(user.getId());
        boolean isRequester = tradeRequest.getRequester().getId().equals(user.getId());

        if (!isTargetOwner && !isRequester) {
            throw new RuntimeException("无权查看此交换请求");
        }

        return toTradeResponse(tradeRequest);
    }

    private TradeDto.TradeResponse toTradeResponse(TradeRequest tradeRequest) {
        TradeDto.TradeResponse response = new TradeDto.TradeResponse();
        response.setId(tradeRequest.getId());
        response.setTargetItem(itemService.toItemListResponse(tradeRequest.getTargetItem()));
        response.setOfferedItem(itemService.toItemListResponse(tradeRequest.getOfferedItem()));
        response.setRequester(itemService.toUserBrief(tradeRequest.getRequester()));
        response.setMessage(tradeRequest.getMessage());
        response.setStatus(tradeRequest.getStatus());
        response.setCreatedAt(tradeRequest.getCreatedAt());
        return response;
    }
}
