<template>
  <div class="app-wrapper">
    <main :class="['page-main', { 'no-chrome': hideChrome }]">
      <router-view />
    </main>

    <BottomNav v-if="!hideChrome" />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import BottomNav from './components/BottomNav.vue'

const route = useRoute()
const hideChrome = computed(() => route.meta.hideChrome)
</script>

<style>
html, body, #app {
  margin: 0;
  padding: 0;
  height: 100%;
}

.app-wrapper {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.page-main {
  flex: 1;
  overflow-y: auto;
  background: #f5f6f7;
  padding-bottom: 56px; /* bottom nav height */
}

.page-main.no-chrome {
  padding-bottom: 0;
}

@media (min-width: 768px) {
  .mobile-header {
    font-size: 1.2rem;
  }
}
</style> 