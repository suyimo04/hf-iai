import type { App } from 'vue'
import BaseCard from './BaseCard.vue'
import BaseButton from './BaseButton.vue'

// 导出组件
export { BaseCard, BaseButton }

// 组件列表
const components = [BaseCard, BaseButton]

// 插件安装函数
export default {
  install(app: App) {
    components.forEach((component) => {
      app.component(component.name as string, component)
    })
  }
}
