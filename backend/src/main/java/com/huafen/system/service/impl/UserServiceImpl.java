package com.huafen.system.service.impl;

import com.huafen.system.common.ResultCode;
import com.huafen.system.dto.UserDTO;
import com.huafen.system.dto.user.UserQueryRequest;
import com.huafen.system.entity.User;
import com.huafen.system.entity.enums.Role;
import com.huafen.system.entity.enums.UserStatus;
import com.huafen.system.exception.BusinessException;
import com.huafen.system.repository.UserRepository;
import com.huafen.system.service.UserService;
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
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户管理服务实现
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public Page<UserDTO> getUsers(UserQueryRequest request) {
        Pageable pageable = PageRequest.of(
                request.getPage() != null ? request.getPage() : 0,
                request.getSize() != null ? request.getSize() : 10,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Specification<User> spec = buildSpecification(request);
        Page<User> userPage = userRepository.findAll(spec, pageable);
        return userPage.map(UserDTO::fromEntity);
    }

    @Override
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));
        return UserDTO.fromEntity(user);
    }

    @Override
    @Transactional
    public UserDTO updateRole(Long id, String role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));

        // 不能修改自己的角色
        checkNotSelf(id, "不能修改自己的角色");

        // 验证角色值
        Role newRole;
        try {
            newRole = Role.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "无效的角色值: " + role);
        }

        user.setRole(newRole);
        User savedUser = userRepository.save(user);
        return UserDTO.fromEntity(savedUser);
    }

    @Override
    @Transactional
    public UserDTO updateStatus(Long id, String status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));

        // 不能修改自己的状态
        checkNotSelf(id, "不能修改自己的状态");

        // 验证状态值
        UserStatus newStatus;
        try {
            newStatus = UserStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "无效的状态值: " + status);
        }

        user.setStatus(newStatus);
        User savedUser = userRepository.save(user);
        return UserDTO.fromEntity(savedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));

        // 不能删除自己
        checkNotSelf(id, "不能删除自己");

        userRepository.delete(user);
    }

    /**
     * 构建动态查询条件
     */
    private Specification<User> buildSpecification(UserQueryRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 关键词搜索（用户名/昵称/邮箱/手机号）
            if (StringUtils.hasText(request.getKeyword())) {
                String keyword = "%" + request.getKeyword().toLowerCase() + "%";
                Predicate usernameLike = cb.like(cb.lower(root.get("username")), keyword);
                Predicate nicknameLike = cb.like(cb.lower(root.get("nickname")), keyword);
                Predicate emailLike = cb.like(cb.lower(root.get("email")), keyword);
                Predicate phoneLike = cb.like(root.get("phone"), keyword);
                predicates.add(cb.or(usernameLike, nicknameLike, emailLike, phoneLike));
            }

            // 角色筛选
            if (StringUtils.hasText(request.getRole())) {
                try {
                    Role role = Role.valueOf(request.getRole().toUpperCase());
                    predicates.add(cb.equal(root.get("role"), role));
                } catch (IllegalArgumentException ignored) {
                    // 忽略无效的角色值
                }
            }

            // 状态筛选
            if (StringUtils.hasText(request.getStatus())) {
                try {
                    UserStatus status = UserStatus.valueOf(request.getStatus().toUpperCase());
                    predicates.add(cb.equal(root.get("status"), status));
                } catch (IllegalArgumentException ignored) {
                    // 忽略无效的状态值
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * 检查操作对象不是当前用户自己
     */
    private void checkNotSelf(Long targetUserId, String errorMessage) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null) {
            userRepository.findByUsername(auth.getName())
                    .ifPresent(currentUser -> {
                        if (currentUser.getId().equals(targetUserId)) {
                            throw new BusinessException(ResultCode.FORBIDDEN, errorMessage);
                        }
                    });
        }
    }
}
