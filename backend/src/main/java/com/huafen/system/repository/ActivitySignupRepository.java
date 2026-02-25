package com.huafen.system.repository;

import com.huafen.system.entity.ActivitySignup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ActivitySignupRepository extends JpaRepository<ActivitySignup, Long>, JpaSpecificationExecutor<ActivitySignup> {

    List<ActivitySignup> findByActivity_Id(Long activityId);

    List<ActivitySignup> findByUser_Id(Long userId);

    Optional<ActivitySignup> findByActivity_IdAndUser_Id(Long activityId, Long userId);

    long countByActivity_Id(Long activityId);
}
