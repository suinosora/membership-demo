package com.example.membership.service;

import com.example.membership.entity.Membership;
import com.example.membership.entity.MembershipRecord;
import com.example.membership.repository.MembershipRecordRepository;
import com.example.membership.repository.MembershipRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 会员服务(对应 Go 的 UserMembershipService)。
 *
 * 核心规则:
 * 1. 幂等:originalId(订单号)唯一,同一订单重复调用不会重复开通。
 * 2. 续期/过期重开:beginTime = max(now, 当前 endTime)。
 *    - 会员还有效 → 从当前到期时间顺延(续期)
 *    - 无记录/已过期 → 从现在重新开始
 */
@Service
@RequiredArgsConstructor
public class MembershipService {

    private final MembershipRepository membershipRepository;
    private final MembershipRecordRepository recordRepository;

    /** 会员开通/续费,幂等。 */
    @Transactional
    public Membership openMembership(Long userId, String type, long durationSeconds, String originalId) {
        if (durationSeconds <= 0) {
            throw new IllegalArgumentException("durationSeconds 必须大于 0");
        }

        // 幂等快速路径:该订单已处理过,直接返回当前状态
        if (recordRepository.existsByOriginalId(originalId)) {
            return membershipRepository.findByUserIdAndMembershipType(userId, type).orElse(null);
        }

        // 悲观锁会员行(续期场景行已存在;首次开通锁不到,靠幂等键 + 唯一约束兜底)
        Optional<Membership> existing = membershipRepository.findForUpdate(userId, type);

        LocalDateTime now = LocalDateTime.now();
        // 核心规则:begin = max(now, 当前 endTime)
        // 会员还有效 → 从当前到期时间顺延(续期);无记录/已过期 → 从现在重新开始
        LocalDateTime begin = existing.map(Membership::getEndTime).orElse(null);
        if (begin == null || begin.isBefore(now)) {
            begin = now;
        }

        Membership membership = existing.orElseGet(() -> {
            Membership m = new Membership();
            m.setUserId(userId);
            m.setMembershipType(type);
            return m;
        });
        membership.setBeginTime(begin);
        membership.setEndTime(begin.plusSeconds(durationSeconds));

        // 保存开通流水(originalId 唯一,用于幂等)
        MembershipRecord record = new MembershipRecord();
        record.setOriginalId(originalId);
        record.setUserId(userId);
        record.setMembershipType(type);
        record.setDurationSeconds(durationSeconds);
        recordRepository.save(record);

        return membershipRepository.save(membership);
    }

    /** 会员状态查询(对应 Go 的 GetMembershipByUserIDAndType)。 */
    @Transactional(readOnly = true)
    public Membership queryMembership(Long userId, String type) {
        return membershipRepository.findByUserIdAndMembershipType(userId, type).orElse(null);
    }
}
