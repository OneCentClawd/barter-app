package com.barter.service;

import com.barter.entity.User;
import com.barter.entity.UserWallet;
import com.barter.entity.WalletTransaction;
import com.barter.repository.UserWalletRepository;
import com.barter.repository.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final UserWalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;

    // 签到积分：第1天1分，第2天2分...最高7分封顶
    // 连签积分不封顶

    /**
     * 获取或创建钱包
     */
    @Transactional
    public UserWallet getOrCreateWallet(User user) {
        return walletRepository.findByUser(user)
                .orElseGet(() -> {
                    UserWallet wallet = new UserWallet();
                    wallet.setUser(user);
                    return walletRepository.save(wallet);
                });
    }

    /**
     * 签到
     */
    @Transactional
    public WalletTransaction signIn(User user) {
        UserWallet wallet = getOrCreateWallet(user);
        LocalDate today = LocalDate.now();
        LocalDate lastSignIn = wallet.getLastSignInDate();
        
        // 检查今天是否已签到
        if (lastSignIn != null && lastSignIn.equals(today)) {
            throw new RuntimeException("今天已经签到过了");
        }
        
        // 计算连签天数
        int streak = wallet.getSignInStreak();
        if (lastSignIn != null && lastSignIn.equals(today.minusDays(1))) {
            // 连续签到
            streak = streak + 1;
        } else {
            // 断签，重新计算
            streak = 1;
        }
        
        // 计算积分：第N天得N分，最高7分
        int points = streak;
        
        wallet.setPoints(wallet.getPoints() + points);
        wallet.setSignInStreak(streak);
        wallet.setLastSignInDate(today);
        wallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(wallet);

        // 记录流水
        WalletTransaction transaction = new WalletTransaction();
        transaction.setUser(user);
        transaction.setType(WalletTransaction.TransactionType.SIGN_IN);
        transaction.setPointsChange(points);
        transaction.setPointsAfter(wallet.getPoints());
        transaction.setBalanceAfter(wallet.getBalance());
        transaction.setDescription("连续签到第" + streak + "天，获得" + points + "积分");
        return transactionRepository.save(transaction);
    }
    
    /**
     * 获取今日签到信息
     */
    public SignInInfo getSignInInfo(User user) {
        UserWallet wallet = getOrCreateWallet(user);
        LocalDate today = LocalDate.now();
        LocalDate lastSignIn = wallet.getLastSignInDate();
        
        boolean signedToday = lastSignIn != null && lastSignIn.equals(today);
        int streak = wallet.getSignInStreak();
        
        // 如果昨天没签到且今天也没签，连签要重置
        if (!signedToday && (lastSignIn == null || !lastSignIn.equals(today.minusDays(1)))) {
            streak = 0;
        }
        
        // 下次签到能得多少分
        int nextPoints = signedToday ? 0 : streak + 1;
        
        return new SignInInfo(signedToday, streak, nextPoints);
    }
    
    public record SignInInfo(boolean signedToday, int streak, int nextPoints) {}

    /**
     * 充值
     */
    @Transactional
    public WalletTransaction recharge(User user, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("充值金额必须大于0");
        }

        UserWallet wallet = getOrCreateWallet(user);
        wallet.setBalance(wallet.getBalance().add(amount));
        wallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(wallet);

        WalletTransaction transaction = new WalletTransaction();
        transaction.setUser(user);
        transaction.setType(WalletTransaction.TransactionType.RECHARGE);
        transaction.setBalanceChange(amount);
        transaction.setPointsAfter(wallet.getPoints());
        transaction.setBalanceAfter(wallet.getBalance());
        transaction.setDescription("充值 " + amount + " 元");
        return transactionRepository.save(transaction);
    }

    /**
     * 冻结保证金
     */
    @Transactional
    public void freezeDeposit(User user, int points, BigDecimal cash, Long tradeId) {
        UserWallet wallet = getOrCreateWallet(user);

        // 检查余额
        int availablePoints = wallet.getPoints() - wallet.getFrozenPoints();
        BigDecimal availableCash = wallet.getBalance().subtract(wallet.getFrozenBalance());

        if (points > availablePoints) {
            throw new RuntimeException("积分余额不足");
        }
        if (cash.compareTo(availableCash) > 0) {
            throw new RuntimeException("现金余额不足");
        }

        wallet.setFrozenPoints(wallet.getFrozenPoints() + points);
        wallet.setFrozenBalance(wallet.getFrozenBalance().add(cash));
        wallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(wallet);

        // 记录流水
        WalletTransaction transaction = new WalletTransaction();
        transaction.setUser(user);
        transaction.setType(WalletTransaction.TransactionType.DEPOSIT_FREEZE);
        transaction.setPointsChange(-points);
        transaction.setBalanceChange(cash.negate());
        transaction.setPointsAfter(wallet.getPoints() - wallet.getFrozenPoints());
        transaction.setBalanceAfter(wallet.getBalance().subtract(wallet.getFrozenBalance()));
        transaction.setDescription("交易保证金冻结");
        transaction.setRelatedId(tradeId);
        transactionRepository.save(transaction);
    }

    /**
     * 解冻保证金（正常完成交易）
     */
    @Transactional
    public void unfreezeDeposit(User user, int points, BigDecimal cash, Long tradeId) {
        UserWallet wallet = getOrCreateWallet(user);

        wallet.setFrozenPoints(Math.max(0, wallet.getFrozenPoints() - points));
        wallet.setFrozenBalance(wallet.getFrozenBalance().subtract(cash).max(BigDecimal.ZERO));
        wallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(wallet);

        WalletTransaction transaction = new WalletTransaction();
        transaction.setUser(user);
        transaction.setType(WalletTransaction.TransactionType.DEPOSIT_UNFREEZE);
        transaction.setPointsChange(points);
        transaction.setBalanceChange(cash);
        transaction.setPointsAfter(wallet.getPoints() - wallet.getFrozenPoints());
        transaction.setBalanceAfter(wallet.getBalance().subtract(wallet.getFrozenBalance()));
        transaction.setDescription("交易保证金退还");
        transaction.setRelatedId(tradeId);
        transactionRepository.save(transaction);
    }

    /**
     * 没收保证金（违约）
     */
    @Transactional
    public void forfeitDeposit(User violator, User receiver, int points, BigDecimal cash, Long tradeId) {
        UserWallet violatorWallet = getOrCreateWallet(violator);
        UserWallet receiverWallet = getOrCreateWallet(receiver);

        // 从违约方扣除
        violatorWallet.setPoints(Math.max(0, violatorWallet.getPoints() - points));
        violatorWallet.setFrozenPoints(Math.max(0, violatorWallet.getFrozenPoints() - points));
        violatorWallet.setBalance(violatorWallet.getBalance().subtract(cash).max(BigDecimal.ZERO));
        violatorWallet.setFrozenBalance(violatorWallet.getFrozenBalance().subtract(cash).max(BigDecimal.ZERO));
        violatorWallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(violatorWallet);

        // 转给对方
        receiverWallet.setPoints(receiverWallet.getPoints() + points);
        receiverWallet.setBalance(receiverWallet.getBalance().add(cash));
        receiverWallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(receiverWallet);

        // 记录违约方流水
        WalletTransaction violatorTx = new WalletTransaction();
        violatorTx.setUser(violator);
        violatorTx.setType(WalletTransaction.TransactionType.DEPOSIT_FORFEIT);
        violatorTx.setPointsChange(-points);
        violatorTx.setBalanceChange(cash.negate());
        violatorTx.setPointsAfter(violatorWallet.getPoints() - violatorWallet.getFrozenPoints());
        violatorTx.setBalanceAfter(violatorWallet.getBalance().subtract(violatorWallet.getFrozenBalance()));
        violatorTx.setDescription("违约保证金没收");
        violatorTx.setRelatedId(tradeId);
        transactionRepository.save(violatorTx);

        // 记录接收方流水
        WalletTransaction receiverTx = new WalletTransaction();
        receiverTx.setUser(receiver);
        receiverTx.setType(WalletTransaction.TransactionType.DEPOSIT_RECEIVE);
        receiverTx.setPointsChange(points);
        receiverTx.setBalanceChange(cash);
        receiverTx.setPointsAfter(receiverWallet.getPoints());
        receiverTx.setBalanceAfter(receiverWallet.getBalance());
        receiverTx.setDescription("收到违约赔偿");
        receiverTx.setRelatedId(tradeId);
        transactionRepository.save(receiverTx);
    }

    /**
     * 推荐奖励
     */
    @Transactional
    public void addReferralReward(User referrer, User newUser, int points) {
        UserWallet wallet = getOrCreateWallet(referrer);
        wallet.setPoints(wallet.getPoints() + points);
        wallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(wallet);

        WalletTransaction transaction = new WalletTransaction();
        transaction.setUser(referrer);
        transaction.setType(WalletTransaction.TransactionType.INVITE_REWARD);
        transaction.setPointsChange(points);
        transaction.setPointsAfter(wallet.getPoints());
        transaction.setBalanceAfter(wallet.getBalance());
        transaction.setDescription("邀请新用户 " + newUser.getUsername() + " 注册奖励");
        transaction.setRelatedId(newUser.getId());
        transactionRepository.save(transaction);
    }

    /**
     * 获取钱包流水
     */
    public Page<WalletTransaction> getTransactions(User user, Pageable pageable) {
        return transactionRepository.findByUserOrderByCreatedAtDesc(user, pageable);
    }
}
