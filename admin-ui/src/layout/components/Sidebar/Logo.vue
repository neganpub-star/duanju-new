<template>
  <div class="sidebar-logo-container" :class="{ 'collapse': collapse }">
    <transition name="sidebarLogoFade">
      <router-link key="logo" class="sidebar-logo-link" to="/">
        <img v-if="logo" :src="logo" class="sidebar-logo" />
      </router-link>
    </transition>
  </div>
</template>

<script setup>
import logo from '@/assets/logo/logo.png'
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
  height: 56px;
  line-height: 56px;
  background: v-bind(getLogoBackground);
  text-align: center;
  overflow: hidden;

  // 底部柔和分隔线
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
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;

    & .sidebar-logo {
      width: 32px;
      height: 32px;
      vertical-align: middle;
      border-radius: 8px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.25);
      transition: transform 0.25s;
    }
    &:hover .sidebar-logo {
      transform: scale(1.06);
    }

  }

  &.collapse {
    .sidebar-logo-link { gap: 0; }
    .sidebar-logo { margin-right: 0; }
  }
}
</style>