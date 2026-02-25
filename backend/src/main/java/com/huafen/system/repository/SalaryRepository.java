package com.huafen.system.repository;

import com.huafen.system.entity.Salary;
import com.huafen.system.entity.enums.SalaryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalaryRepository extends JpaRepository<Salary, Long>, JpaSpecificationExecutor<Salary> {

    List<Salary> findByUser_Id(Long userId);

    List<Salary> findByPeriod(String period);

    List<Salary> findByPeriodAndStatus(String period, SalaryStatus status);
}
