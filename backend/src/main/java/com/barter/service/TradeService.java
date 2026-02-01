package com.barter.service;

import com.barter.dto.ItemDto;
import com.barter.dto.TradeDto;
import com.barter.entity.*;
import com.barter.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class TradeService {

    private final TradeRequestRepository tradeRequestRepository;
    private final ItemRepository itemRepository;
    private final ItemService itemService;
    private final NotificationRepository notificationRepository;
    private final CreditService creditService;
    private final WalletService walletService;
    private final TradeDepositRepository tradeDepositRepository;

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
        
        // 远程交易权限检查
        TradeRequest.TradeMode tradeMode = request.getTradeMode() != null ? 
                request.getTradeMode() : TradeRequest.TradeMode.IN_PERSON;
        
        if (tradeMode == TradeRequest.TradeMode.REMOTE) {
            if (!creditService.canRemoteTrade(requester)) {
                throw new RuntimeException("您的信用分不足，无法发起远程交易");
            }
            if (request.getEstimatedValue() == null || request.getEstimatedValue().compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("远程交易需要填写物品估值");
            }
        }

        TradeRequest tradeRequest = new TradeRequest();
        tradeRequest.setRequester(requester);
        tradeRequest.setTargetItem(targetItem);
        tradeRequest.setOfferedItem(offeredItem);
        tradeRequest.setMessage(request.getMessage());
        tradeRequest.setTradeMode(tradeMode);
        tradeRequest.setEstimatedValue(request.getEstimatedValue());

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
        boolean isRemote = tradeRequest.getTradeMode() == TradeRequest.TradeMode.REMOTE;

        // 状态流转逻辑
        switch (newStatus) {
            case ACCEPTED:
            case REJECTED:
                if (!isTargetOwner || currentStatus != TradeRequest.TradeStatus.PENDING) {
                    throw new RuntimeException("无法执行此操作");
                }
                if (newStatus == TradeRequest.TradeStatus.ACCEPTED) {
                    // 远程交易需要检查对方信用分
                    if (isRemote) {
                        User targetOwner = tradeRequest.getTargetItem().getOwner();
                        if (!creditService.canRemoteTrade(targetOwner)) {
                            throw new RuntimeException("您的信用分不足，无法接受远程交易");
                        }
                    }
                    // 将相关物品标记为交换中
                    tradeRequest.getTargetItem().setStatus(Item.ItemStatus.PENDING);
                    tradeRequest.getOfferedItem().setStatus(Item.ItemStatus.PENDING);
                }
                break;

            case COMPLETED:
                // 面交：ACCEPTED 状态可完成
                // 远程：DELIVERED 状态可完成
                if (isRemote) {
                    if (currentStatus != TradeRequest.TradeStatus.DELIVERED) {
                        throw new RuntimeException("远程交易需要双方都确认收货后才能完成");
                    }
                } else {
                    if (currentStatus != TradeRequest.TradeStatus.ACCEPTED) {
                        throw new RuntimeException("只有已接受的请求才能完成");
                    }
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
                    completeTradeRequest(tradeRequest);
                }
                tradeRequest.setUpdatedAt(LocalDateTime.now());
                tradeRequest = tradeRequestRepository.save(tradeRequest);
                return toTradeResponse(tradeRequest);

            case CANCELLED:
                handleCancellation(tradeRequest, user, isRequester, isTargetOwner, currentStatus);
                break;

            default:
                throw new RuntimeException("无效的状态");
        }

        tradeRequest.setStatus(newStatus);
        tradeRequest.setUpdatedAt(LocalDateTime.now());
        tradeRequest = tradeRequestRepository.save(tradeRequest);

        return toTradeResponse(tradeRequest);
    }
    
    /**
     * 支付保证金
     */
    @Transactional
    public TradeDto.TradeResponse payDeposit(Long tradeId, User user) {
        TradeRequest tradeRequest = tradeRequestRepository.findById(tradeId)
                .orElseThrow(() -> new RuntimeException("交换请求不存在"));
        
        if (tradeRequest.getTradeMode() != TradeRequest.TradeMode.REMOTE) {
            throw new RuntimeException("面交不需要支付保证金");
        }
        
        if (tradeRequest.getStatus() != TradeRequest.TradeStatus.ACCEPTED) {
            throw new RuntimeException("当前状态无法支付保证金");
        }
        
        boolean isRequester = tradeRequest.getRequester().getId().equals(user.getId());
        boolean isTargetOwner = tradeRequest.getTargetItem().getOwner().getId().equals(user.getId());
        
        if (!isRequester && !isTargetOwner) {
            throw new RuntimeException("无权操作此交换请求");
        }
        
        // 检查是否已支付
        if (isRequester && tradeRequest.getRequesterDepositPaid()) {
            throw new RuntimeException("您已支付保证金");
        }
        if (isTargetOwner && tradeRequest.getTargetDepositPaid()) {
            throw new RuntimeException("您已支付保证金");
        }
        
        // 计算保证金
        BigDecimal estimatedValue = tradeRequest.getEstimatedValue();
        double ratio = creditService.getDepositRatio(user);
        BigDecimal depositAmount = estimatedValue.multiply(BigDecimal.valueOf(ratio));
        
        // 优先用积分，不足部分用现金（100积分 = 1元）
        UserWallet wallet = walletService.getOrCreateWallet(user);
        int availablePoints = wallet.getPoints() - wallet.getFrozenPoints();
        // 保证金需要多少积分（100积分=1元）
        int depositPointsNeeded = depositAmount.multiply(BigDecimal.valueOf(100)).intValue();
        int depositPoints = Math.min(availablePoints, depositPointsNeeded);
        // 积分不够的部分用现金补
        BigDecimal depositCash = depositAmount.subtract(BigDecimal.valueOf(depositPoints).divide(BigDecimal.valueOf(100)));
        
        // 冻结保证金
        walletService.freezeDeposit(user, depositPoints, depositCash, tradeId);
        
        // 记录保证金
        TradeDeposit deposit = new TradeDeposit();
        deposit.setTradeRequest(tradeRequest);
        deposit.setUser(user);
        deposit.setPointsAmount(depositPoints);
        deposit.setCashAmount(depositCash);
        deposit.setStatus(TradeDeposit.DepositStatus.FROZEN);
        tradeDepositRepository.save(deposit);
        
        // 更新支付状态
        if (isRequester) {
            tradeRequest.setRequesterDepositPaid(true);
        } else {
            tradeRequest.setTargetDepositPaid(true);
        }
        
        // 双方都支付了，进入待发货状态
        if (tradeRequest.getRequesterDepositPaid() && tradeRequest.getTargetDepositPaid()) {
            tradeRequest.setStatus(TradeRequest.TradeStatus.DEPOSIT_PAID);
        }
        
        tradeRequest.setUpdatedAt(LocalDateTime.now());
        tradeRequest = tradeRequestRepository.save(tradeRequest);
        
        return toTradeResponse(tradeRequest);
    }
    
    /**
     * 上传物流单号（发货）
     */
    @Transactional
    public TradeDto.TradeResponse shipItem(Long tradeId, String trackingNo, User user) {
        TradeRequest tradeRequest = tradeRequestRepository.findById(tradeId)
                .orElseThrow(() -> new RuntimeException("交换请求不存在"));
        
        if (tradeRequest.getTradeMode() != TradeRequest.TradeMode.REMOTE) {
            throw new RuntimeException("面交不需要发货");
        }
        
        if (tradeRequest.getStatus() != TradeRequest.TradeStatus.DEPOSIT_PAID &&
            tradeRequest.getStatus() != TradeRequest.TradeStatus.SHIPPING) {
            throw new RuntimeException("当前状态无法发货");
        }
        
        boolean isRequester = tradeRequest.getRequester().getId().equals(user.getId());
        boolean isTargetOwner = tradeRequest.getTargetItem().getOwner().getId().equals(user.getId());
        
        if (!isRequester && !isTargetOwner) {
            throw new RuntimeException("无权操作此交换请求");
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        if (isRequester) {
            if (tradeRequest.getRequesterTrackingNo() != null) {
                throw new RuntimeException("您已填写物流单号");
            }
            tradeRequest.setRequesterTrackingNo(trackingNo);
            tradeRequest.setRequesterShippedAt(now);
            
            // 加按时发货信用分
            creditService.onTimeShip(user, tradeId);
        } else {
            if (tradeRequest.getTargetTrackingNo() != null) {
                throw new RuntimeException("您已填写物流单号");
            }
            tradeRequest.setTargetTrackingNo(trackingNo);
            tradeRequest.setTargetShippedAt(now);
            
            creditService.onTimeShip(user, tradeId);
        }
        
        // 有一方发货就进入运输中状态
        if (tradeRequest.getStatus() == TradeRequest.TradeStatus.DEPOSIT_PAID) {
            tradeRequest.setStatus(TradeRequest.TradeStatus.SHIPPING);
        }
        
        // 双方都发货了，进入待收货状态
        if (tradeRequest.getRequesterTrackingNo() != null && tradeRequest.getTargetTrackingNo() != null) {
            tradeRequest.setStatus(TradeRequest.TradeStatus.DELIVERED);
        }
        
        tradeRequest.setUpdatedAt(now);
        tradeRequest = tradeRequestRepository.save(tradeRequest);
        
        // 通知对方
        User otherUser = isRequester ? tradeRequest.getTargetItem().getOwner() : tradeRequest.getRequester();
        Notification notification = new Notification();
        notification.setUser(otherUser);
        notification.setType(Notification.NotificationType.TRADE);
        notification.setTitle("对方已发货");
        notification.setContent("物流单号: " + trackingNo);
        notification.setRelatedId(tradeId);
        notificationRepository.save(notification);
        
        return toTradeResponse(tradeRequest);
    }
    
    /**
     * 完成交易
     */
    private void completeTradeRequest(TradeRequest tradeRequest) {
        Item targetItem = tradeRequest.getTargetItem();
        Item offeredItem = tradeRequest.getOfferedItem();
        User requester = tradeRequest.getRequester();
        User targetOwner = targetItem.getOwner();
        LocalDateTime now = LocalDateTime.now();
        
        // 记录原主人和交易信息
        targetItem.setPreviousOwner(targetOwner);
        targetItem.setTradedFromItem(offeredItem);
        targetItem.setTradeRequest(tradeRequest);
        targetItem.setTradedAt(now);
        
        offeredItem.setPreviousOwner(requester);
        offeredItem.setTradedFromItem(targetItem);
        offeredItem.setTradeRequest(tradeRequest);
        offeredItem.setTradedAt(now);
        
        // 转移所有权
        targetItem.setOwner(requester);
        offeredItem.setOwner(targetOwner);
        
        // 将物品标记为已交换
        targetItem.setStatus(Item.ItemStatus.TRADED);
        offeredItem.setStatus(Item.ItemStatus.TRADED);
        
        tradeRequest.setStatus(TradeRequest.TradeStatus.COMPLETED);
        
        // 远程交易：退还保证金
        if (tradeRequest.getTradeMode() == TradeRequest.TradeMode.REMOTE) {
            refundDeposits(tradeRequest);
        }
        
        // 双方加信用分
        creditService.onTradeComplete(requester, tradeRequest.getId());
        creditService.onTradeComplete(targetOwner, tradeRequest.getId());
    }
    
    /**
     * 退还保证金
     */
    private void refundDeposits(TradeRequest tradeRequest) {
        for (TradeDeposit deposit : tradeDepositRepository.findByTradeRequest(tradeRequest)) {
            if (deposit.getStatus() == TradeDeposit.DepositStatus.FROZEN) {
                walletService.unfreezeDeposit(
                        deposit.getUser(),
                        deposit.getPointsAmount(),
                        deposit.getCashAmount(),
                        tradeRequest.getId()
                );
                deposit.setStatus(TradeDeposit.DepositStatus.REFUNDED);
                tradeDepositRepository.save(deposit);
            }
        }
    }
    
    /**
     * 处理取消
     */
    private void handleCancellation(TradeRequest tradeRequest, User user, 
                                    boolean isRequester, boolean isTargetOwner,
                                    TradeRequest.TradeStatus currentStatus) {
        // 远程交易：支付保证金后取消需要扣分和没收保证金
        boolean isRemote = tradeRequest.getTradeMode() == TradeRequest.TradeMode.REMOTE;
        
        // 只有 PENDING, ACCEPTED, DEPOSIT_PAID 状态可以取消
        if (currentStatus != TradeRequest.TradeStatus.PENDING && 
            currentStatus != TradeRequest.TradeStatus.ACCEPTED &&
            currentStatus != TradeRequest.TradeStatus.DEPOSIT_PAID) {
            throw new RuntimeException("当前状态无法取消");
        }
        
        // 恢复物品状态
        if (currentStatus != TradeRequest.TradeStatus.PENDING) {
            tradeRequest.getTargetItem().setStatus(Item.ItemStatus.AVAILABLE);
            tradeRequest.getOfferedItem().setStatus(Item.ItemStatus.AVAILABLE);
        }
        
        // 远程交易且已支付保证金，没收违约方保证金
        if (isRemote && currentStatus == TradeRequest.TradeStatus.DEPOSIT_PAID) {
            User violator = user;
            User receiver = isRequester ? tradeRequest.getTargetItem().getOwner() : tradeRequest.getRequester();
            
            // 没收违约方保证金给对方
            TradeDeposit violatorDeposit = tradeDepositRepository
                    .findByTradeRequestAndUser(tradeRequest, violator)
                    .orElse(null);
            
            if (violatorDeposit != null && violatorDeposit.getStatus() == TradeDeposit.DepositStatus.FROZEN) {
                walletService.forfeitDeposit(
                        violator, receiver,
                        violatorDeposit.getPointsAmount(),
                        violatorDeposit.getCashAmount(),
                        tradeRequest.getId()
                );
                violatorDeposit.setStatus(TradeDeposit.DepositStatus.FORFEITED);
                tradeDepositRepository.save(violatorDeposit);
            }
            
            // 退还对方保证金
            TradeDeposit receiverDeposit = tradeDepositRepository
                    .findByTradeRequestAndUser(tradeRequest, receiver)
                    .orElse(null);
            
            if (receiverDeposit != null && receiverDeposit.getStatus() == TradeDeposit.DepositStatus.FROZEN) {
                walletService.unfreezeDeposit(
                        receiver,
                        receiverDeposit.getPointsAmount(),
                        receiverDeposit.getCashAmount(),
                        tradeRequest.getId()
                );
                receiverDeposit.setStatus(TradeDeposit.DepositStatus.REFUNDED);
                tradeDepositRepository.save(receiverDeposit);
            }
            
            // 扣信用分
            creditService.onTradeCancel(violator, tradeRequest.getId());
        } else if (isRemote && currentStatus == TradeRequest.TradeStatus.ACCEPTED) {
            // 已接受但未支付保证金，退还已支付的保证金
            for (TradeDeposit deposit : tradeDepositRepository.findByTradeRequest(tradeRequest)) {
                if (deposit.getStatus() == TradeDeposit.DepositStatus.FROZEN) {
                    walletService.unfreezeDeposit(
                            deposit.getUser(),
                            deposit.getPointsAmount(),
                            deposit.getCashAmount(),
                            tradeRequest.getId()
                    );
                    deposit.setStatus(TradeDeposit.DepositStatus.REFUNDED);
                    tradeDepositRepository.save(deposit);
                }
            }
        }
        
        // 通知对方
        User cancelledUser = isRequester ? 
            tradeRequest.getTargetItem().getOwner() : 
            tradeRequest.getRequester();
        String cancellerName = user.getNickname() != null ? user.getNickname() : user.getUsername();
        
        Notification notification = new Notification();
        notification.setUser(cancelledUser);
        notification.setType(Notification.NotificationType.TRADE);
        notification.setTitle("交换已取消");
        notification.setContent(cancellerName + " 取消了与您的交换请求");
        notification.setRelatedId(tradeRequest.getId());
        notificationRepository.save(notification);
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
    
    /**
     * 计算保证金
     */
    public TradeDto.DepositCalculation calculateDeposit(BigDecimal estimatedValue, User user) {
        double ratio = creditService.getDepositRatio(user);
        BigDecimal depositAmount = estimatedValue.multiply(BigDecimal.valueOf(ratio));
        
        UserWallet wallet = walletService.getOrCreateWallet(user);
        int availablePoints = wallet.getPoints() - wallet.getFrozenPoints();
        BigDecimal availableCash = wallet.getBalance().subtract(wallet.getFrozenBalance());
        
        // 100积分 = 1元
        int pointsNeededTotal = depositAmount.multiply(BigDecimal.valueOf(100)).intValue();
        int pointsNeeded = Math.min(availablePoints, pointsNeededTotal);
        BigDecimal cashNeeded = depositAmount.subtract(BigDecimal.valueOf(pointsNeeded).divide(BigDecimal.valueOf(100)));
        
        boolean canAfford = cashNeeded.compareTo(availableCash) <= 0;
        
        TradeDto.DepositCalculation calc = new TradeDto.DepositCalculation();
        calc.setTotalAmount(depositAmount);
        calc.setRatio(ratio);
        calc.setPointsNeeded(pointsNeeded);
        calc.setCashNeeded(cashNeeded);
        calc.setCanAfford(canAfford);
        
        return calc;
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
        response.setTradeMode(tradeRequest.getTradeMode());
        response.setEstimatedValue(tradeRequest.getEstimatedValue());
        response.setRequesterTrackingNo(tradeRequest.getRequesterTrackingNo());
        response.setTargetTrackingNo(tradeRequest.getTargetTrackingNo());
        response.setRequesterShippedAt(tradeRequest.getRequesterShippedAt());
        response.setTargetShippedAt(tradeRequest.getTargetShippedAt());
        response.setRequesterDepositPaid(tradeRequest.getRequesterDepositPaid());
        response.setTargetDepositPaid(tradeRequest.getTargetDepositPaid());
        response.setCreatedAt(tradeRequest.getCreatedAt());
        return response;
    }
}
