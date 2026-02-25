package com.huafen.system.repository;

import com.huafen.system.entity.Interview;
import com.huafen.system.entity.enums.InterviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, Long>, JpaSpecificationExecutor<Interview> {

    List<Interview> findByUser_Id(Long userId);

    Optional<Interview> findByApplication_Id(Long applicationId);

    List<Interview> findByStatus(InterviewStatus status);
}
