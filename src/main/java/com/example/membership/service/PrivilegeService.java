package com.example.membership.service;

import com.example.membership.entity.Prop;
import com.example.membership.repository.PropRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 道具/特权服务(对应 Go 的 UserPropService)。
 *
 * 核心规则:
 * 1. 防超扣:消耗数量 = min(number, 剩余),余额不足只消耗剩余部分(对应 Go 的 UserProp.Use())。
 * 2. 并发安全:SELECT ... WHERE total > used FOR UPDATE,行级悲观锁。
 */
@Service
@RequiredArgsConstructor
public class PrivilegeService {

    private final PropRepository propRepository;

    /** 增加道具(发特权)。 */
    @Transactional
    public long addProp(Long userId, String propType, long number) {
        if (number <= 0) {
            throw new IllegalArgumentException("number 必须大于 0");
        }
        Prop prop = propRepository.findByUserIdAndPropType(userId, propType).orElseGet(() -> {
            Prop p = new Prop();
            p.setUserId(userId);
            p.setPropType(propType);
            return p;
        });
        prop.setTotal(prop.getTotal() + number);
        propRepository.save(prop);
        return prop.getLeft();
    }

    /** 消耗道具(使用特权),防超扣:余额不足只消耗剩余,并返回实际消耗数量。 */
    @Transactional
    public long consumePrivilege(Long userId, String propType, long number) {
        if (number <= 0) {
            throw new IllegalArgumentException("number 必须大于 0");
        }
        Optional<Prop> opt = propRepository.findAvailableForUpdate(userId, propType);
        if (opt.isEmpty()) {
            return 0; // 无剩余可用
        }
        Prop prop = opt.get();
        long left = prop.getLeft();
        long consume = Math.min(number, left); // 防超扣:最多消耗剩余部分
        prop.setUsed(prop.getUsed() + consume);
        propRepository.save(prop);
        return consume;
    }

    /** 特权校验:是否还有可用数量。 */
    @Transactional(readOnly = true)
    public boolean checkPrivilege(Long userId, String propType) {
        return propRepository.findByUserIdAndPropType(userId, propType)
                .map(p -> p.getLeft() > 0)
                .orElse(false);
    }
}
