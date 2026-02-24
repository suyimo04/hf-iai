package com.huafen.system.repository;

import com.huafen.system.entity.User;
import com.huafen.system.entity.enums.Role;
import com.huafen.system.entity.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    List<User> findByRole(Role role);

    List<User> findByStatus(UserStatus status);

    long countByRoleIn(List<Role> roles);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT FUNCTION('DATE', u.createdAt) as date, COUNT(u) as count FROM User u WHERE u.createdAt >= :startDate GROUP BY FUNCTION('DATE', u.createdAt) ORDER BY date")
    List<Object[]> countByDateRange(@Param("startDate") LocalDateTime startDate);
}
