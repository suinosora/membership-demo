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
 * 用户道具/特权(对应 Go 的 models.UserProp)。
 * total = 累计获得数量,used = 已消耗数量,剩余 = total - used。
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "props",
        uniqueConstraints = @UniqueConstraint(name = "uk_prop_user_type", columnNames = {"user_id", "prop_type"}))
public class Prop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "prop_type", nullable = false)
    private String propType;

    @Column(nullable = false)
    private long total;

    @Column(nullable = false)
    private long used;

    /** 剩余可用数量(对应 Go 的 GetLeft())。 */
    public long getLeft() {
        return total - used;
    }
}
