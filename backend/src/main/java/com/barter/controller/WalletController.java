package com.barter.controller;

import com.barter.dto.ApiResponse;
import com.barter.dto.WalletDto;
import com.barter.entity.User;
import com.barter.entity.UserWallet;
import com.barter.entity.WalletTransaction;
import com.barter.entity.CreditRecord;
import com.barter.repository.CreditRecordRepository;
import com.barter.service.CreditService;
import com.barter.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;
    private final CreditService creditService;
    private final CreditRecordRepository creditRecordRepository;

    /**
     * 获取钱包信息
     */
    @GetMapping
    public ResponseEntity<ApiResponse<WalletDto.WalletResponse>> getWallet(
            @AuthenticationPrincipal User user) {
        UserWallet wallet = walletService.getOrCreateWallet(user);
        WalletService.SignInInfo signInInfo = walletService.getSignInInfo(user);

        WalletDto.WalletResponse response = new WalletDto.WalletResponse();
        response.setPoints(wallet.getPoints());
        response.setBalance(wallet.getBalance());
        response.setFrozenPoints(wallet.getFrozenPoints());
        response.setFrozenBalance(wallet.getFrozenBalance());
        response.setAvailablePoints(wallet.getPoints() - wallet.getFrozenPoints());
        response.setAvailableBalance(wallet.getBalance().subtract(wallet.getFrozenBalance()));
        response.setSignedToday(signInInfo.signedToday());
        response.setSignInStreak(signInInfo.streak());
        response.setNextSignInPoints(signInInfo.nextPoints());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 签到
     */
    @PostMapping("/sign-in")
    public ResponseEntity<ApiResponse<WalletDto.TransactionResponse>> signIn(
            @AuthenticationPrincipal User user) {
        WalletTransaction transaction = walletService.signIn(user);

        WalletDto.TransactionResponse response = new WalletDto.TransactionResponse();
        response.setId(transaction.getId());
        response.setType(transaction.getType());
        response.setPointsChange(transaction.getPointsChange());
        response.setBalanceChange(transaction.getBalanceChange());
        response.setDescription(transaction.getDescription());
        response.setCreatedAt(transaction.getCreatedAt());

        return ResponseEntity.ok(ApiResponse.success(transaction.getDescription(), response));
    }

    /**
     * 充值（测试用，实际需要对接支付）
     */
    @PostMapping("/recharge")
    public ResponseEntity<ApiResponse<WalletDto.TransactionResponse>> recharge(
            @AuthenticationPrincipal User user,
            @RequestBody WalletDto.RechargeRequest request) {
        if (request.getAmount() == null || request.getAmount().doubleValue() <= 0) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("充值金额必须大于0"));
        }
        
        WalletTransaction transaction = walletService.recharge(user, request.getAmount());
        
        WalletDto.TransactionResponse response = new WalletDto.TransactionResponse();
        response.setId(transaction.getId());
        response.setType(transaction.getType());
        response.setPointsChange(transaction.getPointsChange());
        response.setBalanceChange(transaction.getBalanceChange());
        response.setDescription(transaction.getDescription());
        response.setCreatedAt(transaction.getCreatedAt());

        return ResponseEntity.ok(ApiResponse.success("充值成功", response));
    }

    /**
     * 获取钱包流水
     */
    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<Page<WalletDto.TransactionResponse>>> getTransactions(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<WalletTransaction> transactions = walletService.getTransactions(user, PageRequest.of(page, size));

        Page<WalletDto.TransactionResponse> response = transactions.map(tx -> {
            WalletDto.TransactionResponse dto = new WalletDto.TransactionResponse();
            dto.setId(tx.getId());
            dto.setType(tx.getType());
            dto.setPointsChange(tx.getPointsChange());
            dto.setBalanceChange(tx.getBalanceChange());
            dto.setDescription(tx.getDescription());
            dto.setCreatedAt(tx.getCreatedAt());
            return dto;
        });

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 获取信用分信息
     */
    @GetMapping("/credit")
    public ResponseEntity<ApiResponse<WalletDto.CreditResponse>> getCredit(
            @AuthenticationPrincipal User user) {
        WalletDto.CreditResponse response = new WalletDto.CreditResponse();
        response.setCreditScore(user.getCreditScore() != null ? user.getCreditScore() : 100);
        
        CreditService.CreditLevel level = creditService.getCreditLevel(user);
        response.setLevel(level);
        response.setLevelName(level.getName());
        response.setDepositRatio(creditService.getDepositRatio(user));
        response.setCanRemoteTrade(creditService.canRemoteTrade(user));

        // 计算下一等级所需分数
        switch (level) {
            case NEWBIE:
                response.setNextLevelScore(CreditService.LEVEL_NORMAL);
                break;
            case NORMAL:
                response.setNextLevelScore(CreditService.LEVEL_GOOD);
                break;
            case GOOD:
                response.setNextLevelScore(CreditService.LEVEL_EXCELLENT);
                break;
            case EXCELLENT:
                response.setNextLevelScore(null);  // 已是最高
                break;
        }

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 获取信用分记录
     */
    @GetMapping("/credit/records")
    public ResponseEntity<ApiResponse<Page<WalletDto.CreditRecordResponse>>> getCreditRecords(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<CreditRecord> records = creditRecordRepository.findByUserOrderByCreatedAtDesc(user, PageRequest.of(page, size));

        Page<WalletDto.CreditRecordResponse> response = records.map(record -> {
            WalletDto.CreditRecordResponse dto = new WalletDto.CreditRecordResponse();
            dto.setId(record.getId());
            dto.setType(record.getType());
            dto.setScoreChange(record.getScoreChange());
            dto.setScoreAfter(record.getScoreAfter());
            dto.setDescription(record.getDescription());
            dto.setCreatedAt(record.getCreatedAt());
            return dto;
        });

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
