import type { Directive, DirectiveBinding, App } from 'vue'
import { useUserStore } from '@/stores/user'

/**
 * 检查权限
 * @param el 元素
 * @param binding 指令绑定值
 */
function checkPermission(el: HTMLElement, binding: DirectiveBinding) {
  const userStore = useUserStore()
  const { value } = binding

  if (value && typeof value === 'string') {
    // 单个权限码
    if (!userStore.hasPermission(value)) {
      el.parentNode?.removeChild(el)
    }
  } else if (Array.isArray(value)) {
    // 多个权限码（满足任一即可）
    const hasAny = value.some(code => userStore.hasPermission(code))
    if (!hasAny) {
      el.parentNode?.removeChild(el)
    }
  }
}

/**
 * 权限指令
 * 使用示例:
 * <el-button v-permission="'user:create'">新增</el-button>
 * <el-button v-permission="['user:delete', 'admin']">删除</el-button>
 */
export const permission: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    checkPermission(el, binding)
  },
  updated(el: HTMLElement, binding: DirectiveBinding) {
    checkPermission(el, binding)
  }
}

/**
 * 全局注册权限指令
 * @param app Vue应用实例
 */
export function setupPermissionDirective(app: App) {
  app.directive('permission', permission)
}

/**
 * 函数式权限检查（用于v-if场景）
 * 使用示例:
 * <el-button v-if="hasPermission('user:create')">新增</el-button>
 * <el-button v-if="hasPermission(['user:delete', 'admin'])">删除</el-button>
 *
 * @param code 权限码或权限码数组
 * @returns 是否拥有权限
 */
export function hasPermission(code: string | string[]): boolean {
  const userStore = useUserStore()
  if (typeof code === 'string') {
    return userStore.hasPermission(code)
  }
  return code.some(c => userStore.hasPermission(c))
}
