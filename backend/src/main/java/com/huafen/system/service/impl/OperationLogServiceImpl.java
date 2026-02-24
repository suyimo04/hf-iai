package com.huafen.system.service.impl;

import com.huafen.system.dto.log.LogQueryRequest;
import com.huafen.system.dto.log.OperationLogDTO;
import com.huafen.system.entity.OperationLog;
import com.huafen.system.entity.User;
import com.huafen.system.entity.enums.LogCategory;
import com.huafen.system.repository.OperationLogRepository;
import com.huafen.system.repository.UserRepository;
import com.huafen.system.service.OperationLogService;
import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 操作日志服务实现
 */
@Service
@RequiredArgsConstructor
public class OperationLogServiceImpl implements OperationLogService {

    private final OperationLogRepository operationLogRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void log(LogCategory category, String action, String targetType, Long targetId, String detail) {
        User currentUser = getCurrentUser();
        String ip = getClientIp();

        OperationLog log = OperationLog.builder()
                .user(currentUser)
                .category(category)
                .action(action)
                .targetType(targetType)
                .targetId(targetId)
                .detail(detail)
                .ip(ip)
                .build();

        operationLogRepository.save(log);
    }

    @Override
    @Transactional
    public void log(String action, String targetType, Long targetId, String detail) {
        // 兼容旧方法，category 为 null
        log(null, action, targetType, targetId, detail);
    }

    @Override
    public Page<OperationLogDTO> getLogs(LogQueryRequest request) {
        Specification<OperationLog> spec = buildSpecification(request);
        PageRequest pageRequest = PageRequest.of(
                request.getPage(),
                request.getSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return operationLogRepository.findAll(spec, pageRequest)
                .map(OperationLogDTO::fromEntity);
    }

    private Specification<OperationLog> buildSpecification(LogQueryRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request.getUserId() != null) {
                predicates.add(cb.equal(root.get("user").get("id"), request.getUserId()));
            }

            if (request.getCategory() != null) {
                predicates.add(cb.equal(root.get("category"), request.getCategory()));
            }

            if (request.getAction() != null && !request.getAction().isEmpty()) {
                predicates.add(cb.equal(root.get("action"), request.getAction()));
            }

            if (request.getTargetType() != null && !request.getTargetType().isEmpty()) {
                predicates.add(cb.equal(root.get("targetType"), request.getTargetType()));
            }

            if (request.getStartDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("createdAt"),
                        request.getStartDate().atStartOfDay()
                ));
            }

            if (request.getEndDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("createdAt"),
                        request.getEndDate().atTime(LocalTime.MAX)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        String username = authentication.getName();
        return userRepository.findByUsername(username).orElse(null);
    }

    private String getClientIp() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }

        HttpServletRequest request = attributes.getRequest();
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 多个代理时取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }
}
