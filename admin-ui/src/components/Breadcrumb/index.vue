<template>
  <el-breadcrumb class="app-breadcrumb" :separator-icon="ArrowRight">
    <transition-group name="breadcrumb">
      <el-breadcrumb-item v-for="(item, index) in levelList" :key="item.path">
        <span
          v-if="item.redirect === 'noRedirect' || index == levelList.length - 1"
          class="bc-current"
        >
          <el-icon v-if="index === 0" class="bc-icon"><HomeFilled /></el-icon>
          <span>{{ item.meta.title }}</span>
        </span>
        <a v-else class="bc-link" @click.prevent="handleLink(item)">
          <el-icon v-if="index === 0" class="bc-icon"><HomeFilled /></el-icon>
          <span>{{ item.meta.title }}</span>
        </a>
      </el-breadcrumb-item>
    </transition-group>
  </el-breadcrumb>
</template>

<script setup>
import usePermissionStore from '@/store/modules/permission'
import { ArrowRight, HomeFilled } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const permissionStore = usePermissionStore()
const levelList = ref([])

function getBreadcrumb() {
  // only show routes with meta.title
  let matched = []
  const pathNum = findPathNum(route.path)
  // multi-level menu
  if (pathNum > 2) {
    const reg = /\/\w+/gi
    const pathList = route.path.match(reg).map((item, index) => {
      if (index !== 0) item = item.slice(1)
      return item
    })
    getMatched(pathList, permissionStore.defaultRoutes, matched)
  } else {
    matched = route.matched.filter((item) => item.meta && item.meta.title)
  }
  // 判断是否为首页
  if (!isDashboard(matched[0])) {
    matched = [{ path: "/index", meta: { title: "首页" } }].concat(matched)
  }
  levelList.value = matched.filter(item => item.meta && item.meta.title && item.meta.breadcrumb !== false)
}
function findPathNum(str, char = "/") {
  let index = str.indexOf(char)
  let num = 0
  while (index !== -1) {
    num++
    index = str.indexOf(char, index + 1)
  }
  return num
}
function getMatched(pathList, routeList, matched) {
  let data = routeList.find(item => item.path == pathList[0] || (item.name += '').toLowerCase() == pathList[0])
  if (data) {
    matched.push(data)
    if (data.children && pathList.length) {
      pathList.shift()
      getMatched(pathList, data.children, matched)
    }
  }
}
function isDashboard(route) {
  const name = route && route.name
  if (!name) {
    return false
  }
  return name.trim() === 'Index'
}
function handleLink(item) {
  const { redirect, path } = item
  if (redirect) {
    router.push(redirect)
    return
  }
  router.push(path)
}

watchEffect(() => {
  // if you go to the redirect page, do not update the breadcrumbs
  if (route.path.startsWith('/redirect/')) {
    return
  }
  getBreadcrumb()
})
getBreadcrumb()
</script>

<style lang='scss' scoped>
.app-breadcrumb.el-breadcrumb {
  display: inline-flex;
  align-items: center;
  font-size: 14px;
  line-height: 54px;

  /* 分隔符 chevron */
  :deep(.el-breadcrumb__separator) {
    color: #c0c4cc;
    margin: 0 8px;
    font-weight: 400;
  }
  :deep(.el-breadcrumb__separator.el-icon) {
    width: 13px;
    height: 13px;
  }

  /* 中间可点击项 */
  .bc-link {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    color: #606266;
    font-weight: 500;
    padding: 4px 8px;
    border-radius: 6px;
    transition: background 0.2s, color 0.2s;
    text-decoration: none;
    cursor: pointer;

    &:hover {
      background: rgba(80, 72, 229, 0.08);
      color: #5048e5;
    }
  }

  /* 当前项（末尾） */
  .bc-current {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    color: #303133;
    font-weight: 600;
    padding: 4px 0;
    cursor: text;
  }

  /* 首页 icon */
  .bc-icon {
    font-size: 14px;
    color: #5048e5;
  }
}

/* 暗黑模式 */
html.dark .app-breadcrumb.el-breadcrumb {
  :deep(.el-breadcrumb__separator) { color: #5a5e66; }
  .bc-link { color: #cfcfcf; &:hover { background: rgba(129, 140, 248, 0.16); color: #a5b4fc; } }
  .bc-current { color: #e5e7eb; }
  .bc-icon { color: #a5b4fc; }
}
</style>