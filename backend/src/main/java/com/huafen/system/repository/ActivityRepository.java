package com.huafen.system.repository;

import com.huafen.system.entity.Activity;
import com.huafen.system.entity.enums.ActivityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long>, JpaSpecificationExecutor<Activity> {

    List<Activity> findByStatus(ActivityStatus status);

    List<Activity> findByCreatedBy(Long userId);

    @Query("SELECT a FROM Activity a WHERE a.status = 'PUBLISHED' AND a.startTime > CURRENT_TIMESTAMP ORDER BY a.startTime ASC")
    List<Activity> findUpcoming();

    long countByStatus(ActivityStatus status);

    long countByStatusIn(List<ActivityStatus> statuses);
}
