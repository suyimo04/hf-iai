package com.huafen.system.service.impl;

import com.huafen.system.common.ResultCode;
import com.huafen.system.dto.menu.MenuCreateRequest;
import com.huafen.system.dto.menu.MenuDTO;
import com.huafen.system.dto.menu.MenuUpdateRequest;
import com.huafen.system.entity.Menu;
import com.huafen.system.entity.RoleMenu;
import com.huafen.system.exception.BusinessException;
import com.huafen.system.repository.MenuRepository;
import com.huafen.system.repository.RoleMenuRepository;
import com.huafen.system.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 菜单服务实现
 */
@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final MenuRepository menuRepository;
    private final RoleMenuRepository roleMenuRepository;

    @Override
    public List<Menu> getAllMenus() {
        return menuRepository.findAllByOrderBySortOrderAsc();
    }

    @Override
    public List<MenuDTO> getMenuTree() {
        List<Menu> allMenus = menuRepository.findAllByOrderBySortOrderAsc();
        return buildMenuTree(allMenus);
    }

    @Override
    public List<Menu> getMenusByRole(String role) {
        return menuRepository.findByRole(role);
    }

    @Override
    public List<MenuDTO> getMenuTreeByRole(String role) {
        List<Menu> menus = menuRepository.findByRole(role);
        return buildMenuTree(menus);
    }

    @Override
    @Transactional
    public Menu createMenu(MenuCreateRequest request) {
        // 验证父菜单是否存在
        if (request.getParentId() != null && request.getParentId() > 0) {
            if (!menuRepository.existsById(request.getParentId())) {
                throw new BusinessException(ResultCode.NOT_FOUND, "父菜单不存在");
            }
        }

        Menu menu = Menu.builder()
                .parentId(request.getParentId())
                .name(request.getName())
                .path(request.getPath())
                .component(request.getComponent())
                .icon(request.getIcon())
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .visible(request.getVisible() != null ? request.getVisible() : true)
                .status(request.getStatus() != null ? request.getStatus() : true)
                .build();

        return menuRepository.save(menu);
    }

    @Override
    @Transactional
    public Menu updateMenu(Long id, MenuUpdateRequest request) {
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "菜单不存在"));

        if (request.getParentId() != null) {
            // 不能将自己设为父菜单
            if (request.getParentId().equals(id)) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "不能将自己设为父菜单");
            }
            if (request.getParentId() > 0 && !menuRepository.existsById(request.getParentId())) {
                throw new BusinessException(ResultCode.NOT_FOUND, "父菜单不存在");
            }
            menu.setParentId(request.getParentId());
        }
        if (request.getName() != null) {
            menu.setName(request.getName());
        }
        if (request.getPath() != null) {
            menu.setPath(request.getPath());
        }
        if (request.getComponent() != null) {
            menu.setComponent(request.getComponent());
        }
        if (request.getIcon() != null) {
            menu.setIcon(request.getIcon());
        }
        if (request.getSortOrder() != null) {
            menu.setSortOrder(request.getSortOrder());
        }
        if (request.getVisible() != null) {
            menu.setVisible(request.getVisible());
        }
        if (request.getStatus() != null) {
            menu.setStatus(request.getStatus());
        }

        return menuRepository.save(menu);
    }

    @Override
    @Transactional
    public void deleteMenu(Long id) {
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "菜单不存在"));

        // 检查是否有子菜单
        if (menuRepository.existsByParentId(id)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "存在子菜单，无法删除");
        }

        // 删除角色菜单关联
        roleMenuRepository.deleteByRole(null); // 这里需要先查询关联再删除
        List<RoleMenu> roleMenus = roleMenuRepository.findAll().stream()
                .filter(rm -> rm.getMenu().getId().equals(id))
                .toList();
        roleMenuRepository.deleteAll(roleMenus);

        menuRepository.delete(menu);
    }

    @Override
    @Transactional
    public void assignMenusToRole(String role, List<Long> menuIds) {
        // 删除该角色的所有菜单关联
        roleMenuRepository.deleteByRole(role);

        // 创建新的关联
        if (menuIds != null && !menuIds.isEmpty()) {
            List<Menu> menus = menuRepository.findAllById(menuIds);
            List<RoleMenu> roleMenus = menus.stream()
                    .map(menu -> RoleMenu.builder()
                            .role(role)
                            .menu(menu)
                            .build())
                    .collect(Collectors.toList());
            roleMenuRepository.saveAll(roleMenus);
        }
    }

    /**
     * 构建菜单树
     */
    private List<MenuDTO> buildMenuTree(List<Menu> menus) {
        // 转换为DTO
        List<MenuDTO> dtoList = menus.stream()
                .map(MenuDTO::fromEntity)
                .collect(Collectors.toList());

        // 按parentId分组
        Map<Long, List<MenuDTO>> parentMap = dtoList.stream()
                .filter(dto -> dto.getParentId() != null && dto.getParentId() > 0)
                .collect(Collectors.groupingBy(MenuDTO::getParentId));

        // 为每个节点设置子节点
        dtoList.forEach(dto -> {
            List<MenuDTO> children = parentMap.get(dto.getId());
            if (children != null) {
                dto.setChildren(children);
            }
        });

        // 返回根节点（parentId为null或0的节点）
        return dtoList.stream()
                .filter(dto -> dto.getParentId() == null || dto.getParentId() == 0)
                .collect(Collectors.toList());
    }
}
