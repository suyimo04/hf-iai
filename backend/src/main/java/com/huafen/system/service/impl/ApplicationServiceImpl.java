package com.huafen.system.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huafen.system.common.ResultCode;
import com.huafen.system.dto.application.ApplicationDTO;
import com.huafen.system.dto.application.ApplicationQueryRequest;
import com.huafen.system.dto.application.ApplicationReviewRequest;
import com.huafen.system.dto.application.ApplicationSubmitRequest;
import com.huafen.system.entity.Application;
import com.huafen.system.entity.User;
import com.huafen.system.entity.enums.ApplicationStatus;
import com.huafen.system.exception.BusinessException;
import com.huafen.system.repository.ApplicationRepository;
import com.huafen.system.repository.UserRepository;
import com.huafen.system.service.ApplicationService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 报名服务实现
 */
@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public ApplicationDTO submit(ApplicationSubmitRequest request) {
        User currentUser = getCurrentUser();

        // 检查是否已有报名记录
        List<Application> existingApplications = applicationRepository.findByUser_Id(currentUser.getId());
        if (!existingApplications.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "您已提交过报名申请");
        }

        // 创建报名记录
        Application application = Application.builder()
                .user(currentUser)
                .status(ApplicationStatus.PENDING)
                .formData(convertFormDataToJson(request.getFormData()))
                .build();

        Application saved = applicationRepository.save(application);
        return convertToDTO(saved);
    }

    @Override
    public Page<ApplicationDTO> getApplications(ApplicationQueryRequest request) {
        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Specification<Application> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), request.getStatus()));
            }

            if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
                String keyword = "%" + request.getKeyword() + "%";
                predicates.add(cb.or(
                        cb.like(root.get("user").get("username"), keyword),
                        cb.like(root.get("user").get("nickname"), keyword)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Application> page = applicationRepository.findAll(spec, pageable);
        return page.map(this::convertToDTO);
    }

    @Override
    public ApplicationDTO getById(Long id) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "报名记录不存在"));
        return convertToDTO(application);
    }

    @Override
    @Transactional
    public ApplicationDTO review(Long id, ApplicationReviewRequest request) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "报名记录不存在"));

        // 验证状态流转
        validateStatusTransition(application.getStatus(), request.getStatus());

        User reviewer = getCurrentUser();
        application.setStatus(request.getStatus());
        application.setReviewer(reviewer);
        application.setReviewComment(request.getComment());

        Application saved = applicationRepository.save(application);
        return convertToDTO(saved);
    }

    @Override
    public ApplicationDTO getMyApplication() {
        User currentUser = getCurrentUser();
        List<Application> applications = applicationRepository.findByUser_Id(currentUser.getId());
        if (applications.isEmpty()) {
            return null;
        }
        return convertToDTO(applications.get(0));
    }

    private void validateStatusTransition(ApplicationStatus current, ApplicationStatus target) {
        // 状态流转：PENDING -> REVIEWING -> INTERVIEW/PASSED/REJECTED
        boolean valid = switch (current) {
            case PENDING -> target == ApplicationStatus.REVIEWING || target == ApplicationStatus.REJECTED;
            case REVIEWING -> target == ApplicationStatus.INTERVIEW ||
                              target == ApplicationStatus.PASSED ||
                              target == ApplicationStatus.REJECTED;
            case INTERVIEW -> target == ApplicationStatus.PASSED || target == ApplicationStatus.REJECTED;
            default -> false;
        };

        if (!valid) {
            throw new BusinessException(ResultCode.BAD_REQUEST,
                    "无效的状态流转: " + current + " -> " + target);
        }
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

    private String convertFormDataToJson(Map<String, Object> formData) {
        try {
            return objectMapper.writeValueAsString(formData);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "表单数据格式错误");
        }
    }

    private Map<String, Object> parseFormDataFromJson(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private ApplicationDTO convertToDTO(Application application) {
        ApplicationDTO dto = ApplicationDTO.fromEntity(application);
        dto.setFormData(parseFormDataFromJson(application.getFormData()));
        return dto;
    }
}
