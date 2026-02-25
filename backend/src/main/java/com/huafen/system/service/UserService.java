package com.huafen.system.service;

import com.huafen.system.dto.UserDTO;
import com.huafen.system.dto.user.UserQueryRequest;
import org.springframework.data.domain.Page;

/**
 * 用户管理服务接口
 */
public interface UserService {

    /**
     * 分页查询用户列表
     */
    Page<UserDTO> getUsers(UserQueryRequest request);

    /**
     * 根据ID获取用户
     */
    UserDTO getUserById(Long id);

    /**
     * 修改用户角色
     */
    UserDTO updateRole(Long id, String role);

    /**
     * 修改用户状态
     */
    UserDTO updateStatus(Long id, String status);

    /**
     * 删除用户
     */
    void deleteUser(Long id);
}
