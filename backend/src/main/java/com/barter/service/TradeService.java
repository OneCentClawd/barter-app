package com.barter.service;

import com.barter.dto.ItemDto;
import com.barter.dto.TradeDto;
import com.barter.entity.Item;
import com.barter.entity.Notification;
import com.barter.entity.TradeRequest;
import com.barter.entity.User;
import com.barter.repository.ItemRepository;
import com.barter.repository.NotificationRepository;
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
    private final NotificationRepository notificationRepository;

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
                // 记录谁确认了
                if (isRequester) {
                    if (tradeRequest.getRequesterConfirmed()) {
                        throw new RuntimeException("您已确认过了");
                    }
                    tradeRequest.setRequesterConfirmed(true);
                } else if (isTargetOwner) {
                    if (tradeRequest.getTargetConfirmed()) {
                        throw new RuntimeException("您已确认过了");
                    }
                    tradeRequest.setTargetConfirmed(true);
                }
                
                // 双方都确认了才真正完成
                if (tradeRequest.getRequesterConfirmed() && tradeRequest.getTargetConfirmed()) {
                    // 将物品标记为已交换
                    tradeRequest.getTargetItem().setStatus(Item.ItemStatus.TRADED);
                    tradeRequest.getOfferedItem().setStatus(Item.ItemStatus.TRADED);
                    tradeRequest.setStatus(TradeRequest.TradeStatus.COMPLETED);
                }
                tradeRequest.setUpdatedAt(LocalDateTime.now());
                tradeRequest = tradeRequestRepository.save(tradeRequest);
                return toTradeResponse(tradeRequest);  // 提前返回，不改变状态

            case CANCELLED:
                // 只有 PENDING 和 ACCEPTED 状态可以取消
                if (currentStatus != TradeRequest.TradeStatus.PENDING && 
                    currentStatus != TradeRequest.TradeStatus.ACCEPTED) {
                    throw new RuntimeException("当前状态无法取消");
                }
                
                // 如果是 ACCEPTED 状态取消，需要恢复物品状态并通知对方
                if (currentStatus == TradeRequest.TradeStatus.ACCEPTED) {
                    // 恢复物品为可用状态
                    tradeRequest.getTargetItem().setStatus(Item.ItemStatus.AVAILABLE);
                    tradeRequest.getOfferedItem().setStatus(Item.ItemStatus.AVAILABLE);
                    
                    // 通知被取消的一方
                    User cancelledUser = isRequester ? 
                        tradeRequest.getTargetItem().getOwner() : 
                        tradeRequest.getRequester();
                    String cancellerName = isRequester ? 
                        (tradeRequest.getRequester().getNickname() != null ? 
                            tradeRequest.getRequester().getNickname() : 
                            tradeRequest.getRequester().getUsername()) :
                        (user.getNickname() != null ? user.getNickname() : user.getUsername());
                    
                    Notification notification = new Notification();
                    notification.setUser(cancelledUser);
                    notification.setType(Notification.NotificationType.TRADE);
                    notification.setTitle("交换已取消");
                    notification.setContent(cancellerName + " 取消了与您的交换请求");
                    notification.setRelatedId(tradeRequest.getId());
                    notificationRepository.save(notification);
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
        response.setRequesterConfirmed(tradeRequest.getRequesterConfirmed());
        response.setTargetConfirmed(tradeRequest.getTargetConfirmed());
        response.setCreatedAt(tradeRequest.getCreatedAt());
        return response;
    }
}
