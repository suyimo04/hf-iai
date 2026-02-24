package com.huafen.system.entity;

import com.huafen.system.entity.enums.SalaryStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 薪酬记录实体
 */
@Entity
@Table(name = "salaries")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Salary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 20)
    private String period;

    @Column(name = "base_points")
    private Integer basePoints;

    @Column(name = "bonus_points")
    private Integer bonusPoints;

    @Column
    private Integer deduction;

    @Column(name = "total_points")
    private Integer totalPoints;

    @Column
    private Integer coins;

    @Column(precision = 10, scale = 2)
    private BigDecimal salary;

    @Column(columnDefinition = "TEXT")
    private String remark;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private SalaryStatus status;

    @Column(name = "checkin_count")
    private Integer checkinCount;

    @Column(name = "checkin_points")
    private Integer checkinPoints;

    @Column(name = "checkin_coins")
    private Integer checkinCoins;

    @Column(name = "pool_allocation", precision = 10, scale = 2)
    private BigDecimal poolAllocation;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
