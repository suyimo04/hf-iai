package com.huafen.system.service;

import com.huafen.system.dto.menu.MenuCreateRequest;
import com.huafen.system.dto.menu.MenuDTO;
import com.huafen.system.dto.menu.MenuUpdateRequest;
import com.huafen.system.entity.Menu;

import java.util.List;

/**
 * 菜单服务接口
 */
public interface MenuService {

    /**
     * 获取所有菜单（平铺列表）
     */
    List<Menu> getAllMenus();

    /**
     * 获取菜单树形结构
     */
    List<MenuDTO> getMenuTree();

    /**
     * 根据角色获取菜单列表
     */
    List<Menu> getMenusByRole(String role);

    /**
     * 根据角色获取菜单树形结构
     */
    List<MenuDTO> getMenuTreeByRole(String role);

    /**
     * 创建菜单
     */
    Menu createMenu(MenuCreateRequest request);

    /**
     * 更新菜单
     */
    Menu updateMenu(Long id, MenuUpdateRequest request);

    /**
     * 删除菜单
     */
    void deleteMenu(Long id);

    /**
     * 为角色分配菜单
     */
    void assignMenusToRole(String role, List<Long> menuIds);
}
