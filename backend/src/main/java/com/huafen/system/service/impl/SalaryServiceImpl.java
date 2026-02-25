package com.huafen.system.service.impl;

import com.huafen.system.common.ResultCode;
import com.huafen.system.dto.salary.*;
import com.huafen.system.entity.Salary;
import com.huafen.system.entity.User;
import com.huafen.system.entity.enums.SalaryStatus;
import com.huafen.system.entity.enums.UserStatus;
import com.huafen.system.exception.BusinessException;
import com.huafen.system.repository.SalaryRepository;
import com.huafen.system.repository.UserRepository;
import com.huafen.system.service.ConfigService;
import com.huafen.system.service.PointService;
import com.huafen.system.service.SalaryService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * 薪酬服务实现
 */
@Service
@RequiredArgsConstructor
public class SalaryServiceImpl implements SalaryService {

    private static final int DEFAULT_SALARY_POOL = 2000;
    private static final int MIN_SALARY = 200;
    private static final int MAX_SALARY = 400;
    private static final int MIN_MEMBERS = 5;

    private final SalaryRepository salaryRepository;
    private final UserRepository userRepository;
    private final ConfigService configService;
    private final PointService pointService;

    @Override
    public Page<SalaryDTO> getSalaries(SalaryQueryRequest request) {
        Specification<Salary> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request.getPeriod() != null && !request.getPeriod().isEmpty()) {
                predicates.add(cb.equal(root.get("period"), request.getPeriod()));
            }
            if (request.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), request.getStatus()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        PageRequest pageRequest = PageRequest.of(
                request.getPage(),
                request.getSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return salaryRepository.findAll(spec, pageRequest).map(SalaryDTO::fromEntity);
    }

    @Override
    public SalaryDTO getById(Long id) {
        Salary salary = salaryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "薪酬记录不存在"));
        return SalaryDTO.fromEntity(salary);
    }

    @Override
    @Transactional
    public SalaryDTO edit(SalaryEditRequest request) {
        Salary salary = salaryRepository.findById(request.getId())
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "薪酬记录不存在"));

        if (request.getBasePoints() != null) {
            salary.setBasePoints(request.getBasePoints());
        }
        if (request.getBonusPoints() != null) {
            salary.setBonusPoints(request.getBonusPoints());
        }
        if (request.getDeduction() != null) {
            salary.setDeduction(request.getDeduction());
        }
        if (request.getCoins() != null) {
            salary.setCoins(request.getCoins());
        }
        if (request.getRemark() != null) {
            salary.setRemark(request.getRemark());
        }

        // 重新计算总积分
        int totalPoints = (salary.getBasePoints() != null ? salary.getBasePoints() : 0)
                + (salary.getBonusPoints() != null ? salary.getBonusPoints() : 0)
                - (salary.getDeduction() != null ? salary.getDeduction() : 0);
        salary.setTotalPoints(totalPoints);

        Salary saved = salaryRepository.save(salary);
        return SalaryDTO.fromEntity(saved);
    }

    @Override
    public SalaryValidationResult validate(SalaryBatchSaveRequest request) {
        List<String> errors = new ArrayList<>();
        BigDecimal totalSalary = BigDecimal.ZERO;

        // 获取薪酬池总额
        int salaryPool = configService.getIntValue("salary_pool", DEFAULT_SALARY_POOL);

        // 获取该期间的所有薪酬记录
        List<Salary> salaries = salaryRepository.findByPeriod(request.getPeriod());

        if (salaries.size() < MIN_MEMBERS) {
            errors.add("至少需要" + MIN_MEMBERS + "名正式成员");
        }

        // 计算总积分
        int grandTotalPoints = 0;
        for (Salary s : salaries) {
            grandTotalPoints += (s.getTotalPoints() != null ? s.getTotalPoints() : 0);
        }

        if (grandTotalPoints == 0) {
            errors.add("总积分为0，无法计算薪酬");
            return SalaryValidationResult.builder()
                    .valid(false)
                    .errors(errors)
                    .totalSalary(BigDecimal.ZERO)
                    .build();
        }

        // 计算每人薪酬并校验
        for (Salary s : salaries) {
            int points = s.getTotalPoints() != null ? s.getTotalPoints() : 0;
            BigDecimal salary = BigDecimal.valueOf(points)
                    .multiply(BigDecimal.valueOf(salaryPool))
                    .divide(BigDecimal.valueOf(grandTotalPoints), 2, RoundingMode.HALF_UP);

            if (salary.compareTo(BigDecimal.valueOf(MIN_SALARY)) < 0) {
                errors.add(s.getUser().getNickname() + " 工资 " + salary + " 低于最低限额 " + MIN_SALARY);
            }
            if (salary.compareTo(BigDecimal.valueOf(MAX_SALARY)) > 0) {
                errors.add(s.getUser().getNickname() + " 工资 " + salary + " 超过最高限额 " + MAX_SALARY);
            }

            totalSalary = totalSalary.add(salary);
        }

        if (totalSalary.compareTo(BigDecimal.valueOf(salaryPool)) > 0) {
            errors.add("总薪酬 " + totalSalary + " 超过薪酬池 " + salaryPool);
        }

        return SalaryValidationResult.builder()
                .valid(errors.isEmpty())
                .errors(errors)
                .totalSalary(totalSalary)
                .build();
    }

    @Override
    @Transactional
    public List<SalaryDTO> batchSave(SalaryBatchSaveRequest request) {
        // 先校验
        SalaryValidationResult validation = validate(request);
        if (!validation.getValid()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, String.join("; ", validation.getErrors()));
        }

        // 获取薪酬池总额
        int salaryPool = configService.getIntValue("salary_pool", DEFAULT_SALARY_POOL);

        // 获取该期间的所有薪酬记录
        List<Salary> salaries = salaryRepository.findByPeriod(request.getPeriod());

        // 计算总积分
        int grandTotalPoints = 0;
        for (Salary s : salaries) {
            grandTotalPoints += (s.getTotalPoints() != null ? s.getTotalPoints() : 0);
        }

        // 更新每条记录的薪酬并确认
        List<SalaryDTO> result = new ArrayList<>();
        for (Salary s : salaries) {
            int points = s.getTotalPoints() != null ? s.getTotalPoints() : 0;
            BigDecimal salary = BigDecimal.valueOf(points)
                    .multiply(BigDecimal.valueOf(salaryPool))
                    .divide(BigDecimal.valueOf(grandTotalPoints), 2, RoundingMode.HALF_UP);

            s.setSalary(salary);
            s.setStatus(SalaryStatus.CONFIRMED);

            Salary saved = salaryRepository.save(s);
            result.add(SalaryDTO.fromEntity(saved));
        }

        return result;
    }

    @Override
    @Transactional
    public void generateMonthlySalary(String period) {
        // 检查该期间是否已有记录
        List<Salary> existing = salaryRepository.findByPeriod(period);
        if (!existing.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该期间已存在薪酬记录");
        }

        // 获取所有正式成员
        List<User> activeUsers = userRepository.findByStatus(UserStatus.ACTIVE);

        if (activeUsers.size() < MIN_MEMBERS) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "正式成员不足" + MIN_MEMBERS + "人");
        }

        // 为每个成员创建薪酬记录
        for (User user : activeUsers) {
            // 获取用户总积分
            Integer totalPoints = pointService.getTotalPoints(user.getId());
            if (totalPoints == null) {
                totalPoints = 0;
            }

            Salary salary = Salary.builder()
                    .user(user)
                    .period(period)
                    .basePoints(totalPoints)
                    .bonusPoints(0)
                    .deduction(0)
                    .totalPoints(totalPoints)
                    .coins(0)
                    .status(SalaryStatus.DRAFT)
                    .build();

            salaryRepository.save(salary);
        }
    }

    @Override
    public List<SalaryDTO> getMySalaries() {
        User currentUser = getCurrentUser();
        List<Salary> salaries = salaryRepository.findByUser_Id(currentUser.getId());
        return salaries.stream().map(SalaryDTO::fromEntity).toList();
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "未登录");
        }
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "用户不存在"));
    }
}
