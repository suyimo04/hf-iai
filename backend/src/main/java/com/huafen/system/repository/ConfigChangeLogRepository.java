package com.huafen.system.repository;

import com.huafen.system.entity.ConfigChangeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ConfigChangeLogRepository extends JpaRepository<ConfigChangeLog, Long> {

    List<ConfigChangeLog> findByConfigGroupAndConfigKey(String configGroup, String configKey);

    List<ConfigChangeLog> findByConfigGroup(String configGroup);

    List<ConfigChangeLog> findByChangedBy(Long changedBy);

    List<ConfigChangeLog> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    List<ConfigChangeLog> findByConfigGroupOrderByCreatedAtDesc(String configGroup);

    List<ConfigChangeLog> findByConfigKeyOrderByCreatedAtDesc(String configKey);
}
