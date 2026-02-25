package com.huafen.system.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户分页查询请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserQueryRequest {

    /**
     * 搜索关键词（用户名/昵称/邮箱/手机号）
     */
    private String keyword;

    /**
     * 角色筛选
     */
    private String role;

    /**
     * 状态筛选
     */
    private String status;

    /**
     * 页码（从0开始）
     */
    @Builder.Default
    private Integer page = 0;

    /**
     * 每页大小
     */
    @Builder.Default
    private Integer size = 10;
}
