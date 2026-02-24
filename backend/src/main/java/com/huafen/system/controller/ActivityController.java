package com.huafen.system.controller;

import com.huafen.system.common.Result;
import com.huafen.system.dto.activity.*;
import com.huafen.system.service.ActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 活动控制器
 */
@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    /**
     * 分页查询活动列表
     */
    @GetMapping
    public Result<Page<ActivityDTO>> list(ActivityQueryRequest request) {
        return Result.success(activityService.getActivities(request));
    }

    /**
     * 获取活动详情
     */
    @GetMapping("/{id}")
    public Result<ActivityDTO> getById(@PathVariable Long id) {
        return Result.success(activityService.getById(id));
    }

    /**
     * 创建活动（管理员/组长/副组长）
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LEADER', 'VICE_LEADER')")
    public Result<ActivityDTO> create(@RequestBody ActivityCreateRequest request) {
        return Result.success(activityService.create(request));
    }

    /**
     * 更新活动（管理员/组长/副组长）
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LEADER', 'VICE_LEADER')")
    public Result<ActivityDTO> update(@PathVariable Long id, @RequestBody ActivityUpdateRequest request) {
        return Result.success(activityService.update(id, request));
    }

    /**
     * 删除活动（管理员/组长）
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LEADER')")
    public Result<Void> delete(@PathVariable Long id) {
        activityService.delete(id);
        return Result.success();
    }

    /**
     * 报名活动
     */
    @PostMapping("/{id}/signup")
    public Result<Void> signup(@PathVariable Long id) {
        activityService.signup(id);
        return Result.success();
    }

    /**
     * 签到（管理员/组长/副组长操作）
     */
    @PostMapping("/{id}/signin/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LEADER', 'VICE_LEADER')")
    public Result<Void> signin(@PathVariable Long id, @PathVariable Long userId) {
        activityService.signin(id, userId);
        return Result.success();
    }

    /**
     * 获取活动报名列表（管理员/组长/副组长）
     */
    @GetMapping("/{id}/signups")
    @PreAuthorize("hasAnyRole('ADMIN', 'LEADER', 'VICE_LEADER')")
    public Result<List<ActivitySignupDTO>> getSignups(@PathVariable Long id) {
        return Result.success(activityService.getSignups(id));
    }
}
