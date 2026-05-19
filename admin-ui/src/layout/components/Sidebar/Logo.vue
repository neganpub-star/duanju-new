<template>
  <div class="sidebar-logo-container" :class="{ 'collapse': collapse }">
    <router-link key="logo" class="sidebar-logo-link" to="/">
      <div class="sidebar-logo-wrap">
        <img v-if="logo" :src="logo" class="sidebar-logo" />
        <span class="sidebar-logo-glow"></span>
      </div>
      <transition name="brandFade">
        <div v-if="!collapse" class="sidebar-brand">
          <div class="brand-title">
            <span class="brand-title-cn">短剧后台</span>
            <span class="brand-title-badge">v2</span>
          </div>
          <div class="brand-subtitle">
            <span class="brand-line"></span>
            <span>DRAMA · ADMIN</span>
            <span class="brand-line"></span>
          </div>
        </div>
      </transition>
    </router-link>
  </div>
</template>

<script setup>
import logo from '@/assets/logo/logo.svg'
import useSettingsStore from '@/store/modules/settings'
import variables from '@/assets/styles/variables.module.scss'

defineProps({
  collapse: {
    type: Boolean,
    required: true
  }
})

const title = import.meta.env.VITE_APP_TITLE
const settingsStore = useSettingsStore()
const sideTheme = computed(() => settingsStore.sideTheme)

// 获取Logo背景色
const getLogoBackground = computed(() => {
  if (settingsStore.isDark) {
    return 'var(--sidebar-bg)'
  }
  if (settingsStore.navType == 3) {
    return variables.menuLightBg
  }
  return sideTheme.value === 'theme-dark' ? variables.menuBg : variables.menuLightBg
})

// 获取Logo文字颜色
const getLogoTextColor = computed(() => {
  if (settingsStore.isDark) {
    return 'var(--sidebar-logo-text)'
  }
  if (settingsStore.navType == 3) {
    return variables.menuLightText
  }
  return sideTheme.value === 'theme-dark' ? '#fff' : variables.menuLightText
})

// Logo 底部分隔线颜色（浅/暗主题分别可见）
const getLogoDivider = computed(() => {
  if (settingsStore.isDark) return 'rgba(255, 255, 255, 0.08)'
  return sideTheme.value === 'theme-dark' ? 'rgba(255, 255, 255, 0.08)' : 'rgba(0, 0, 0, 0.08)'
})
</script>

<style lang="scss" scoped>
.sidebarLogoFade-enter-active {
  transition: opacity 1.5s;
}

.sidebarLogoFade-enter,
.sidebarLogoFade-leave-to {
  opacity: 0;
}

.sidebar-logo-container {
  position: relative;
  height: 64px;
  background: v-bind(getLogoBackground);
  overflow: hidden;

  /* 顶部高光线 */
  &::before {
    content: '';
    position: absolute;
    left: 12px; right: 12px; top: 0;
    height: 2px;
    background: linear-gradient(90deg, transparent, #5048e5 30%, #ec4899 70%, transparent);
    opacity: 0.7;
    border-radius: 0 0 2px 2px;
  }

  /* 底部柔和分隔线 */
  &::after {
    content: '';
    position: absolute;
    left: 12px;
    right: 12px;
    bottom: 0;
    height: 1px;
    background: linear-gradient(90deg,
      transparent 0%,
      v-bind(getLogoDivider) 50%,
      transparent 100%);
  }

  & .sidebar-logo-link {
    height: 100%;
    width: 100%;
    /* sidebar.scss 全局 a { display: inline-block } 优先级更高，必须 !important */
    display: flex !important;
    flex-direction: row;
    flex-wrap: nowrap;
    align-items: center;
    justify-content: flex-start;
    gap: 12px;
    padding: 0 16px;
    text-decoration: none;
    box-sizing: border-box;
    overflow: hidden;
  }

  /* logo 包装：附加柔光 */
  .sidebar-logo-wrap {
    position: relative;
    width: 38px;
    height: 38px;
    flex-shrink: 0;
    flex-grow: 0;
  }
  .sidebar-logo-glow {
    position: absolute;
    inset: -4px;
    border-radius: 14px;
    background: radial-gradient(closest-side, rgba(236, 72, 153, 0.35), transparent 70%);
    filter: blur(6px);
    z-index: 0;
    pointer-events: none;
  }
  .sidebar-logo {
    position: relative;
    z-index: 1;
    width: 38px;
    height: 38px;
    border-radius: 11px;
    box-shadow:
      0 2px 8px rgba(80, 72, 229, 0.30),
      0 6px 20px rgba(236, 72, 153, 0.20);
    transition: transform 0.25s, box-shadow 0.25s;
  }
  & .sidebar-logo-link:hover {
    .sidebar-logo {
      transform: scale(1.08) rotate(-3deg);
      box-shadow:
        0 4px 12px rgba(80, 72, 229, 0.45),
        0 10px 28px rgba(236, 72, 153, 0.30);
    }
    .sidebar-logo-glow { opacity: 1.2; }
  }

  /* 品牌文字 */
  .sidebar-brand {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    justify-content: center;
    gap: 3px;
    line-height: 1.1;
    white-space: nowrap;
    overflow: hidden;
    min-width: 0;
    flex: 1 1 auto;
  }
  .brand-title {
    display: flex;
    align-items: center;
    gap: 6px;
  }
  .brand-title-cn {
    font-size: 16px;
    font-weight: 700;
    color: v-bind(getLogoTextColor);
    letter-spacing: 1px;
    line-height: 1;
  }
  .brand-title-badge {
    font-size: 9px;
    font-weight: 700;
    color: #fff;
    padding: 1px 5px;
    border-radius: 4px;
    background: linear-gradient(135deg, #5048e5, #ec4899);
    letter-spacing: 0.5px;
    line-height: 1.2;
  }
  .brand-subtitle {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 9px;
    font-weight: 600;
    color: v-bind(getLogoTextColor);
    opacity: 0.5;
    letter-spacing: 1.6px;
  }
  .brand-line {
    flex: 1;
    height: 1px;
    background: currentColor;
    opacity: 0.4;
    max-width: 14px;
  }

  /* brand 文字淡入淡出（与 sidebar 折叠联动） */
  .brandFade-enter-active,
  .brandFade-leave-active {
    transition: opacity 0.25s, transform 0.25s;
  }
  .brandFade-enter-from,
  .brandFade-leave-to {
    opacity: 0;
    transform: translateX(-6px);
  }

  &.collapse {
    .sidebar-logo-link {
      gap: 0;
      padding: 0;
      justify-content: center !important;
    }
  }
}
</style>