<template>
  <Teleport to="body">
    <div v-if="enabled" aria-hidden="true">
      <!-- 背景粒子层：小鱼干跟随（在内容下方） -->
      <div class="fish-layer">
        <div
          v-for="p in particles"
          :key="p.id"
          class="fish"
          :style="{
            left: `${p.x}px`,
            top: `${p.y}px`,
            width: `${p.size}px`,
            height: `${p.size}px`,
            opacity: p.opacity,
            animationDuration: `${p.duration}ms`,
            filter: `blur(${p.blur}px)`,
            '--r': `${p.rotate}deg`,
            '--dx': `${p.driftX}px`,
            '--dy': `${p.driftY}px`,
          }"
        />
      </div>

      <!-- 光标层：猫爪（在所有内容上方） -->
      <div class="cursor-layer">
        <div
          class="paw-cursor"
          :class="{ down: isMouseDown }"
          :style="{
            transform: `translate3d(${pawX}px, ${pawY}px, 0) rotate(${pawRotate}deg) translate(-8px, -8px) scale(${
              isMouseDown ? 0.9 : 1
            })`,
          }"
        />
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'

type Particle = {
  id: number
  x: number
  y: number
  size: number
  rotate: number
  driftX: number
  driftY: number
  opacity: number
  duration: number
  blur: number
}

const enabled = ref(false)
const isMouseDown = ref(false)

const pawX = ref(0)
const pawY = ref(0)
const pawRotate = ref(0)

const particles = ref<Particle[]>([])

let idSeq = 1
let rafId = 0

let targetX = 0
let targetY = 0
let currentX = 0
let currentY = 0

let lastMoveX = 0
let lastMoveY = 0
let lastSpawnTs = 0

const MAX_PARTICLES = 40

function supportsMouse() {
  if (typeof window === 'undefined') return false
  // 兼容 iframe/预览环境：有些环境 hover 媒体查询会异常，导致特效被错误禁用
  const fine = window.matchMedia?.('(pointer: fine)').matches ?? true
  const hover = window.matchMedia?.('(hover: hover)').matches ?? true
  return fine || hover
}

function spawnParticle(x: number, y: number, dx: number, dy: number) {
  const now = performance.now()
  // 控制生成频率，避免太密集
  if (now - lastSpawnTs < 28) return
  lastSpawnTs = now

  const speed = Math.min(18, Math.hypot(dx, dy))
  const size = 12 + Math.random() * 10 + speed * 0.35

  const driftX = (Math.random() - 0.5) * 30 + dx * 0.15
  const driftY = -18 - Math.random() * 22 + dy * 0.15

  const p: Particle = {
    id: idSeq++,
    x,
    y,
    size,
    rotate: (Math.atan2(dy, dx) * 180) / Math.PI + (Math.random() * 20 - 10),
    driftX,
    driftY,
    opacity: 0.85,
    duration: 850 + Math.random() * 400,
    blur: Math.random() * 0.6,
  }

  particles.value.push(p)
  if (particles.value.length > MAX_PARTICLES) {
    particles.value.splice(0, particles.value.length - MAX_PARTICLES)
  }

  // 到时间后移除粒子（避免常驻占用）
  window.setTimeout(() => {
    particles.value = particles.value.filter((it) => it.id !== p.id)
  }, p.duration)
}

function onMove(e: MouseEvent) {
  targetX = e.clientX
  targetY = e.clientY

  const dx = targetX - lastMoveX
  const dy = targetY - lastMoveY
  lastMoveX = targetX
  lastMoveY = targetY

  // 速度/方向用于猫爪轻微旋转
  if (Math.abs(dx) + Math.abs(dy) > 0.5) {
    const ang = (Math.atan2(dy, dx) * 180) / Math.PI
    pawRotate.value = ang * 0.15
  }

  // 小鱼干粒子
  if (Math.hypot(dx, dy) > 2.5) {
    spawnParticle(targetX, targetY, dx, dy)
  }
}

function tick() {
  // 让猫爪略微跟随（更像“抓挠”的感觉）
  currentX += (targetX - currentX) * 0.35
  currentY += (targetY - currentY) * 0.35

  pawX.value = currentX
  pawY.value = currentY

  rafId = window.requestAnimationFrame(tick)
}

function onDown() {
  isMouseDown.value = true
}

function onUp() {
  isMouseDown.value = false
}

onMounted(() => {
  if (!supportsMouse()) return
  enabled.value = true
  document.documentElement.classList.add('meow-cursor-enabled')

  // 初始化到屏幕中间，避免首帧闪到 0,0
  targetX = window.innerWidth / 2
  targetY = window.innerHeight / 2
  currentX = targetX
  currentY = targetY
  pawX.value = currentX
  pawY.value = currentY
  lastMoveX = targetX
  lastMoveY = targetY

  window.addEventListener('mousemove', onMove, { passive: true })
  window.addEventListener('mousedown', onDown, { passive: true })
  window.addEventListener('mouseup', onUp, { passive: true })
  rafId = window.requestAnimationFrame(tick)
})

onBeforeUnmount(() => {
  window.removeEventListener('mousemove', onMove)
  window.removeEventListener('mousedown', onDown)
  window.removeEventListener('mouseup', onUp)
  if (rafId) window.cancelAnimationFrame(rafId)
  document.documentElement.classList.remove('meow-cursor-enabled')
})
</script>

<style scoped>
.fish-layer {
  position: fixed;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
  /* 放到内容之上，确保“小鱼干跟随”可见，但不影响交互 */
  z-index: 2147483646;
}

.fish {
  position: fixed;
  background-image: url('@/assets/fishbone.svg');
  background-repeat: no-repeat;
  background-position: center;
  background-size: contain;
  will-change: transform, opacity;
  animation-name: fishFloat;
  animation-timing-function: ease-out;
  animation-fill-mode: forwards;
}

@keyframes fishFloat {
  0% {
    opacity: 0.85;
    transform: translate(-50%, -50%) rotate(var(--r, 0deg)) scale(0.9);
  }
  100% {
    opacity: 0;
    transform: translate(calc(-50% + var(--dx, 18px)), calc(-50% + var(--dy, -22px)))
      rotate(var(--r, 0deg)) scale(1.25);
  }
}

.paw-cursor {
  position: fixed;
  left: 0;
  top: 0;
  width: 32px;
  height: 32px;
  background-image: url('@/assets/cursor-paw.png');
  background-repeat: no-repeat;
  background-position: center;
  background-size: contain;
  opacity: 0.95;
  will-change: transform;
}

.paw-cursor.down {
  transform-origin: center;
  filter: drop-shadow(0 2px 6px rgba(0, 0, 0, 0.15));
}

.cursor-layer {
  position: fixed;
  inset: 0;
  pointer-events: none;
  /* 确保猫爪永远在最上层，不会被 Header / 内容遮挡 */
  z-index: 2147483647;
}
</style>
