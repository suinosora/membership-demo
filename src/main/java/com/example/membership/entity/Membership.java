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
 * 用户会员状态(对应 Go 的 models.UserMembership)。
 * 一个用户 + 一个会员类型 = 一行。
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "memberships",
        uniqueConstraints = @UniqueConstraint(name = "uk_member_user_type", columnNames = {"user_id", "membership_type"}))
public class Membership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "membership_type", nullable = false)
    private String membershipType;

    @Column(name = "begin_time")
    private LocalDateTime beginTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    /** 是否已过期(对应 Go 的 Expired())。 */
    public boolean expired() {
        return endTime == null || endTime.isBefore(LocalDateTime.now());
    }
}
