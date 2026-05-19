<template>
  <div class="app-container vip-order-page">
    <!-- 顶部统计卡片 -->
    <div class="stat-cards">
      <div class="stat-card stat-card--total">
        <div class="stat-icon"><el-icon><Tickets /></el-icon></div>
        <div class="stat-body">
          <div class="stat-label">订单总数</div>
          <div class="stat-value">{{ stats.totalCount }}</div>
        </div>
      </div>
      <div class="stat-card stat-card--paid">
        <div class="stat-icon"><el-icon><CircleCheck /></el-icon></div>
        <div class="stat-body">
          <div class="stat-label">已支付</div>
          <div class="stat-value">{{ stats.paidCount }}</div>
        </div>
      </div>
      <div class="stat-card stat-card--pending">
        <div class="stat-icon"><el-icon><Clock /></el-icon></div>
        <div class="stat-body">
          <div class="stat-label">待支付</div>
          <div class="stat-value">{{ stats.pendingCount }}</div>
        </div>
      </div>
      <div class="stat-card stat-card--gmv">
        <div class="stat-icon"><el-icon><Money /></el-icon></div>
        <div class="stat-body">
          <div class="stat-label">累计 GMV</div>
          <div class="stat-value">¥{{ formatMoney(stats.gmv) }}</div>
        </div>
      </div>
    </div>

    <!-- 搜索/工具栏 -->
    <div class="toolbar">
      <el-form :model="queryParams" :inline="true" ref="queryRef" class="toolbar-form">
        <el-form-item label="手机号" prop="mobile">
          <el-input v-model="queryParams.mobile" placeholder="用户手机号" clearable style="width:160px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="queryParams.nickname" placeholder="用户昵称" clearable style="width:150px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="全部" clearable style="width:120px">
            <el-option label="待支付" :value="0" />
            <el-option label="已支付" :value="1" />
            <el-option label="已完成" :value="2" />
            <el-option label="已取消" :value="-1" />
            <el-option label="已关闭" :value="-2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleQuery">搜索</el-button>
          <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 表格 -->
    <el-table v-loading="loading" :data="list" stripe class="vip-order-table" :header-cell-style="headerStyle">
      <el-table-column label="订单号" min-width="220">
        <template #default="{ row }">
          <div class="order-sn">
            <span class="sn-text">{{ row.orderSn }}</span>
            <el-tooltip content="复制订单号" placement="top">
              <el-icon class="sn-copy" @click="copyText(row.orderSn)"><CopyDocument /></el-icon>
            </el-tooltip>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="用户" min-width="180">
        <template #default="{ row }">
          <div class="user-cell">
            <el-image v-if="row.avatar" :src="row.avatar" class="user-avatar" fit="cover">
              <template #error><div class="avatar-empty" title="头像加载失败">无</div></template>
              <template #placeholder><div class="avatar-empty">无</div></template>
            </el-image>
            <div v-else class="avatar-empty" title="未设置头像">无</div>
            <div class="user-info">
              <span class="user-name">{{ row.nickname || '—' }}</span>
              <span class="user-mobile">{{ row.mobile || 'ID:' + row.userId }}</span>
            </div>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="天数" width="100" align="center">
        <template #default="{ row }">
          <div class="days-badge">
            <span class="days-num">{{ row.days }}</span>
            <span class="days-unit">天</span>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="实付" width="120" align="center">
        <template #default="{ row }">
          <span class="money-text">¥{{ row.payFee }}</span>
          <span v-if="row.totalFee && Number(row.totalFee) !== Number(row.payFee)" class="money-original">
            ¥{{ row.totalFee }}
          </span>
        </template>
      </el-table-column>

      <el-table-column label="支付方式" width="120" align="center">
        <template #default="{ row }">
          <span v-if="row.payType" class="pay-chip" :class="`pay-${row.payType}`">
            <el-icon class="pay-icon"><component :is="payIcon(row.payType)" /></el-icon>
            {{ payLabel(row.payType) }}
          </span>
          <span v-else class="text-muted">—</span>
        </template>
      </el-table-column>

      <el-table-column label="状态" width="110" align="center">
        <template #default="{ row }">
          <el-tag :type="statusTag(row.status).type" effect="light" size="small" class="status-tag">
            <el-icon class="status-icon"><component :is="statusTag(row.status).icon" /></el-icon>
            <span>{{ statusTag(row.status).label }}</span>
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="支付时间" width="170" align="center">
        <template #default="{ row }">
          <span v-if="row.payTime" class="time-text">{{ row.payTime }}</span>
          <span v-else class="text-muted">—</span>
        </template>
      </el-table-column>

      <el-table-column label="创建时间" width="170" align="center">
        <template #default="{ row }">
          <span class="time-text">{{ row.createTime }}</span>
        </template>
      </el-table-column>

      <template #empty>
        <div class="empty-state">
          <el-icon class="empty-icon"><Tickets /></el-icon>
          <div class="empty-text">暂无订单数据</div>
        </div>
      </template>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script setup>
import { listVipOrder, statsVipOrder } from '@/api/commerce/vip'
import { ElMessage } from 'element-plus'
import {
  Search, Refresh, Tickets, CircleCheck, Clock, Money, CopyDocument,
  ChatDotRound, CreditCard, Wallet, Coin, Setting, CircleClose, WarningFilled, SuccessFilled,
} from '@element-plus/icons-vue'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const queryParams = reactive({ pageNum: 1, pageSize: 10, siteId: 1, mobile: '', nickname: '', status: '' })
const queryRef = ref()

// 表头样式
const headerStyle = { background: '#f7f8fa', color: '#303133', fontWeight: 600 }

// 全量统计（与筛选条件联动，由后端聚合）
const stats = ref({ totalCount: 0, paidCount: 0, pendingCount: 0, canceledCount: 0, gmv: 0 })
function formatMoney(v) {
  const n = Number(v) || 0
  return n.toFixed(2)
}

// 支付方式映射
const PAY_LABEL = { wechat: '微信', alipay: '支付宝', wallet: '余额', score: '积分', cryptocard: '加密卡', system: '系统' }
const PAY_ICON  = { wechat: ChatDotRound, alipay: CreditCard, wallet: Wallet, score: Coin, cryptocard: CreditCard, system: Setting }
function payLabel(t) { return PAY_LABEL[t] || t }
function payIcon(t)  { return PAY_ICON[t] || CreditCard }

// 状态映射
function statusTag(s) {
  switch (s) {
    case 0:  return { type: 'warning', icon: Clock,        label: '待支付' }
    case 1:  return { type: 'success', icon: CircleCheck,  label: '已支付' }
    case 2:  return { type: 'success', icon: SuccessFilled, label: '已完成' }
    case -1: return { type: 'info',    icon: CircleClose,  label: '已取消' }
    case -2: return { type: 'danger',  icon: WarningFilled, label: '已关闭' }
    default: return { type: 'info',    icon: CircleClose,  label: '未知' }
  }
}

async function getList() {
  loading.value = true
  try {
    // 并发拉取列表 + 全量统计（统计条件与列表一致）
    const statsParams = {
      siteId: queryParams.siteId,
      status: queryParams.status,
      mobile: queryParams.mobile,
      nickname: queryParams.nickname,
    }
    const [listRes, statsRes] = await Promise.all([
      listVipOrder(queryParams),
      statsVipOrder(statsParams),
    ])
    list.value = listRes.data.rows
    total.value = listRes.data.total
    stats.value = statsRes.data || { totalCount: 0, paidCount: 0, pendingCount: 0, canceledCount: 0, gmv: 0 }
  } finally {
    loading.value = false
  }
}

function handleQuery() { queryParams.pageNum = 1; getList() }
function resetQuery() { queryRef.value?.resetFields(); handleQuery() }

async function copyText(text) {
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success('已复制订单号')
  } catch {
    ElMessage.warning('复制失败，请手动选择')
  }
}

getList()
</script>

<style scoped>
.vip-order-page { padding: 16px; }

/* 统计卡片 */
.stat-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}
.stat-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 20px;
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  transition: transform 0.2s, box-shadow 0.2s;
  position: relative;
  overflow: hidden;
}
.stat-card:hover { transform: translateY(-2px); box-shadow: 0 6px 24px rgba(0, 0, 0, 0.08); }
.stat-card::before {
  content: ''; position: absolute; inset: 0; opacity: 0.06; pointer-events: none;
}
.stat-card--total::before   { background: linear-gradient(135deg, #667eea, #764ba2); }
.stat-card--paid::before    { background: linear-gradient(135deg, #43e97b, #38f9d7); }
.stat-card--pending::before { background: linear-gradient(135deg, #fa709a, #fee140); }
.stat-card--gmv::before     { background: linear-gradient(135deg, #f5576c, #fa709a); }

.stat-icon {
  width: 44px; height: 44px; border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  font-size: 22px; color: #fff; flex-shrink: 0;
}
.stat-card--total   .stat-icon { background: linear-gradient(135deg, #667eea, #764ba2); }
.stat-card--paid    .stat-icon { background: linear-gradient(135deg, #43e97b, #38f9d7); }
.stat-card--pending .stat-icon { background: linear-gradient(135deg, #fa709a, #fee140); }
.stat-card--gmv     .stat-icon { background: linear-gradient(135deg, #f5576c, #fa709a); }

.stat-body { display: flex; flex-direction: column; gap: 2px; }
.stat-label { font-size: 12px; color: #909399; }
.stat-value { font-size: 22px; font-weight: 700; color: #303133; line-height: 1.2; font-variant-numeric: tabular-nums; }

/* 工具栏 */
.toolbar {
  padding: 14px 16px 0;
  margin-bottom: 12px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}
.toolbar-form :deep(.el-form-item) { margin-bottom: 14px; }

/* 表格 */
.vip-order-table {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}
.vip-order-table :deep(.el-table__inner-wrapper)::before { display: none; }
.vip-order-table :deep(.cell) { padding: 12px 12px; }

/* 订单号 */
.order-sn { display: flex; align-items: center; gap: 6px; }
.sn-text {
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
  font-size: 12px;
  color: #606266;
  background: #f4f6f9;
  padding: 4px 10px;
  border-radius: 8px;
  letter-spacing: 0.3px;
}
.sn-copy {
  cursor: pointer;
  color: #909399;
  transition: color 0.2s;
}
.sn-copy:hover { color: var(--el-color-primary); }

/* 用户 */
.user-cell { display: flex; align-items: center; gap: 10px; }
.user-avatar {
  width: 36px; height: 36px;
  border-radius: 50%;
  flex-shrink: 0;
  background: #f0f2f5;
  overflow: hidden;
}
.user-avatar :deep(img) { width: 100%; height: 100%; object-fit: cover; }
.avatar-empty {
  width: 36px; height: 36px;
  border-radius: 50%;
  background: #f4f6f9;
  border: 1px dashed #dcdfe6;
  color: #909399;
  font-size: 12px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  user-select: none;
}
.user-info { display: flex; flex-direction: column; gap: 2px; min-width: 0; }
.user-name { font-size: 13px; font-weight: 600; color: #303133; }
.user-mobile { font-size: 12px; color: #909399; }

/* 天数徽章 */
.days-badge {
  display: inline-flex;
  align-items: baseline;
  gap: 2px;
  padding: 4px 12px;
  border-radius: 14px;
  background: linear-gradient(135deg, #e3f2fd, #bbdefb);
  color: #1565c0;
}
.days-num { font-size: 16px; font-weight: 700; font-variant-numeric: tabular-nums; }
.days-unit { font-size: 11px; opacity: 0.7; }

/* 金额 */
.money-text {
  display: inline-block;
  font-size: 16px; font-weight: 700; color: #f56c6c;
  font-variant-numeric: tabular-nums;
}
.money-original {
  display: block;
  font-size: 11px; color: #c0c4cc;
  text-decoration: line-through;
  margin-top: 2px;
}

/* 支付方式 chip */
.pay-chip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  white-space: nowrap;
}
.pay-icon { font-size: 12px; }
.pay-wechat     { background: #e7f7ed; color: #07c160; }
.pay-alipay     { background: #e6f4ff; color: #1677ff; }
.pay-wallet     { background: #fff7e6; color: #fa8c16; }
.pay-score      { background: #fff0f6; color: #c41d7f; }
.pay-cryptocard { background: #f0e7ff; color: #722ed1; }
.pay-system     { background: #f5f5f5; color: #606266; }

/* 状态标签 */
.status-tag :deep(.el-tag__content),
.status-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  white-space: nowrap;
}
.status-icon { font-size: 12px; }

/* 时间 */
.time-text {
  font-size: 12px;
  color: #606266;
  font-variant-numeric: tabular-nums;
}
.text-muted { color: #c0c4cc; font-size: 12px; }

/* 空状态 */
.empty-state {
  display: flex; flex-direction: column; align-items: center; gap: 12px;
  padding: 40px 0;
}
.empty-icon { font-size: 56px; color: #dcdfe6; }
.empty-text { color: #909399; font-size: 14px; }

/* 暗黑模式 */
html.dark .stat-card,
html.dark .toolbar,
html.dark .vip-order-table {
  background: #1f1f1f;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.2);
}
html.dark .stat-value { color: #e5e7eb; }
html.dark .user-name { color: #e5e7eb; }
html.dark .sn-text { background: #2a2a2a; color: #cfcfcf; }
html.dark .days-badge { background: rgba(21, 101, 192, 0.15); color: #90caf9; }
</style>
