package com.example.membership.service;

import com.example.membership.entity.CoinAccount;
import com.example.membership.entity.CoinTransaction;
import com.example.membership.repository.CoinAccountRepository;
import com.example.membership.repository.CoinTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 探币账户服务(对应 Go 钱包 AddCoin / 探币消费)。
 *
 * 核心规则:
 * 1. 幂等:eventId(订单号)唯一,同一事件重复调用不会重复加/扣币。
 * 2. 防超扣:扣币时余额不足直接拒绝,不允许扣成负数。
 * 3. 并发安全:SELECT ... FOR UPDATE,行级悲观锁。
 */
@Service
@RequiredArgsConstructor
public class CoinService {

    private final CoinAccountRepository accountRepository;
    private final CoinTransactionRepository transactionRepository;

    /** 加币,幂等。 */
    @Transactional
    public long addCoin(String eventId, Long userId, long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("amount 必须大于 0");
        }
        if (transactionRepository.existsByEventId(eventId)) {
            return balanceOf(userId); // 幂等:已处理过,返回当前余额
        }
        CoinAccount account = getOrCreateAccount(userId);
        account.setBalance(account.getBalance() + amount);
        accountRepository.save(account);
        saveTransaction(eventId, userId, amount);
        return account.getBalance();
    }

    /** 扣币,幂等 + 防超扣。 */
    @Transactional
    public long deductCoin(String eventId, Long userId, long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("amount 必须大于 0");
        }
        if (transactionRepository.existsByEventId(eventId)) {
            return balanceOf(userId); // 幂等:已处理过
        }
        CoinAccount account = getOrCreateAccount(userId);
        if (account.getBalance() < amount) {
            // 防超扣:余额不足直接拒绝
            throw new IllegalStateException(
                    "余额不足:当前 " + account.getBalance() + ",需要 " + amount);
        }
        account.setBalance(account.getBalance() - amount);
        accountRepository.save(account);
        saveTransaction(eventId, userId, -amount);
        return account.getBalance();
    }

    /** 查询余额。 */
    @Transactional(readOnly = true)
    public long balanceOf(Long userId) {
        return accountRepository.findByUserId(userId)
                .map(CoinAccount::getBalance)
                .orElse(0L);
    }

    private CoinAccount getOrCreateAccount(Long userId) {
        return accountRepository.findForUpdate(userId).orElseGet(() -> {
            CoinAccount account = new CoinAccount();
            account.setUserId(userId);
            account.setBalance(0L);
            return accountRepository.save(account);
        });
    }

    private void saveTransaction(String eventId, Long userId, long amount) {
        CoinTransaction tx = new CoinTransaction();
        tx.setEventId(eventId);
        tx.setUserId(userId);
        tx.setAmount(amount);
        transactionRepository.save(tx);
    }
}
