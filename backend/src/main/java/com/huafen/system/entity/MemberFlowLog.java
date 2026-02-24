package com.huafen.system.entity;

import com.huafen.system.entity.enums.Role;
import com.huafen.system.entity.enums.TriggerType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 成员流动日志实体
 * 记录用户角色变更历史
 */
@Entity
@Table(name = "member_flow_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberFlowLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_role", length = 20)
    private Role fromRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_role", length = 20)
    private Role toRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_type", length = 20, nullable = false)
    private TriggerType triggerType;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
