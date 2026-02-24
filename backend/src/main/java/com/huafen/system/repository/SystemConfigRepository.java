package com.huafen.system.repository;

import com.huafen.system.entity.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SystemConfigRepository extends JpaRepository<SystemConfig, Long> {

    Optional<SystemConfig> findByConfigKey(String key);

    Optional<SystemConfig> findByConfigGroupAndConfigKey(String configGroup, String configKey);

    List<SystemConfig> findByConfigGroup(String configGroup);
}
