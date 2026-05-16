<template>
  <div class="app-container dashboard">
    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :xs="12" :sm="12" :md="6" v-for="card in statCards" :key="card.key">
        <div class="stat-card" :style="{ borderTopColor: card.color }">
          <div class="stat-icon" :style="{ background: card.color + '18', color: card.color }">
            <el-icon :size="28"><component :is="card.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats[card.key] ?? '—' }}</div>
            <div class="stat-label">{{ card.label }}</div>
          </div>
          <div v-if="card.subKey" class="stat-sub">
            <span>{{ card.subLabel }}</span>
            <span class="sub-val">+{{ stats[card.subKey] ?? 0 }}</span>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 第二行卡片 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :xs="12" :sm="12" :md="6" v-for="card in statCards2" :key="card.key">
        <div class="stat-card" :style="{ borderTopColor: card.color }">
          <div class="stat-icon" :style="{ background: card.color + '18', color: card.color }">
            <el-icon :size="28"><component :is="card.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats[card.key] ?? '—' }}</div>
            <div class="stat-label">{{ card.label }}</div>
          </div>
          <div v-if="card.subKey" class="stat-sub">
            <span>{{ card.subLabel }}</span>
            <span class="sub-val">{{ stats[card.subKey] ?? 0 }}</span>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 快捷入口 -->
    <el-row :gutter="16" style="margin-top:16px">
      <el-col :span="24">
        <el-card shadow="never">
          <template #header><span class="card-title">快捷入口</span></template>
          <div class="shortcut-grid">
            <router-link v-for="s in shortcuts" :key="s.path" :to="s.path" class="shortcut-item">
              <div class="shortcut-icon" :style="{ background: s.color + '18', color: s.color }">
                <el-icon :size="22"><component :is="s.icon" /></el-icon>
              </div>
              <span>{{ s.label }}</span>
            </router-link>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'

const stats = ref({})

const statCards = [
  { key: 'totalUsers',  subKey: 'todayUsers', label: '总用户数',  subLabel: '今日新增', color: '#409eff', icon: 'User' },
  { key: 'totalVideos', subKey: 'normalVideos', label: '短剧总数', subLabel: '已上架', color: '#67c23a', icon: 'VideoPlay' },
]
const statCards2 = [
  { key: 'todayOrders',  subKey: 'todayRevenue', label: '今日订单', subLabel: '今日收入 ¥', color: '#e6a23c', icon: 'ShoppingCart' },
  { key: 'pendingWithdraw', label: '待审核提现', color: '#f56c6c', icon: 'Money' },
]

const shortcuts = [
  { label: '视频管理', path: '/drama/video',        icon: 'VideoPlay', color: '#409eff' },
  { label: '分类管理', path: '/drama/category',     icon: 'Grid',      color: '#67c23a' },
  { label: '用户列表', path: '/member/list',         icon: 'User',      color: '#e6a23c' },
  { label: 'VIP套餐',  path: '/commerce/vip',        icon: 'Star',      color: '#9b59b6' },
  { label: 'VIP订单',  path: '/commerce/vip-order',  icon: 'Document',  color: '#1abc9c' },
  { label: '提现审核', path: '/commerce/withdraw',   icon: 'Money',     color: '#f56c6c' },
  { label: '钱包流水', path: '/commerce/wallet-log', icon: 'List',      color: '#95a5a6' },
  { label: '参数配置', path: '/system-config/storage', icon: 'Setting', color: '#34495e' },
]

async function loadStats() {
  try {
    const res = await request({ url: '/admin/system/stats', method: 'get', params: { siteId: 1 } })
    stats.value = res.data || {}
  } catch {}
}

onMounted(loadStats)
</script>

<style scoped lang="scss">
.dashboard { padding-bottom: 24px; }

.stat-row { margin-bottom: 16px; }

.stat-card {
  background: #fff;
  border-radius: 8px;
  border-top: 3px solid #409eff;
  padding: 20px 16px 14px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 1px 4px rgba(0,0,0,.06);
  position: relative;
  overflow: hidden;

  .stat-icon {
    width: 52px; height: 52px; border-radius: 12px;
    display: flex; align-items: center; justify-content: center;
    flex-shrink: 0;
  }
  .stat-info { flex: 1; }
  .stat-value { font-size: 28px; font-weight: 700; line-height: 1.2; color: #1f2d3d; }
  .stat-label { font-size: 13px; color: #909399; margin-top: 4px; }
  .stat-sub {
    position: absolute; bottom: 10px; right: 14px;
    font-size: 12px; color: #c0c4cc;
    .sub-val { color: #67c23a; margin-left: 4px; font-weight: 600; }
  }
}

.card-title { font-weight: 600; font-size: 14px; }

.shortcut-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 12px;
}
.shortcut-item {
  display: flex; flex-direction: column; align-items: center; gap: 8px;
  padding: 16px 8px; border-radius: 8px; text-decoration: none;
  color: #606266; font-size: 13px; cursor: pointer;
  transition: background .15s;
  &:hover { background: #f5f7fa; }
  .shortcut-icon {
    width: 44px; height: 44px; border-radius: 12px;
    display: flex; align-items: center; justify-content: center;
  }
}
</style>
