package com.huafen.system.repository;

import com.huafen.system.entity.MemberFlowLog;
import com.huafen.system.entity.enums.FlowStatus;
import com.huafen.system.entity.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberFlowLogRepository extends JpaRepository<MemberFlowLog, Long>, JpaSpecificationExecutor<MemberFlowLog> {

    /**
     * 查找用户的待审批流转记录
     */
    Optional<MemberFlowLog> findByUser_IdAndStatus(Long userId, FlowStatus status);

    /**
     * 查找用户的所有流转记录
     */
    List<MemberFlowLog> findByUser_IdOrderByCreatedAtDesc(Long userId);

    /**
     * 查找指定目标角色的待审批记录
     */
    List<MemberFlowLog> findByToRoleAndStatus(Role toRole, FlowStatus status);

    /**
     * 分页查询所有流转记录
     */
    Page<MemberFlowLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * 检查用户是否有待审批的流转申请
     */
    boolean existsByUser_IdAndStatus(Long userId, FlowStatus status);
}
