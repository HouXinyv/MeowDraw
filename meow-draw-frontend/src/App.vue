<script setup lang="ts">
import BasicLayout from '@/layouts/BasicLayout.vue'
import CursorEffects from '@/components/CursorEffects.vue'
</script>

<template>
  <div class="app-root">
    <CursorEffects />
    <BasicLayout />
  </div>
</template>

<style>
/* 全局样式优化 */
* {
  box-sizing: border-box;
}

body {
  margin: 0;
  padding: 0;
  font-family:
    -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Oxygen', 'Ubuntu', 'Cantarell',
    'Fira Sans', 'Droid Sans', 'Helvetica Neue', sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  overflow-x: hidden;
  min-height: 100vh;

  /* 全站海洋背景（温馨淡色，偏“奇幻漂流”的轻松氛围） */
  background-color: #eaf6ff;
  background-image:
    radial-gradient(900px 650px at 22% 18%, rgba(56, 189, 248, 0.22) 0%, rgba(234, 246, 255, 0) 62%),
    radial-gradient(850px 600px at 85% 28%, rgba(45, 212, 191, 0.16) 0%, rgba(234, 246, 255, 0) 64%),
    radial-gradient(700px 520px at 55% 82%, rgba(167, 139, 250, 0.10) 0%, rgba(234, 246, 255, 0) 66%),
    radial-gradient(900px 520px at 50% -10%, rgba(255, 236, 196, 0.28) 0%, rgba(234, 246, 255, 0) 60%),
    linear-gradient(180deg, #f7fbff 0%, #eaf6ff 42%, #dff3ff 100%);
}

html {
  overflow-x: hidden;
}

.app-root {
  position: relative;
  z-index: 1;
}

/* 波光层（不挡交互） */
body::before {
  content: '';
  position: fixed;
  inset: -20vh -10vw;
  pointer-events: none;
  z-index: 0;
  background:
    radial-gradient(900px 260px at 12% 22%, rgba(255, 255, 255, 0.10) 0%, rgba(255, 255, 255, 0) 62%),
    radial-gradient(760px 240px at 72% 36%, rgba(255, 255, 255, 0.08) 0%, rgba(255, 255, 255, 0) 64%),
    radial-gradient(880px 280px at 42% 72%, rgba(255, 255, 255, 0.07) 0%, rgba(255, 255, 255, 0) 66%);
  filter: blur(12px);
  opacity: 0.75;
  transform: translate3d(0, 0, 0);
  animation: oceanShimmer 12s ease-in-out infinite alternate;
}

/* 气泡层（更弱、慢） */
body::after {
  content: '';
  position: fixed;
  inset: 0;
  pointer-events: none;
  z-index: 0;
  background-image:
    radial-gradient(6px 6px at 12% 78%, rgba(255, 255, 255, 0.16) 0 55%, rgba(255, 255, 255, 0) 60%),
    radial-gradient(4px 4px at 28% 86%, rgba(255, 255, 255, 0.12) 0 55%, rgba(255, 255, 255, 0) 60%),
    radial-gradient(5px 5px at 48% 92%, rgba(255, 255, 255, 0.14) 0 55%, rgba(255, 255, 255, 0) 60%),
    radial-gradient(3px 3px at 68% 80%, rgba(255, 255, 255, 0.10) 0 55%, rgba(255, 255, 255, 0) 60%),
    radial-gradient(5px 5px at 86% 88%, rgba(255, 255, 255, 0.13) 0 55%, rgba(255, 255, 255, 0) 60%);
  opacity: 0.6;
  animation: oceanBubbles 14s linear infinite;
}

@keyframes oceanShimmer {
  0% {
    transform: translate3d(-2vw, -1vh, 0) rotate(-0.2deg);
  }
  100% {
    transform: translate3d(2vw, 1.5vh, 0) rotate(0.2deg);
  }
}

@keyframes oceanBubbles {
  0% {
    transform: translate3d(0, 10vh, 0);
  }
  100% {
    transform: translate3d(0, -10vh, 0);
  }
}

/* 桌面端隐藏系统光标，使用猫爪光标替代 */
@media (pointer: fine) and (hover: hover) {
  html.meow-cursor-enabled,
  html.meow-cursor-enabled body,
  html.meow-cursor-enabled body * {
    cursor: none !important;
  }
}
</style>
