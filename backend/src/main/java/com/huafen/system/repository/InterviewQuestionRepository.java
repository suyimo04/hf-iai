package com.huafen.system.repository;

import com.huafen.system.entity.InterviewQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterviewQuestionRepository extends JpaRepository<InterviewQuestion, Long>, JpaSpecificationExecutor<InterviewQuestion> {

    List<InterviewQuestion> findByEnabledTrueOrderBySortOrder();

    List<InterviewQuestion> findByCategory(String category);

    List<InterviewQuestion> findByCategoryAndEnabledTrue(String category);
}
