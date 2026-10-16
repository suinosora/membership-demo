package com.example.membership.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 探币账户余额(对应 Go 钱包服务的余额)。
 * 一个用户一行。
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "coin_accounts",
        uniqueConstraints = @UniqueConstraint(name = "uk_coin_user", columnNames = "user_id"))
public class CoinAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private long balance;
}
