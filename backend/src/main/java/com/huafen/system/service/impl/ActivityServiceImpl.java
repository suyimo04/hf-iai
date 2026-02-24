package com.huafen.system.service.impl;

import com.huafen.system.common.ResultCode;
import com.huafen.system.dto.activity.*;
import com.huafen.system.dto.point.PointAddRequest;
import com.huafen.system.entity.Activity;
import com.huafen.system.entity.ActivitySignup;
import com.huafen.system.entity.User;
import com.huafen.system.entity.enums.ActivityStatus;
import com.huafen.system.entity.enums.PointType;
import com.huafen.system.exception.BusinessException;
import com.huafen.system.repository.ActivityRepository;
import com.huafen.system.repository.ActivitySignupRepository;
import com.huafen.system.repository.UserRepository;
import com.huafen.system.service.ActivityService;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 活动服务实现
 */
@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {

    private final ActivityRepository activityRepository;
    private final ActivitySignupRepository activitySignupRepository;
    private final UserRepository userRepository;
    private final PointService pointService;

    @Override
    public Page<ActivityDTO> getActivities(ActivityQueryRequest request) {
        Specification<Activity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), request.getStatus()));
            }
            if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
                String keyword = "%" + request.getKeyword() + "%";
                predicates.add(cb.or(
                        cb.like(root.get("title"), keyword),
                        cb.like(root.get("description"), keyword),
                        cb.like(root.get("location"), keyword)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        PageRequest pageRequest = PageRequest.of(
                request.getPage(),
                request.getSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return activityRepository.findAll(spec, pageRequest).map(activity -> {
            long count = activitySignupRepository.countByActivityId(activity.getId());
            return ActivityDTO.fromEntity(activity, count);
        });
    }

    @Override
    public ActivityDTO getById(Long id) {
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "活动不存在"));
        long count = activitySignupRepository.countByActivityId(id);
        return ActivityDTO.fromEntity(activity, count);
    }

    @Override
    @Transactional
    public ActivityDTO create(ActivityCreateRequest request) {
        User currentUser = getCurrentUser();

        Activity activity = Activity.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .location(request.getLocation())
                .maxParticipants(request.getMaxParticipants())
                .pointsReward(request.getPointsReward())
                .status(request.getStatus() != null ? request.getStatus() : ActivityStatus.DRAFT)
                .createdBy(currentUser)
                .build();

        Activity saved = activityRepository.save(activity);
        return ActivityDTO.fromEntity(saved, 0L);
    }

    @Override
    @Transactional
    public ActivityDTO update(Long id, ActivityUpdateRequest request) {
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "活动不存在"));

        if (request.getTitle() != null) {
            activity.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            activity.setDescription(request.getDescription());
        }
        if (request.getStartTime() != null) {
            activity.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            activity.setEndTime(request.getEndTime());
        }
        if (request.getLocation() != null) {
            activity.setLocation(request.getLocation());
        }
        if (request.getMaxParticipants() != null) {
            activity.setMaxParticipants(request.getMaxParticipants());
        }
        if (request.getPointsReward() != null) {
            activity.setPointsReward(request.getPointsReward());
        }
        if (request.getStatus() != null) {
            activity.setStatus(request.getStatus());
        }

        Activity saved = activityRepository.save(activity);
        long count = activitySignupRepository.countByActivityId(id);
        return ActivityDTO.fromEntity(saved, count);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "活动不存在"));
        activityRepository.delete(activity);
    }

    @Override
    @Transactional
    public void signup(Long activityId) {
        User currentUser = getCurrentUser();
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "活动不存在"));

        // 检查活动状态
        if (activity.getStatus() != ActivityStatus.PUBLISHED && activity.getStatus() != ActivityStatus.ONGOING) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "活动当前不可报名");
        }

        // 检查是否已报名
        if (activitySignupRepository.findByActivityIdAndUserId(activityId, currentUser.getId()).isPresent()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "您已报名该活动");
        }

        // 检查人数限制
        if (activity.getMaxParticipants() != null) {
            long currentCount = activitySignupRepository.countByActivityId(activityId);
            if (currentCount >= activity.getMaxParticipants()) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "活动报名人数已满");
            }
        }

        ActivitySignup signup = ActivitySignup.builder()
                .activity(activity)
                .user(currentUser)
                .signedIn(false)
                .build();

        activitySignupRepository.save(signup);
    }

    @Override
    @Transactional
    public void signin(Long activityId, Long userId) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "活动不存在"));

        ActivitySignup signup = activitySignupRepository.findByActivityIdAndUserId(activityId, userId)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "该用户未报名此活动"));

        if (Boolean.TRUE.equals(signup.getSignedIn())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该用户已签到");
        }

        // 更新签到状态
        signup.setSignedIn(true);
        signup.setSignInTime(LocalDateTime.now());
        activitySignupRepository.save(signup);

        // 发放积分奖励
        if (activity.getPointsReward() != null && activity.getPointsReward() > 0) {
            PointAddRequest pointRequest = PointAddRequest.builder()
                    .userId(userId)
                    .type(PointType.ACTIVITY)
                    .amount(activity.getPointsReward())
                    .description("活动签到奖励：" + activity.getTitle())
                    .build();
            pointService.addPoint(pointRequest);
        }
    }

    @Override
    public List<ActivitySignupDTO> getSignups(Long activityId) {
        if (!activityRepository.existsById(activityId)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "活动不存在");
        }
        List<ActivitySignup> signups = activitySignupRepository.findByActivityId(activityId);
        return signups.stream().map(ActivitySignupDTO::fromEntity).toList();
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
