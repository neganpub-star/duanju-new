<template>
  <div class="navbar" :class="'nav' + settingsStore.navType">
    <hamburger id="hamburger-container" :is-active="appStore.sidebar.opened" class="hamburger-container" @toggleClick="toggleSideBar" />
    <breadcrumb v-if="settingsStore.navType == 1" id="breadcrumb-container" class="breadcrumb-container" />
    <top-nav v-if="settingsStore.navType == 2" id="topmenu-container" class="topmenu-container" />
    <template v-if="settingsStore.navType == 3">
      <logo v-show="settingsStore.sidebarLogo" :collapse="false"></logo>
      <top-bar id="topbar-container" class="topbar-container" />
    </template>

    <div class="right-menu">
      <template v-if="appStore.device !== 'mobile'">
        <header-search id="header-search" class="right-menu-item" />

        <screenfull id="screenfull" class="right-menu-item hover-effect" />

        <el-tooltip content="主题模式" effect="dark" placement="bottom">
          <div class="right-menu-item hover-effect theme-switch-wrapper" @click="toggleTheme">
            <svg-icon v-if="settingsStore.isDark" icon-class="sunny" />
            <svg-icon v-if="!settingsStore.isDark" icon-class="moon" />
          </div>
        </el-tooltip>

        <el-tooltip content="布局大小" effect="dark" placement="bottom">
          <size-select id="size-select" class="right-menu-item hover-effect" />
        </el-tooltip>

        <el-tooltip content="消息通知" effect="dark" placement="bottom">
          <header-notice id="header-notice" class="right-menu-item hover-effect" />
        </el-tooltip>
      </template>

      <el-dropdown @command="handleCommand" class="avatar-container right-menu-item hover-effect" trigger="hover" popper-class="user-dropdown">
        <div class="avatar-wrapper">
          <img :src="userStore.avatar" class="user-avatar" />
          <span class="user-nickname"> {{ userStore.nickName }} </span>
          <el-icon class="avatar-arrow"><CaretBottom /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu class="user-dropdown-menu">
            <!-- 顶部用户卡片 -->
            <div class="user-dropdown-header">
              <img :src="userStore.avatar" class="user-dropdown-avatar" />
              <div class="user-dropdown-info">
                <span class="user-dropdown-name">{{ userStore.nickName }}</span>
                <span class="user-dropdown-role">{{ userRoleLabel }}</span>
              </div>
            </div>

            <router-link to="/user/profile">
              <el-dropdown-item>
                <el-icon><User /></el-icon>
                <span>个人中心</span>
              </el-dropdown-item>
            </router-link>
            <el-dropdown-item command="setLayout" v-if="settingsStore.showSettings">
              <el-icon><Setting /></el-icon>
              <span>布局设置</span>
            </el-dropdown-item>
            <el-dropdown-item command="lockScreen">
              <el-icon><Lock /></el-icon>
              <span>锁定屏幕</span>
            </el-dropdown-item>
            <el-dropdown-item divided command="logout" class="logout-item">
              <el-icon><SwitchButton /></el-icon>
              <span>退出登录</span>
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
import { ElMessageBox } from 'element-plus'
import { CaretBottom, User, Setting, Lock, SwitchButton } from '@element-plus/icons-vue'
import Breadcrumb from '@/components/Breadcrumb'
import TopNav from './TopNav'
import TopBar from './TopBar'
import Logo from './Sidebar/Logo'
import Hamburger from '@/components/Hamburger'
import Screenfull from '@/components/Screenfull'
import SizeSelect from '@/components/SizeSelect'
import HeaderSearch from '@/components/HeaderSearch'
import useAppStore from '@/store/modules/app'
import useUserStore from '@/store/modules/user'
import useLockStore from '@/store/modules/lock'
import useSettingsStore from '@/store/modules/settings'
import HeaderNotice from './HeaderNotice'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()
const lockStore = useLockStore()
const settingsStore = useSettingsStore()

// 角色文案：admin → 超级管理员；其他取首个角色名；都没有则展示 "用户"
const userRoleLabel = computed(() => {
  const roles = userStore.roles || []
  if (roles.includes('admin')) return '超级管理员'
  if (roles.length) return roles[0]
  return '用户'
})

function toggleSideBar() {
  appStore.toggleSideBar()
}

function handleCommand(command) {
  switch (command) {
    case "setLayout":
      setLayout()
      break
    case "lockScreen":
      lockScreen()
      break
    case "logout":
      logout()
      break
    default:
      break
  }
}

function logout() {
  ElMessageBox.confirm('确定注销并退出系统吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.logOut().then(() => {
      location.href = '/login'
    })
  }).catch(() => { })
}

const emits = defineEmits(['setLayout'])
function setLayout() {
  emits('setLayout')
}

function lockScreen() {
  const currentPath = route.fullPath
  lockStore.lockScreen(currentPath)
  router.push('/lock')
}

async function toggleTheme(event) {
  const x = event?.clientX || window.innerWidth / 2
  const y = event?.clientY || window.innerHeight / 2
  const wasDark = settingsStore.isDark

  const isReducedMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches
  const isSupported = document.startViewTransition && !isReducedMotion

  if (!isSupported) {
    settingsStore.toggleTheme()
    return
  }

  try {
    const transition = document.startViewTransition(async () => {
      await new Promise((resolve) => setTimeout(resolve, 10))
      settingsStore.toggleTheme()
      await nextTick()
    })
    await transition.ready

    const endRadius = Math.hypot(Math.max(x, window.innerWidth - x), Math.max(y, window.innerHeight - y))
    const clipPath = [`circle(0px at ${x}px ${y}px)`, `circle(${endRadius}px at ${x}px ${y}px)`]
    document.documentElement.animate(
      {
        clipPath: !wasDark ? [...clipPath].reverse() : clipPath
      }, {
        duration: 650,
        easing: "cubic-bezier(0.4, 0, 0.2, 1)",
        fill: "forwards",
        pseudoElement: !wasDark ? "::view-transition-old(root)" : "::view-transition-new(root)"
      }
    )
    await transition.finished
  } catch (error) {
    console.warn("View transition failed, falling back to immediate toggle:", error)
    settingsStore.toggleTheme()
  }
}
</script>

<style lang='scss' scoped>
.navbar.nav3 {
  .hamburger-container {
    display: none !important;
  }
}

.navbar {
  height: 54px;
  overflow: hidden;
  position: relative;
  background: var(--navbar-bg);
  border-bottom: 1px solid #ebeef5;
  box-shadow: 0 1px 8px rgba(0, 21, 41, 0.04);
  display: flex;
  align-items: center;
  padding: 0 4px 0 8px;
  box-sizing: border-box;

  /* hamburger 折叠按钮：与右侧 item 统一为胶囊 */
  .hamburger-container {
    cursor: pointer;
    transition: background 0.2s, color 0.2s;
    -webkit-tap-highlight-color: transparent;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 36px;
    height: 36px;
    flex-shrink: 0;
    margin-right: 8px;
    border-radius: 10px;
    color: #5a5e66;
    line-height: 1;

    :deep(.hamburger) {
      width: 18px;
      height: 18px;
      transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    }

    &:hover {
      background: rgba(80, 72, 229, 0.08);
      color: #5048e5;
    }
  }

  .breadcrumb-container {
    flex-shrink: 0;
  }

  .topmenu-container {
    position: absolute;
    left: 50px;
  }

  .topbar-container {
    flex: 1;
    min-width: 0;
    display: flex;
    align-items: center;
    overflow: hidden;
    margin-left: 8px;
  }

  .right-menu {
    height: 100%;
    display: flex;
    align-items: center;
    gap: 4px;
    margin-left: auto;
    padding-right: 12px;

    &:focus {
      outline: none;
    }

    /* 每个 icon 按钮：统一胶囊风 */
    .right-menu-item {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      width: 36px;
      height: 36px;
      padding: 0;
      line-height: 1;
      font-size: 17px;
      color: #5a5e66;
      border-radius: 10px;
      transition: background 0.2s, color 0.2s, transform 0.2s;

      &.hover-effect {
        cursor: pointer;

        &:hover {
          background: rgba(80, 72, 229, 0.08);
          color: #5048e5;
        }

        &:active {
          transform: scale(0.94);
        }
      }

      &.theme-switch-wrapper {
        display: inline-flex;
        align-items: center;
        justify-content: center;

        svg {
          width: 17px;
          height: 17px;
          transition: transform 0.4s cubic-bezier(0.4, 0, 0.2, 1);
        }

        &:hover svg {
          transform: rotate(30deg) scale(1.1);
        }
      }
    }

    /* 用户头像下拉 */
    .avatar-container {
      margin-left: 4px;
      padding: 0;
      width: auto;
      height: 36px;
      border-radius: 18px;
      transition: background 0.2s;

      &:hover {
        background: rgba(80, 72, 229, 0.08);

        .avatar-arrow { transform: rotate(180deg); color: #5048e5; }
      }

      .avatar-wrapper {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 0 12px 0 4px;
        height: 36px;
        margin: 0;
        right: 0;
        cursor: pointer;

        .user-avatar {
          width: 28px;
          height: 28px;
          margin: 0;
          border-radius: 50%;
          border: 2px solid rgba(80, 72, 229, 0.18);
          box-sizing: border-box;
        }

        .user-nickname {
          position: static;
          font-size: 13px;
          font-weight: 600;
          color: #303133;
          line-height: 1;
        }

        .avatar-arrow {
          font-size: 12px;
          color: #909399;
          transition: transform 0.25s, color 0.2s;
        }

        i { display: none; }
      }
    }
  }
}

/* ============ 用户下拉菜单（全局，popper 在 body 上） ============ */
:global(.user-dropdown.el-popper) {
  border-radius: 14px !important;
  padding: 0 !important;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12) !important;
  border: 1px solid rgba(80, 72, 229, 0.08) !important;
}
:global(.user-dropdown .el-dropdown-menu) {
  padding: 6px;
  background: #fff;
}

/* 顶部用户卡片 */
:global(.user-dropdown .user-dropdown-header) {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 14px;
  margin: -6px -6px 4px;
  background: linear-gradient(135deg, #eef2ff 0%, #f8f9ff 100%);
  border-bottom: 1px solid #f0f0f0;
}
:global(.user-dropdown .user-dropdown-avatar) {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  border: 2px solid #fff;
  box-shadow: 0 2px 8px rgba(80, 72, 229, 0.18);
}
:global(.user-dropdown .user-dropdown-info) {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}
:global(.user-dropdown .user-dropdown-name) {
  font-size: 14px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
}
:global(.user-dropdown .user-dropdown-role) {
  font-size: 11px;
  color: #5048e5;
  background: rgba(80, 72, 229, 0.1);
  padding: 2px 8px;
  border-radius: 8px;
  width: fit-content;
  font-weight: 600;
}

/* 菜单项 */
:global(.user-dropdown .el-dropdown-menu__item) {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 9px 14px;
  margin: 2px 0;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
  color: #5a5e66;
  transition: background 0.15s, color 0.15s;
}
:global(.user-dropdown .el-dropdown-menu__item .el-icon) {
  font-size: 16px;
  color: #909399;
  transition: color 0.15s;
}
:global(.user-dropdown .el-dropdown-menu__item:not(.is-disabled):hover) {
  background: rgba(80, 72, 229, 0.08);
  color: #5048e5;
}
:global(.user-dropdown .el-dropdown-menu__item:not(.is-disabled):hover .el-icon) {
  color: #5048e5;
}
:global(.user-dropdown a) { text-decoration: none; }
:global(.user-dropdown .el-dropdown-menu__item--divided) {
  margin-top: 4px;
  padding-top: 9px;
  border-top: 1px solid #f0f0f0;
}
:global(.user-dropdown .el-dropdown-menu__item--divided::before) { display: none; }
:global(.user-dropdown .logout-item) { color: #f56c6c; }
:global(.user-dropdown .logout-item .el-icon) { color: #f56c6c; }
:global(.user-dropdown .logout-item:not(.is-disabled):hover) {
  background: rgba(245, 108, 108, 0.08);
  color: #f56c6c;
}
:global(.user-dropdown .logout-item:not(.is-disabled):hover .el-icon) { color: #f56c6c; }

/* 暗黑模式 */
:global(html.dark .user-dropdown.el-popper) {
  border-color: rgba(129, 140, 248, 0.18) !important;
  background: #1f1f1f;
}
:global(html.dark .user-dropdown .el-dropdown-menu) { background: #1f1f1f; }
:global(html.dark .user-dropdown .user-dropdown-header) {
  background: linear-gradient(135deg, #1f1f2f 0%, #1a1a1a 100%);
  border-bottom-color: #2c2c2c;
}
:global(html.dark .user-dropdown .user-dropdown-name) { color: #e5e7eb; }
:global(html.dark .user-dropdown .el-dropdown-menu__item) { color: #cfcfcf; }
:global(html.dark .user-dropdown .el-dropdown-menu__item:not(.is-disabled):hover) {
  background: rgba(129, 140, 248, 0.16);
  color: #a5b4fc;
}
:global(html.dark .user-dropdown .el-dropdown-menu__item:not(.is-disabled):hover .el-icon) { color: #a5b4fc; }
:global(html.dark .user-dropdown .el-dropdown-menu__item--divided) { border-top-color: #2c2c2c; }

/* 暗黑模式 navbar */
html.dark .navbar {
  border-bottom-color: #2c2c2c;
  .hamburger-container {
    color: #cfcfcf;
    &:hover { background: rgba(129, 140, 248, 0.16); color: #a5b4fc; }
  }
  .right-menu {
    .right-menu-item {
      color: #cfcfcf;
      &.hover-effect:hover {
        background: rgba(129, 140, 248, 0.16);
        color: #a5b4fc;
      }
    }
    .avatar-container:hover { background: rgba(129, 140, 248, 0.16); }
    .avatar-container .user-nickname { color: #e5e7eb; }
  }
}
</style>
