package com.huafen.system.controller;

import com.huafen.system.common.Result;
import com.huafen.system.dto.menu.MenuCreateRequest;
import com.huafen.system.dto.menu.MenuDTO;
import com.huafen.system.dto.menu.MenuUpdateRequest;
import com.huafen.system.entity.Menu;
import com.huafen.system.entity.User;
import com.huafen.system.repository.UserRepository;
import com.huafen.system.service.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单管理控制器
 */
@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;
    private final UserRepository userRepository;

    /**
     * 获取菜单树（管理用）
     */
    @GetMapping("/tree")
    @PreAuthorize("hasAnyRole('ADMIN', 'LEADER')")
    public Result<List<MenuDTO>> getMenuTree() {
        List<MenuDTO> menuTree = menuService.getMenuTree();
        return Result.success(menuTree);
    }

    /**
     * 获取当前用户菜单（根据角色）
     */
    @GetMapping("/my-menus")
    public Result<List<MenuDTO>> getMyMenus() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null) {
            return Result.success(List.of());
        }

        List<MenuDTO> menus = menuService.getMenuTreeByRole(user.getRole().name());
        return Result.success(menus);
    }

    /**
     * 创建菜单
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Menu> createMenu(@Valid @RequestBody MenuCreateRequest request) {
        Menu menu = menuService.createMenu(request);
        return Result.success(menu);
    }

    /**
     * 更新菜单
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Menu> updateMenu(@PathVariable Long id, @Valid @RequestBody MenuUpdateRequest request) {
        Menu menu = menuService.updateMenu(id, request);
        return Result.success(menu);
    }

    /**
     * 删除菜单
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteMenu(@PathVariable Long id) {
        menuService.deleteMenu(id);
        return Result.success();
    }

    /**
     * 分配角色菜单
     */
    @PostMapping("/role/{role}/menus")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> assignRoleMenus(@PathVariable String role, @RequestBody List<Long> menuIds) {
        menuService.assignMenusToRole(role, menuIds);
        return Result.success();
    }
}
