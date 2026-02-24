<template>
  <nav class="breadcrumb" aria-label="面包屑导航">
    <ol class="breadcrumb__list">
      <!-- 首页 -->
      <li class="breadcrumb__item">
        <router-link to="/" class="breadcrumb__link breadcrumb__link--home">
          <span class="breadcrumb__icon">
            <svg viewBox="0 0 20 20" fill="currentColor" width="14" height="14">
              <path d="M10.707 2.293a1 1 0 00-1.414 0l-7 7a1 1 0 001.414 1.414L4 10.414V17a1 1 0 001 1h2a1 1 0 001-1v-2a1 1 0 011-1h2a1 1 0 011 1v2a1 1 0 001 1h2a1 1 0 001-1v-6.586l.293.293a1 1 0 001.414-1.414l-7-7z" />
            </svg>
          </span>
          <span class="breadcrumb__text">首页</span>
        </router-link>
      </li>

      <!-- 动态路由项 -->
      <li
        v-for="(item, index) in breadcrumbs"
        :key="item.path"
        class="breadcrumb__item"
      >
        <span class="breadcrumb__separator">
          <svg viewBox="0 0 20 20" fill="currentColor" width="12" height="12">
            <path fill-rule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clip-rule="evenodd" />
          </svg>
        </span>
        <router-link
          v-if="index < breadcrumbs.length - 1"
          :to="item.path"
          class="breadcrumb__link"
        >
          <span class="breadcrumb__text">{{ item.title }}</span>
        </router-link>
        <span
          v-else
          class="breadcrumb__current"
          aria-current="page"
        >
          {{ item.title }}
        </span>
      </li>
    </ol>
  </nav>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

defineOptions({
  name: 'Breadcrumb'
})

interface BreadcrumbItem {
  path: string
  title: string
}

const route = useRoute()

const breadcrumbs = computed<BreadcrumbItem[]>(() => {
  return route.matched
    .filter(item => item.meta?.title)
    .map(item => ({
      path: item.path || '/',
      title: item.meta.title as string
    }))
})
</script>

<style scoped>
.breadcrumb {
  display: flex;
  align-items: center;
}

.breadcrumb__list {
  display: flex;
  align-items: center;
  list-style: none;
  margin: 0;
  padding: 0;
  gap: 2px;
}

.breadcrumb__item {
  display: flex;
  align-items: center;
}

.breadcrumb__separator {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #d1d5db;
  margin: 0 4px;
}

.breadcrumb__link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 8px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
  color: #6b7280;
  text-decoration: none;
  transition: all 0.2s ease;
}

.breadcrumb__link:hover {
  color: var(--el-color-primary, #10b981);
  background-color: var(--el-color-primary-light-9, #f0fdf4);
}

.breadcrumb__link--home {
  color: #9ca3af;
}

.breadcrumb__link--home:hover {
  color: var(--el-color-primary, #10b981);
}

.breadcrumb__icon {
  display: flex;
  align-items: center;
  justify-content: center;
}

.breadcrumb__text {
  line-height: 1;
}

.breadcrumb__current {
  display: inline-flex;
  align-items: center;
  padding: 4px 8px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 600;
  color: #1f2937;
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.08) 0%, rgba(16, 185, 129, 0.04) 100%);
}
</style>
