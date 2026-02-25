package com.huafen.system.service.impl;

import com.huafen.system.common.ResultCode;
import com.huafen.system.dto.point.*;
import com.huafen.system.entity.Point;
import com.huafen.system.entity.User;
import com.huafen.system.entity.enums.PointType;
import com.huafen.system.exception.BusinessException;
import com.huafen.system.repository.PointRepository;
import com.huafen.system.repository.UserRepository;
import com.huafen.system.service.PointService;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 积分服务实现
 */
@Service
@RequiredArgsConstructor
public class PointServiceImpl implements PointService {

    private static final int CHECKIN_POINTS = 10;

    private final PointRepository pointRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public PointDTO addPoint(PointAddRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "用户不存在"));

        Point point = Point.builder()
                .user(user)
                .type(request.getType())
                .amount(request.getAmount())
                .description(request.getDescription())
                .build();

        Point saved = pointRepository.save(point);
        return PointDTO.fromEntity(saved);
    }

    @Override
    public Page<PointDTO> getPoints(PointQueryRequest request) {
        Specification<Point> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request.getUserId() != null) {
                predicates.add(cb.equal(root.get("user").get("id"), request.getUserId()));
            }
            if (request.getType() != null) {
                predicates.add(cb.equal(root.get("type"), request.getType()));
            }
            if (request.getStartDate() != null) {
                LocalDateTime startDateTime = request.getStartDate().atStartOfDay();
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startDateTime));
            }
            if (request.getEndDate() != null) {
                LocalDateTime endDateTime = request.getEndDate().atTime(LocalTime.MAX);
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), endDateTime));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        PageRequest pageRequest = PageRequest.of(
                request.getPage(),
                request.getSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return pointRepository.findAll(spec, pageRequest).map(PointDTO::fromEntity);
    }

    @Override
    public List<PointDTO> getMyPoints() {
        User currentUser = getCurrentUser();
        List<Point> points = pointRepository.findByUser_Id(currentUser.getId());
        return points.stream().map(PointDTO::fromEntity).toList();
    }

    @Override
    public UserPointSummary getUserSummary(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "用户不存在"));

        List<Point> points = pointRepository.findByUser_Id(userId);

        int checkinPoints = 0;
        int taskPoints = 0;
        int rewardPoints = 0;
        int deductionPoints = 0;
        int activityPoints = 0;

        for (Point point : points) {
            switch (point.getType()) {
                case CHECKIN -> checkinPoints += point.getAmount();
                case TASK -> taskPoints += point.getAmount();
                case REWARD -> rewardPoints += point.getAmount();
                case DEDUCTION -> deductionPoints += point.getAmount();
                case ACTIVITY -> activityPoints += point.getAmount();
            }
        }

        int totalPoints = checkinPoints + taskPoints + rewardPoints + deductionPoints + activityPoints;

        return UserPointSummary.builder()
                .userId(userId)
                .username(user.getUsername())
                .totalPoints(totalPoints)
                .checkinPoints(checkinPoints)
                .taskPoints(taskPoints)
                .rewardPoints(rewardPoints)
                .deductionPoints(deductionPoints)
                .activityPoints(activityPoints)
                .build();
    }

    @Override
    public Integer getTotalPoints(Long userId) {
        return pointRepository.sumByUserId(userId);
    }

    @Override
    @Transactional
    public PointDTO checkin() {
        User currentUser = getCurrentUser();

        // 检查今日是否已签到
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);

        Specification<Point> spec = (root, query, cb) -> cb.and(
                cb.equal(root.get("user").get("id"), currentUser.getId()),
                cb.equal(root.get("type"), PointType.CHECKIN),
                cb.greaterThanOrEqualTo(root.get("createdAt"), todayStart),
                cb.lessThanOrEqualTo(root.get("createdAt"), todayEnd)
        );

        if (pointRepository.count(spec) > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "今日已签到");
        }

        Point point = Point.builder()
                .user(currentUser)
                .type(PointType.CHECKIN)
                .amount(CHECKIN_POINTS)
                .description("每日签到")
                .build();

        Point saved = pointRepository.save(point);
        return PointDTO.fromEntity(saved);
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
