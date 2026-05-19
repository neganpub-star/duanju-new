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

    <!-- 资金流水统计：今日/本周/本月/本年 × 订单收入/余额入账/已打款提现 -->
    <div class="fund-section">
      <div class="section-title">
        <el-icon><Money /></el-icon>
        <span>资金流水</span>
      </div>
      <div class="fund-cards">
        <div
          v-for="(item, idx) in fundCards"
          :key="item.label"
          class="fund-card"
          :class="`fund-card--${idx}`"
        >
          <div class="fund-header">
            <div class="fund-icon"><el-icon><component :is="item.icon" /></el-icon></div>
            <div class="fund-period">{{ item.label }}</div>
          </div>
          <div class="fund-row fund-row--primary">
            <span class="fund-sub">订单收入</span>
            <span class="fund-num revenue">¥{{ formatMoney(item.revenue) }}</span>
          </div>
          <div class="fund-row">
            <span class="fund-sub">余额入账</span>
            <span class="fund-num income">¥{{ formatMoney(item.income) }}</span>
          </div>
          <div class="fund-row">
            <span class="fund-sub">已打款</span>
            <span class="fund-num withdraw">¥{{ formatMoney(item.withdraw) }}</span>
          </div>
        </div>
      </div>
    </div>

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
import { ref, computed, onMounted } from 'vue'
import request from '@/utils/request'
import { Sunny, Calendar, DataLine, TrendCharts, Money } from '@element-plus/icons-vue'

const stats = ref({})
const fundStats = ref({})

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

const fundCards = computed(() => [
  { label: '今日', icon: Sunny,       revenue: fundStats.value.todayRevenue, income: fundStats.value.todayIncome, withdraw: fundStats.value.todayWithdraw },
  { label: '本周', icon: Calendar,    revenue: fundStats.value.weekRevenue,  income: fundStats.value.weekIncome,  withdraw: fundStats.value.weekWithdraw },
  { label: '本月', icon: DataLine,    revenue: fundStats.value.monthRevenue, income: fundStats.value.monthIncome, withdraw: fundStats.value.monthWithdraw },
  { label: '本年', icon: TrendCharts, revenue: fundStats.value.yearRevenue,  income: fundStats.value.yearIncome,  withdraw: fundStats.value.yearWithdraw },
])

function formatMoney(v) {
  const n = Number(v) || 0
  return n.toFixed(2)
}

async function loadStats() {
  try {
    const res = await request({ url: '/admin/system/stats', method: 'get', params: { siteId: 1 } })
    stats.value = res.data || {}
  } catch {}
}

async function loadFundStats() {
  try {
    const res = await request({ url: '/admin/commerce/wallet/stats', method: 'get', params: { siteId: 1 } })
    fundStats.value = res.data || {}
  } catch {}
}

onMounted(() => {
  loadStats()
  loadFundStats()
})
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

/* 资金流水统计 */
.fund-section {
  margin-bottom: 16px;
}
.section-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin: 4px 0 10px;
}
.fund-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}
.fund-card {
  position: relative;
  overflow: hidden;
  background: #fff;
  border-radius: 10px;
  padding: 14px 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: transform 0.2s, box-shadow 0.2s;
}
.fund-card:hover { transform: translateY(-1px); box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08); }
.fund-card::before {
  content: '';
  position: absolute; inset: 0;
  opacity: 0.05;
  pointer-events: none;
}
.fund-card--0::before { background: linear-gradient(135deg, #4facfe, #00f2fe); }
.fund-card--1::before { background: linear-gradient(135deg, #43e97b, #38f9d7); }
.fund-card--2::before { background: linear-gradient(135deg, #fa709a, #fee140); }
.fund-card--3::before { background: linear-gradient(135deg, #f5576c, #fa709a); }

.fund-header {
  display: flex; align-items: center; gap: 8px;
  margin-bottom: 10px;
}
.fund-icon {
  width: 28px; height: 28px;
  border-radius: 8px;
  display: flex; align-items: center; justify-content: center;
  color: #fff; font-size: 15px;
  flex-shrink: 0;
}
.fund-card--0 .fund-icon { background: linear-gradient(135deg, #4facfe, #00f2fe); }
.fund-card--1 .fund-icon { background: linear-gradient(135deg, #43e97b, #38f9d7); }
.fund-card--2 .fund-icon { background: linear-gradient(135deg, #fa709a, #fee140); }
.fund-card--3 .fund-icon { background: linear-gradient(135deg, #f5576c, #fa709a); }
.fund-period { font-size: 13px; font-weight: 600; color: #303133; }

.fund-row {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  padding: 3px 0;
  font-size: 12px;
}
.fund-row--primary {
  padding-bottom: 6px;
  border-bottom: 1px dashed #f0f0f0;
  margin-bottom: 4px;
}
.fund-sub { color: #909399; }
.fund-num { font-weight: 700; font-variant-numeric: tabular-nums; }
.fund-row--primary .fund-num { font-size: 17px; }
.fund-row:not(.fund-row--primary) .fund-num { font-size: 13px; }
.fund-num.revenue  { color: #5048e5; }
.fund-num.income   { color: #67c23a; }
.fund-num.withdraw { color: #f56c6c; }

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

/* ============ 暗黑模式适配 ============ */
html.dark {
  .stat-card,
  .fund-card,
  :deep(.el-card),
  :deep(.box-card) {
    background: #1f1f1f;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.25);
  }
  .stat-value { color: #e5e7eb; }
  .stat-label { color: #909399; }
  .stat-sub { color: #6e6e6e; }

  .section-title { color: #e5e7eb; }
  .card-title { color: #e5e7eb; }

  .fund-period { color: #e5e7eb; }
  .fund-sub { color: #909399; }
  .fund-row--primary { border-bottom-color: #2c2c2c; }
  .fund-card::before { opacity: 0.10; }

  .shortcut-item {
    color: #cfcfcf;
    &:hover { background: rgba(129, 140, 248, 0.10); }
  }

  /* el-card 头部 */
  :deep(.el-card__header) {
    background: #1f1f1f;
    border-bottom-color: #2c2c2c;
    color: #e5e7eb;
  }
  :deep(.el-card__body) { color: #cfcfcf; }
}
</style>
