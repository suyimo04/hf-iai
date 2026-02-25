package com.huafen.system.controller;

import com.huafen.system.common.Result;
import com.huafen.system.dto.point.*;
import com.huafen.system.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 积分控制器
 */
@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    /**
     * 添加积分（管理员/组长）
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LEADER')")
    public Result<PointDTO> addPoint(@RequestBody PointAddRequest request) {
        return Result.success(pointService.addPoint(request));
    }

    /**
     * 分页查询积分记录（管理员/组长/副组长）
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LEADER', 'VICE_LEADER')")
    public Result<Page<PointDTO>> list(PointQueryRequest request) {
        return Result.success(pointService.getPoints(request));
    }

    /**
     * 获取当前用户的积分记录
     */
    @GetMapping("/my")
    public Result<List<PointDTO>> getMyPoints() {
        return Result.success(pointService.getMyPoints());
    }

    /**
     * 获取用户积分汇总
     */
    @GetMapping("/summary/{userId}")
    public Result<UserPointSummary> getUserSummary(@PathVariable Long userId) {
        return Result.success(pointService.getUserSummary(userId));
    }

    /**
     * 每日签到
     */
    @PostMapping("/checkin")
    public Result<PointDTO> checkin() {
        return Result.success(pointService.checkin());
    }
}
