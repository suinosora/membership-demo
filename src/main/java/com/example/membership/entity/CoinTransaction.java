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
 * 探币流水,用于幂等(对应 Go 的 coin consume record / wallet EventId)。
 * eventId(订单号)唯一:同一事件重复调用不会重复加/扣币。
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "coin_transactions",
        uniqueConstraints = @UniqueConstraint(name = "uk_coin_event_id", columnNames = "event_id"))
public class CoinTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 幂等键:上游事件/订单号。 */
    @Column(name = "event_id", nullable = false)
    private String eventId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 变动金额:正 = 加币,负 = 扣币。 */
    @Column(nullable = false)
    private long amount;

    @Column(name = "created_time")
    private LocalDateTime createdTime = LocalDateTime.now();
}
