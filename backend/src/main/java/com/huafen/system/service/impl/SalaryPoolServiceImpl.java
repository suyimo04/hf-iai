package com.huafen.system.service.impl;

import com.huafen.system.common.ResultCode;
import com.huafen.system.dto.salary.SalaryAllocationDTO;
import com.huafen.system.dto.salary.SalaryDTO;
import com.huafen.system.dto.salary.SalaryPoolSummaryDTO;
import com.huafen.system.dto.salary.SalaryValidationResult;
import com.huafen.system.entity.Salary;
import com.huafen.system.entity.User;
import com.huafen.system.entity.enums.SalaryStatus;
import com.huafen.system.entity.enums.UserStatus;
import com.huafen.system.exception.BusinessException;
import com.huafen.system.repository.SalaryRepository;
import com.huafen.system.repository.UserRepository;
import com.huafen.system.service.PointService;
import com.huafen.system.service.SalaryPoolService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 薪酬池分配服务实现
 */
@Service
@RequiredArgsConstructor
public class SalaryPoolServiceImpl implements SalaryPoolService {

    private final SalaryRepository salaryRepository;
    private final UserRepository userRepository;
    private final PointService pointService;

    @Override
    public SalaryValidationResult validateAllocation(List<SalaryAllocationDTO> allocations) {
        List<String> errors = new ArrayList<>();
        BigDecimal totalCoins = BigDecimal.ZERO;

        // 1. 检查人数是否为5
        if (allocations.size() != FORMAL_MEMBER_COUNT) {
            errors.add("正式成员必须为" + FORMAL_MEMBER_COUNT + "人，当前为" + allocations.size() + "人");
        }

        // 2. 检查每人200-400范围
        for (SalaryAllocationDTO alloc : allocations) {
            if (alloc.getCoins() == null) {
                errors.add(alloc.getNickname() + "的迷你币未设置");
                continue;
            }
            if (alloc.getCoins() < MIN_PER_PERSON) {
                errors.add(alloc.getNickname() + "的迷你币" + alloc.getCoins() + "低于最低限额" + MIN_PER_PERSON);
            }
            if (alloc.getCoins() > MAX_PER_PERSON) {
                errors.add(alloc.getNickname() + "的迷你币" + alloc.getCoins() + "超过最高限额" + MAX_PER_PERSON);
            }
            totalCoins = totalCoins.add(BigDecimal.valueOf(alloc.getCoins()));
        }

        // 3. 检查总额不超过2000
        if (totalCoins.compareTo(BigDecimal.valueOf(TOTAL_POOL)) > 0) {
            errors.add("总额" + totalCoins.intValue() + "超过薪酬池上限" + TOTAL_POOL);
        }

        return SalaryValidationResult.builder()
                .valid(errors.isEmpty())
                .errors(errors)
                .totalSalary(totalCoins)
                .build();
    }
    @Override
    @Transactional
    public void batchSaveSalaries(String period, List<SalaryAllocationDTO> allocations) {
        // 先校验
        SalaryValidationResult validation = validateAllocation(allocations);
        if (!validation.getValid()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, String.join("; ", validation.getErrors()));
        }

        // 获取该期间的所有薪酬记录
        List<Salary> salaries = salaryRepository.findByPeriod(period);
        Map<Long, Salary> salaryMap = salaries.stream()
                .collect(Collectors.toMap(s -> s.getUser().getId(), s -> s));

        // 更新每条记录的迷你币
        for (SalaryAllocationDTO alloc : allocations) {
            Salary salary = salaryMap.get(alloc.getUserId());
            if (salary == null) {
                throw new BusinessException(ResultCode.NOT_FOUND, "用户" + alloc.getNickname() + "的薪酬记录不存在");
            }
            salary.setCoins(alloc.getCoins());
            salary.setSalary(BigDecimal.valueOf(alloc.getCoins()));
            salary.setStatus(SalaryStatus.CONFIRMED);
            if (alloc.getRemark() != null) {
                salary.setRemark(alloc.getRemark());
            }
            salaryRepository.save(salary);
        }
    }

    @Override
    @Transactional
    public List<SalaryDTO> generateMonthlySalaries(String period) {
        // 检查该期间是否已有记录
        List<Salary> existing = salaryRepository.findByPeriod(period);
        if (!existing.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该期间已存在薪酬记录");
        }

        // 获取所有正式成员
        List<User> activeUsers = userRepository.findByStatus(UserStatus.ACTIVE);
        if (activeUsers.size() < FORMAL_MEMBER_COUNT) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "正式成员不足" + FORMAL_MEMBER_COUNT + "人");
        }

        // 计算总积分
        int grandTotalPoints = 0;
        List<Integer> userPoints = new ArrayList<>();
        for (User user : activeUsers) {
            Integer points = pointService.getTotalPoints(user.getId());
            if (points == null) points = 0;
            userPoints.add(points);
            grandTotalPoints += points;
        }

        // 为每个成员创建薪酬记录，基于积分比例分配迷你币
        List<SalaryDTO> result = new ArrayList<>();
        for (int i = 0; i < activeUsers.size(); i++) {
            User user = activeUsers.get(i);
            int points = userPoints.get(i);

            // 按积分比例计算迷你币，确保在200-400范围内
            int coins = 0;
            if (grandTotalPoints > 0) {
                coins = (int) Math.round((double) points / grandTotalPoints * TOTAL_POOL);
                coins = Math.max(MIN_PER_PERSON, Math.min(MAX_PER_PERSON, coins));
            } else {
                coins = TOTAL_POOL / activeUsers.size();
            }

            Salary salary = Salary.builder()
                    .user(user)
                    .period(period)
                    .basePoints(points)
                    .bonusPoints(0)
                    .deduction(0)
                    .totalPoints(points)
                    .coins(coins)
                    .salary(BigDecimal.valueOf(coins))
                    .status(SalaryStatus.DRAFT)
                    .build();

            Salary saved = salaryRepository.save(salary);
            result.add(SalaryDTO.fromEntity(saved));
        }

        return result;
    }

    @Override
    public SalaryPoolSummaryDTO getPoolSummary(String period) {
        List<Salary> salaries = salaryRepository.findByPeriod(period);

        int allocatedCoins = salaries.stream()
                .mapToInt(s -> s.getCoins() != null ? s.getCoins() : 0)
                .sum();

        List<SalaryAllocationDTO> allocations = salaries.stream()
                .map(s -> SalaryAllocationDTO.builder()
                        .userId(s.getUser().getId())
                        .userName(s.getUser().getUsername())
                        .nickname(s.getUser().getNickname())
                        .coins(s.getCoins())
                        .totalPoints(s.getTotalPoints())
                        .remark(s.getRemark())
                        .build())
                .collect(Collectors.toList());

        return SalaryPoolSummaryDTO.builder()
                .period(period)
                .totalPool(TOTAL_POOL)
                .allocatedCoins(allocatedCoins)
                .remainingCoins(TOTAL_POOL - allocatedCoins)
                .memberCount(salaries.size())
                .minPerPerson(MIN_PER_PERSON)
                .maxPerPerson(MAX_PER_PERSON)
                .allocations(allocations)
                .build();
    }
}
