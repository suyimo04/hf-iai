<template>
  <div class="main-layout">
    <Sidebar class="main-layout__sidebar" />
    <div class="main-layout__container">
      <Header class="main-layout__header" />
      <main class="main-layout__content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import Sidebar from './components/Sidebar.vue'
import Header from './components/Header.vue'
</script>

<style scoped>
.main-layout {
  display: flex;
  min-height: 100vh;
}

.main-layout__sidebar {
  width: 240px;
  flex-shrink: 0;
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  z-index: 100;
}

.main-layout__container {
  flex: 1;
  margin-left: 240px;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.main-layout__header {
  height: 60px;
  flex-shrink: 0;
  position: sticky;
  top: 0;
  z-index: 90;
}

.main-layout__content {
  flex: 1;
  padding: var(--spacing-lg, 24px);
  overflow-x: hidden;
}
</style>
