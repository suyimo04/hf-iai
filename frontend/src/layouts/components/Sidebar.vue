<template>
  <aside class="sidebar glass-effect">
    <div class="sidebar__logo">
      <div class="sidebar__logo-icon">
        <el-icon :size="28"><Promotion /></el-icon>
      </div>
      <span class="sidebar__logo-text">花粉俱乐部</span>
    </div>

    <el-menu
      :default-active="activeMenu"
      class="sidebar__menu"
      :router="true"
      :unique-opened="true"
      background-color="transparent"
      text-color="#4b5563"
      active-text-color="#10b981"
    >
      <template v-for="item in filteredMenus" :key="item.path">
        <!-- 有子菜单 -->
        <el-sub-menu v-if="item.children && item.children.length" :index="item.path">
          <template #title>
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.title }}</span>
          </template>
          <!-- 二级菜单 -->
          <template v-for="child in item.children" :key="child.path">
            <!-- 三级菜单 -->
            <el-sub-menu v-if="child.children && child.children.length" :index="child.path">
              <template #title>
                <span>{{ child.title }}</span>
              </template>
              <el-menu-item
                v-for="grandChild in child.children"
                :key="grandChild.path"
                :index="grandChild.path"
              >
                <span>{{ grandChild.title }}</span>
              </el-menu-item>
            </el-sub-menu>
            <!-- 二级菜单项 -->
            <el-menu-item v-else :index="child.path">
              <span>{{ child.title }}</span>
            </el-menu-item>
          </template>
        </el-sub-menu>
        <!-- 无子菜单 -->
        <el-menu-item v-else :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.title }}</span>
        </el-menu-item>
      </template>
    </el-menu>

    <div class="sidebar__footer">
      <div class="sidebar__version">v1.0.0</div>
    </div>
  </aside>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import type { Role } from '@/types'
import {
  Promotion,
  DataLine,
  User,
  AddLocation,
  ChatLineRound,
  Setting as SettingIcon,
  Calendar,
  Money,
  Document,
  Tickets
} from '@element-plus/icons-vue'

interface MenuItem {
  path: string
  title: string
  icon?: typeof DataLine
  roles?: Role[]
  children?: MenuItem[]
}

const route = useRoute()
const userStore = useUserStore()

const menus: MenuItem[] = [
  { path: '/dashboard', title: '数据看板', icon: DataLine },
  {
    path: '/members',
    title: '成员管理',
    icon: User,
    roles: ['ADMIN', 'LEADER', 'VICE_LEADER'],
    children: [
      { path: '/members', title: '成员列表' },
      { path: '/members/flow', title: '成员流转' }
    ]
  },
  {
    path: '/recruitment',
    title: '招募管理',
    icon: AddLocation,
    roles: ['ADMIN', 'LEADER', 'VICE_LEADER'],
    children: [
      { path: '/recruitment', title: '报名列表' },
      { path: '/recruitment/interview-config', title: '面试配置' }
    ]
  },
  {
    path: '/interview',
    title: 'AI面试',
    icon: ChatLineRound,
    roles: ['ADMIN', 'LEADER'],
    children: [
      { path: '/interview', title: '面试列表' },
      { path: '/interview/replay', title: '面试回放' }
    ]
  },
  { path: '/activities', title: '活动管理', icon: Calendar },
  {
    path: '/salary',
    title: '薪酬管理',
    icon: Money,
    roles: ['ADMIN', 'LEADER', 'VICE_LEADER', 'MEMBER'],
    children: [
      { path: '/salary', title: '薪酬列表' },
      { path: '/salary/batch', title: '批量编辑', roles: ['ADMIN', 'LEADER'] },
      { path: '/salary/flow', title: '成员流转日志', roles: ['ADMIN', 'LEADER'] }
    ]
  },
  {
    path: '/questionnaire',
    title: '问卷管理',
    icon: Document,
    roles: ['ADMIN', 'LEADER', 'VICE_LEADER'],
    children: [
      { path: '/questionnaire', title: '问卷列表' },
      { path: '/questionnaire/create', title: '创建问卷' }
    ]
  },
  {
    path: '/settings',
    title: '系统设置',
    icon: SettingIcon,
    roles: ['ADMIN'],
    children: [
      { path: '/settings/config', title: '配置中心' },
      { path: '/settings/menus', title: '菜单管理' },
      { path: '/settings/permissions', title: '权限管理' }
    ]
  },
  {
    path: '/logs',
    title: '日志管理',
    icon: Tickets,
    roles: ['ADMIN', 'LEADER'],
    children: [
      { path: '/logs', title: '操作日志' },
      { path: '/logs/role-change', title: '角色变更' }
    ]
  }
]

const activeMenu = computed(() => {
  return route.path
})

const filterMenuByRole = (menu: MenuItem): MenuItem | null => {
  // 检查当前菜单权限
  if (menu.roles && menu.roles.length > 0 && !userStore.hasPermission(menu.roles)) {
    return null
  }

  // 如果有子菜单，递归过滤
  if (menu.children && menu.children.length > 0) {
    const filteredChildren = menu.children
      .map(child => filterMenuByRole(child))
      .filter((child): child is MenuItem => child !== null)

    if (filteredChildren.length === 0) {
      return null
    }

    return { ...menu, children: filteredChildren }
  }

  return menu
}

const filteredMenus = computed(() => {
  return menus
    .map(menu => filterMenuByRole(menu))
    .filter((menu): menu is MenuItem => menu !== null)
})
</script>

<style scoped>
.sidebar {
  display: flex;
  flex-direction: column;
  height: 100%;
  border-right: 1px solid rgba(0, 0, 0, 0.05);
  border-radius: 0;
}

.sidebar__logo {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px 24px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.sidebar__logo-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #10b981 0%, #34d399 100%);
  border-radius: 10px;
  color: white;
}

.sidebar__logo-text {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.sidebar__menu {
  flex: 1;
  border-right: none;
  padding: 12px 8px;
  overflow-y: auto;
}

.sidebar__menu .el-menu-item {
  height: 48px;
  line-height: 48px;
  margin: 4px 0;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.sidebar__menu .el-menu-item:hover {
  background-color: rgba(16, 185, 129, 0.08) !important;
}

.sidebar__menu .el-menu-item.is-active {
  background-color: rgba(16, 185, 129, 0.12) !important;
  font-weight: 500;
}

.sidebar__footer {
  padding: 16px 24px;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
}

.sidebar__version {
  font-size: 12px;
  color: #9ca3af;
  text-align: center;
}
</style>
