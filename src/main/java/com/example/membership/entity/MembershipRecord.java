package com.example.membership.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 会员开通流水,用于幂等(对应 Go 的 UserMembershipRecord)。
 * originalId(订单号)唯一,同一订单重复调用不会重复开通。
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "membership_records",
        uniqueConstraints = @UniqueConstraint(name = "uk_member_original_id", columnNames = "original_id"))
public class MembershipRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 幂等键:上游订单号。 */
    @Column(name = "original_id", nullable = false)
    private String originalId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "membership_type", nullable = false)
    private String membershipType;

    /** 会员时长(秒)。 */
    @Column(name = "duration_seconds", nullable = false)
    private long durationSeconds;

    @Column(name = "created_time")
    private LocalDateTime createdTime = LocalDateTime.now();
}
