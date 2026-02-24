package com.huafen.system.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 月度绩效实体
 * 记录用户每月的绩效数据
 */
@Entity
@Table(name = "monthly_performances", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "period"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyPerformance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 7, nullable = false)
    private String period; // 格式: YYYY-MM

    @Column(name = "total_points")
    private Integer totalPoints;

    @Column(name = "checkin_count")
    private Integer checkinCount;

    @Column(name = "consecutive_low_months")
    private Integer consecutiveLowMonths;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
