package com.barter.service;

import com.barter.entity.CreditRecord;
import com.barter.entity.User;
import com.barter.repository.CreditRecordRepository;
import com.barter.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreditService {

    private final UserRepository userRepository;
    private final CreditRecordRepository creditRecordRepository;

    // 信用分变化值
    public static final int TRADE_COMPLETE = 5;      // 完成交易
    public static final int GOOD_REVIEW = 3;         // 获得好评
    public static final int ON_TIME_SHIP = 1;        // 按时发货
    public static final int TRADE_CANCEL = -10;      // 取消交易
    public static final int LATE_SHIP = -25;         // 超时不发货
    public static final int BAD_REVIEW = -8;         // 获得差评
    public static final int REPORT_CONFIRMED = -40;  // 举报成立
    public static final int DEPOSIT_DEFAULT = -50;   // 保证金违约

    // 信用等级阈值
    public static final int LEVEL_NORMAL = 60;       // 普通
    public static final int LEVEL_GOOD = 151;        // 良好
    public static final int LEVEL_EXCELLENT = 301;   // 优秀

    /**
     * 获取信用等级
     */
    public CreditLevel getCreditLevel(User user) {
        int score = user.getCreditScore() != null ? user.getCreditScore() : 100;
        if (score >= LEVEL_EXCELLENT) {
            return CreditLevel.EXCELLENT;
        } else if (score >= LEVEL_GOOD) {
            return CreditLevel.GOOD;
        } else if (score >= LEVEL_NORMAL) {
            return CreditLevel.NORMAL;
        } else {
            return CreditLevel.NEWBIE;
        }
    }

    /**
     * 获取保证金比例
     */
    public double getDepositRatio(User user) {
        CreditLevel level = getCreditLevel(user);
        switch (level) {
            case EXCELLENT:
                return 0.0;   // 免保证金
            case GOOD:
                return 0.5;   // 50%
            case NORMAL:
                return 1.0;   // 100%
            default:
                return 1.0;   // 新手不能远程
        }
    }

    /**
     * 是否可以远程交易
     */
    public boolean canRemoteTrade(User user) {
        return getCreditLevel(user) != CreditLevel.NEWBIE;
    }

    /**
     * 增加信用分
     */
    @Transactional
    public void addCredit(User user, CreditRecord.CreditChangeType type, int change, String description, Long relatedId) {
        int currentScore = user.getCreditScore() != null ? user.getCreditScore() : 100;
        int newScore = Math.max(0, currentScore + change);  // 不低于0

        user.setCreditScore(newScore);
        userRepository.save(user);

        // 记录变更
        CreditRecord record = new CreditRecord();
        record.setUser(user);
        record.setType(type);
        record.setScoreChange(change);
        record.setScoreAfter(newScore);
        record.setDescription(description);
        record.setRelatedId(relatedId);
        creditRecordRepository.save(record);
    }

    /**
     * 交易完成加分
     */
    @Transactional
    public void onTradeComplete(User user, Long tradeId) {
        addCredit(user, CreditRecord.CreditChangeType.TRADE_COMPLETE, TRADE_COMPLETE,
                "完成交易", tradeId);
    }

    /**
     * 获得好评加分
     */
    @Transactional
    public void onGoodReview(User user, Long tradeId) {
        addCredit(user, CreditRecord.CreditChangeType.GOOD_REVIEW, GOOD_REVIEW,
                "获得好评", tradeId);
    }

    /**
     * 按时发货加分
     */
    @Transactional
    public void onTimeShip(User user, Long tradeId) {
        addCredit(user, CreditRecord.CreditChangeType.ON_TIME_SHIP, ON_TIME_SHIP,
                "按时发货", tradeId);
    }

    /**
     * 取消交易扣分
     */
    @Transactional
    public void onTradeCancel(User user, Long tradeId) {
        addCredit(user, CreditRecord.CreditChangeType.TRADE_CANCEL, TRADE_CANCEL,
                "取消交易", tradeId);
    }

    /**
     * 超时不发货扣分
     */
    @Transactional
    public void onLateShip(User user, Long tradeId) {
        addCredit(user, CreditRecord.CreditChangeType.LATE_SHIP, LATE_SHIP,
                "超时未发货", tradeId);
    }

    /**
     * 获得差评扣分
     */
    @Transactional
    public void onBadReview(User user, Long tradeId) {
        addCredit(user, CreditRecord.CreditChangeType.BAD_REVIEW, BAD_REVIEW,
                "获得差评", tradeId);
    }

    public enum CreditLevel {
        NEWBIE("新手", 0),
        NORMAL("普通", 60),
        GOOD("良好", 151),
        EXCELLENT("优秀", 301);

        private final String name;
        private final int minScore;

        CreditLevel(String name, int minScore) {
            this.name = name;
            this.minScore = minScore;
        }

        public String getName() {
            return name;
        }

        public int getMinScore() {
            return minScore;
        }
    }
}
