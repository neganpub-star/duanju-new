<template>
  <div class="app-container withdraw-page">
    <!-- 状态统计 -->
    <div class="stat-cards">
      <div
        v-for="(item, idx) in statCards"
        :key="item.label"
        class="stat-card"
        :class="[`stat-card--${idx}`, { 'stat-card--alert': item.alert && Number(item.value) > 0 }]"
        @click="quickFilter(item.statusValue)"
      >
        <div class="stat-icon"><el-icon><component :is="item.icon" /></el-icon></div>
        <div class="stat-body">
          <div class="stat-label">{{ item.label }}</div>
          <div class="stat-value">{{ item.value }}<span v-if="item.unit" class="stat-unit">{{ item.unit }}</span></div>
        </div>
      </div>
    </div>

    <!-- 工具栏 -->
    <div class="toolbar">
      <el-form :model="queryParams" :inline="true" ref="queryRef" class="toolbar-form">
        <el-form-item label="手机号" prop="mobile">
          <el-input v-model="queryParams.mobile" placeholder="用户手机号" clearable style="width:160px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="queryParams.nickname" placeholder="用户昵称" clearable style="width:150px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="全部" clearable style="width:130px">
            <el-option label="待审核" :value="0" />
            <el-option label="处理中" :value="1" />
            <el-option label="已完成" :value="2" />
            <el-option label="已拒绝" :value="-1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleQuery">搜索</el-button>
          <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 表格 -->
    <el-table v-loading="loading" :data="list" stripe class="withdraw-table" :header-cell-style="headerStyle">
      <el-table-column label="申请单号" min-width="200">
        <template #default="{ row }">
          <div class="order-sn">
            <span class="sn-text">{{ row.applySn }}</span>
            <el-tooltip content="复制单号" placement="top">
              <el-icon class="sn-copy" @click="copyText(row.applySn)"><CopyDocument /></el-icon>
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

      <el-table-column label="申请金额 / 实际到账" width="180" align="center">
        <template #default="{ row }">
          <div class="amount-flow">
            <span class="amount-apply">¥{{ row.money }}</span>
            <el-icon class="flow-arrow"><Right /></el-icon>
            <span class="amount-actual">¥{{ row.actualMoney }}</span>
          </div>
          <div v-if="hasFee(row)" class="amount-fee">手续费 ¥{{ feeOf(row) }}</div>
        </template>
      </el-table-column>

      <el-table-column label="提现方式" width="120" align="center">
        <template #default="{ row }">
          <span v-if="row.applyType" class="pay-chip" :class="`pay-${row.applyType}`">
            <el-icon class="pay-icon"><component :is="typeIcon(row.applyType)" /></el-icon>
            {{ typeLabel(row.applyType) }}
          </span>
          <span v-else class="text-muted">—</span>
        </template>
      </el-table-column>

      <el-table-column label="收款信息" min-width="200">
        <template #default="{ row }">
          <div v-if="parseInfo(row.applyInfo)" class="apply-info-cell">
            <div class="info-name">{{ maskName(parseInfo(row.applyInfo).real_name) }}</div>
            <div class="info-account">{{ maskAccount(parseInfo(row.applyInfo).account) }}</div>
            <div v-if="parseInfo(row.applyInfo).bank_name" class="info-bank">{{ parseInfo(row.applyInfo).bank_name }}</div>
          </div>
          <span v-else class="text-muted">—</span>
        </template>
      </el-table-column>

      <el-table-column label="状态" width="110" align="center">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" effect="light" size="small" class="status-tag">
            <el-icon class="status-icon"><component :is="statusIcon(row.status)" /></el-icon>
            <span>{{ statusLabel(row.status) }}</span>
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="申请时间" prop="createTime" width="170" align="center">
        <template #default="{ row }">
          <span class="time-text">{{ row.createTime }}</span>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="180" fixed="right" align="center">
        <template #default="{ row }">
          <div class="action-cell">
            <template v-if="row.status === 0">
              <el-button link type="primary" size="small" :icon="CircleCheck" @click="handleApprove(row, 1)">通过</el-button>
              <el-divider direction="vertical" />
              <el-button link type="danger" size="small" :icon="CircleClose" @click="handleApprove(row, -1)">拒绝</el-button>
            </template>
            <template v-else-if="row.status === 1">
              <el-button link type="success" size="small" :icon="SuccessFilled" @click="handleApprove(row, 2)">完成</el-button>
              <el-divider direction="vertical" />
            </template>
            <el-button v-if="row.applyInfo" link type="info" size="small" :icon="View" @click="showDetail(row)">详情</el-button>
          </div>
        </template>
      </el-table-column>

      <template #empty>
        <div class="empty-state">
          <el-icon class="empty-icon"><Wallet /></el-icon>
          <div class="empty-text">暂无提现申请</div>
        </div>
      </template>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 审核弹窗 -->
    <el-dialog
      v-model="approveVisible"
      width="520px"
      append-to-body
      class="app-dialog"
      :show-close="false"
    >
      <template #header>
        <div class="app-dialog__header">
          <div class="app-dialog__title">
            <el-icon class="app-dialog__icon"><Wallet /></el-icon>
            <span>审核提现</span>
            <el-tag v-if="currentRow?.id" effect="light" type="primary" round size="small" class="app-dialog__tag">#{{ currentRow.id }}</el-tag>
          </div>
          <el-icon class="app-dialog__close" @click="approveVisible = false"><Close /></el-icon>
        </div>
      </template>

      <div class="app-section">
        <div class="app-section__title"><span class="app-section__bar"></span>提现信息</div>
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="用户">{{ currentRow?.nickname }} · {{ currentRow?.mobile }}</el-descriptions-item>
          <el-descriptions-item label="申请金额">
            <span class="amount-large">¥{{ currentRow?.money }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="实际到账">
            <span class="amount-actual">¥{{ currentRow?.actualMoney }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="操作">
            <el-tag :type="approveActionTag.type" effect="light" size="small">
              <el-icon><component :is="approveActionTag.icon" /></el-icon>
              <span>{{ approveActionTag.label }}</span>
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <div class="app-section" :class="approveForm.status === -1 ? 'app-section--warning' : 'app-section--highlight'">
        <div class="app-section__title">
          <span class="app-section__bar"></span>
          {{ approveForm.status === -1 ? '驳回备注' : '审核备注' }}
        </div>
        <el-form label-width="60px">
          <el-form-item label="备注">
            <el-input v-model="approveForm.remark" type="textarea" :rows="3" placeholder="备注信息（选填）" />
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <div class="app-dialog__footer">
          <el-button @click="approveVisible = false">取消</el-button>
          <el-button
            :type="approveForm.status === -1 ? 'danger' : 'primary'"
            :icon="approveForm.status === -1 ? CircleClose : Check"
            :loading="submitting"
            @click="submitApprove"
          >{{ approveForm.status === -1 ? '确认驳回' : '确认通过' }}</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 收款信息详情 -->
    <el-dialog
      v-model="detailVisible"
      width="440px"
      append-to-body
      class="app-dialog"
      :show-close="false"
    >
      <template #header>
        <div class="app-dialog__header">
          <div class="app-dialog__title">
            <el-icon class="app-dialog__icon"><CreditCard /></el-icon>
            <span>收款账户信息</span>
          </div>
          <el-icon class="app-dialog__close" @click="detailVisible = false"><Close /></el-icon>
        </div>
      </template>

      <div class="app-section">
        <div class="app-section__title"><span class="app-section__bar"></span>账户详情</div>
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item v-for="(v, k) in parsedApplyInfo" :key="k" :label="infoLabel(k)">{{ v }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <template #footer>
        <div class="app-dialog__footer">
          <el-button type="primary" @click="detailVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { listWithdraw, handleWithdraw, getWithdrawStats } from '@/api/commerce/wallet'
import { ElMessage } from 'element-plus'
import {
  Search, Refresh, Wallet, Right, CopyDocument, View,
  Clock, CircleCheck, CircleClose, SuccessFilled, WarningFilled,
  ChatDotRound, CreditCard, Money, Close, Check,
} from '@element-plus/icons-vue'

const loading = ref(false)
const submitting = ref(false)
const list = ref([])
const total = ref(0)
const queryParams = reactive({ pageNum: 1, pageSize: 20, siteId: 1, status: '', mobile: '', nickname: '' })
const queryRef = ref()
const approveVisible = ref(false)
const detailVisible = ref(false)
const currentRow = ref(null)
const currentId = ref(null)
const approveForm = reactive({ status: 1, remark: '' })
const parsedApplyInfo = ref({})

const headerStyle = { background: '#f7f8fa', color: '#303133', fontWeight: 600 }

const TYPE_MAP  = { wechat: '微信', alipay: '支付宝', bank: '银行卡' }
const TYPE_ICON = { wechat: ChatDotRound, alipay: CreditCard, bank: CreditCard }
const INFO_LABEL_MAP = { real_name: '真实姓名', account: '账号/卡号', bank_name: '开户银行' }
const STATUS_MAP = { '-1': '已拒绝', 0: '待审核', 1: '处理中', 2: '已完成' }
const STATUS_TYPE_MAP = { '-1': 'danger', 0: 'warning', 1: 'primary', 2: 'success' }
const STATUS_ICON_MAP = { '-1': CircleClose, 0: Clock, 1: WarningFilled, 2: CircleCheck }

function typeLabel(t) { return TYPE_MAP[t] || t }
function typeIcon(t)  { return TYPE_ICON[t] || CreditCard }
function statusLabel(s) { return STATUS_MAP[String(s)] || s }
function statusType(s)  { return STATUS_TYPE_MAP[String(s)] || 'info' }
function statusIcon(s)  { return STATUS_ICON_MAP[String(s)] || Clock }
function infoLabel(k)   { return INFO_LABEL_MAP[k] || k }

// 解析收款 JSON
function parseInfo(json) {
  try { return JSON.parse(json || '{}') } catch { return null }
}
// 姓名脱敏：保留首末，中间用 *
function maskName(name) {
  if (!name || name.length < 2) return name || ''
  if (name.length === 2) return name[0] + '*'
  return name[0] + '*'.repeat(name.length - 2) + name[name.length - 1]
}
// 账号脱敏：保留前 4 后 4
function maskAccount(acc) {
  if (!acc) return ''
  const s = String(acc)
  if (s.length <= 8) return s
  return s.slice(0, 4) + ' **** ' + s.slice(-4)
}
function hasFee(row) {
  const m = Number(row.money) || 0
  const a = Number(row.actualMoney) || 0
  return m > a
}
function feeOf(row) {
  return ((Number(row.money) || 0) - (Number(row.actualMoney) || 0)).toFixed(2)
}

// 状态统计
const stats = ref({ pendingCount: 0, processingCount: 0, doneCount: 0, rejectedCount: 0, totalDoneAmount: 0 })
const statCards = computed(() => [
  { label: '待审核', icon: Clock,         value: stats.value.pendingCount   ?? 0, statusValue: 0,  alert: true },
  { label: '处理中', icon: WarningFilled, value: stats.value.processingCount ?? 0, statusValue: 1 },
  { label: '已完成', icon: CircleCheck,   value: stats.value.doneCount       ?? 0, statusValue: 2 },
  { label: '累计已打款', icon: Money,      value: '¥' + Number(stats.value.totalDoneAmount || 0).toFixed(2) },
])
const approveActionTag = computed(() => {
  switch (approveForm.status) {
    case -1: return { type: 'danger',  icon: CircleClose,   label: '拒绝' }
    case 1:  return { type: 'primary', icon: CircleCheck,   label: '通过' }
    case 2:  return { type: 'success', icon: SuccessFilled, label: '完成' }
    default: return { type: 'info',    icon: Clock,         label: '未知' }
  }
})

async function loadStats() {
  try {
    const res = await getWithdrawStats({ siteId: 1 })
    stats.value = res.data || stats.value
  } catch {}
}

async function getList() {
  loading.value = true
  try {
    const res = await listWithdraw(queryParams)
    list.value = res.data.rows
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function handleQuery() { queryParams.pageNum = 1; getList() }
function resetQuery() { queryRef.value?.resetFields(); handleQuery() }

// 点击统计卡片快速筛选
function quickFilter(status) {
  if (status === undefined || status === null) return
  queryParams.status = status
  handleQuery()
}

function handleApprove(row, status) {
  currentId.value = row.id
  currentRow.value = row
  approveForm.status = status
  approveForm.remark = ''
  approveVisible.value = true
}

function showDetail(row) {
  try { parsedApplyInfo.value = JSON.parse(row.applyInfo || '{}') } catch { parsedApplyInfo.value = {} }
  detailVisible.value = true
}

async function submitApprove() {
  submitting.value = true
  try {
    await handleWithdraw(currentId.value, approveForm)
    ElMessage.success('操作成功')
    approveVisible.value = false
    getList()
    loadStats()
  } finally {
    submitting.value = false
  }
}

async function copyText(text) {
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success('已复制单号')
  } catch {
    ElMessage.warning('复制失败，请手动选择')
  }
}

loadStats()
getList()
</script>

<style scoped>
.withdraw-page { padding: 12px; }

/* 统计卡片 */
.stat-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 10px;
}
.stat-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  border-radius: 10px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: transform 0.2s, box-shadow 0.2s;
  position: relative;
  overflow: hidden;
  cursor: pointer;
}
.stat-card:hover { transform: translateY(-1px); box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08); }
.stat-card::before {
  content: ''; position: absolute; inset: 0;
  opacity: 0.05; pointer-events: none;
}
.stat-card--0::before { background: linear-gradient(135deg, #fa709a, #fee140); }
.stat-card--1::before { background: linear-gradient(135deg, #4facfe, #00f2fe); }
.stat-card--2::before { background: linear-gradient(135deg, #43e97b, #38f9d7); }
.stat-card--3::before { background: linear-gradient(135deg, #f5576c, #fa709a); }

/* 待审核 > 0 时加红色警示边 */
.stat-card--alert {
  box-shadow: 0 0 0 2px #f56c6c, 0 2px 8px rgba(245, 108, 108, 0.2);
}

.stat-icon {
  width: 34px; height: 34px; border-radius: 8px;
  display: flex; align-items: center; justify-content: center;
  font-size: 18px; color: #fff; flex-shrink: 0;
}
.stat-card--0 .stat-icon { background: linear-gradient(135deg, #fa709a, #fee140); }
.stat-card--1 .stat-icon { background: linear-gradient(135deg, #4facfe, #00f2fe); }
.stat-card--2 .stat-icon { background: linear-gradient(135deg, #43e97b, #38f9d7); }
.stat-card--3 .stat-icon { background: linear-gradient(135deg, #f5576c, #fa709a); }

.stat-body { display: flex; flex-direction: column; gap: 0; line-height: 1.2; }
.stat-label { font-size: 11px; color: #909399; }
.stat-value { font-size: 20px; font-weight: 700; color: #303133; line-height: 1.2; font-variant-numeric: tabular-nums; }
.stat-unit { font-size: 11px; color: #909399; font-weight: 500; margin-left: 2px; }

/* 工具栏 */
.toolbar {
  padding: 10px 14px 0;
  margin-bottom: 10px;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}
.toolbar-form :deep(.el-form-item) { margin-bottom: 10px; }

/* 表格 */
.withdraw-table {
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}
.withdraw-table :deep(.el-table__inner-wrapper)::before { display: none; }
.withdraw-table :deep(.cell) { padding: 6px 10px; line-height: 1.5; }
.withdraw-table :deep(.el-table__row) td { padding: 7px 0; }
.withdraw-table :deep(th.el-table__cell) { padding: 8px 0; }

/* 单号 */
.order-sn { display: flex; align-items: center; gap: 4px; }
.sn-text {
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
  font-size: 11px;
  color: #606266;
  background: #f4f6f9;
  padding: 2px 8px;
  border-radius: 6px;
}
.sn-copy { cursor: pointer; color: #909399; transition: color 0.2s; }
.sn-copy:hover { color: var(--el-color-primary); }

/* 用户 */
.user-cell { display: flex; align-items: center; gap: 8px; }
.user-avatar {
  width: 32px; height: 32px;
  border-radius: 50%;
  flex-shrink: 0;
  background: #f0f2f5;
  overflow: hidden;
}
.user-avatar :deep(img) { width: 100%; height: 100%; object-fit: cover; }
.avatar-empty {
  width: 32px; height: 32px;
  border-radius: 50%;
  background: #f4f6f9;
  border: 1px dashed #dcdfe6;
  color: #909399; font-size: 11px;
  display: inline-flex; align-items: center; justify-content: center;
  flex-shrink: 0; user-select: none;
}
.user-info { display: flex; flex-direction: column; gap: 0; line-height: 1.35; min-width: 0; }
.user-name { font-size: 13px; font-weight: 600; color: #303133; }
.user-mobile { font-size: 11px; color: #909399; }

/* 金额对比 */
.amount-flow {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-variant-numeric: tabular-nums;
}
.amount-apply { color: #909399; font-size: 13px; text-decoration: line-through; }
.amount-actual { color: #f56c6c; font-size: 16px; font-weight: 700; }
.flow-arrow { color: #c0c4cc; font-size: 12px; }
.amount-fee {
  font-size: 11px;
  color: #d46b08;
  margin-top: 2px;
}

/* 提现方式 chip */
.pay-chip {
  display: inline-flex; align-items: center; gap: 3px;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 11px;
  font-weight: 500;
  white-space: nowrap;
}
.pay-icon { font-size: 11px; }
.pay-wechat { background: #e7f7ed; color: #07c160; }
.pay-alipay { background: #e6f4ff; color: #1677ff; }
.pay-bank   { background: #f0e7ff; color: #722ed1; }

/* 收款信息 */
.apply-info-cell { display: flex; flex-direction: column; gap: 0; line-height: 1.35; }
.info-name { font-size: 12px; font-weight: 600; color: #303133; }
.info-account {
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
  font-size: 12px; color: #606266;
}
.info-bank { font-size: 11px; color: #909399; }

/* 状态 */
.status-tag :deep(.el-tag__content),
.status-tag {
  display: inline-flex; align-items: center; gap: 4px;
  white-space: nowrap;
}
.status-icon { font-size: 12px; }

/* 时间 */
.time-text {
  font-size: 11px; color: #606266;
  font-variant-numeric: tabular-nums;
  white-space: nowrap;
}
.text-muted { color: #c0c4cc; font-size: 12px; }

/* 操作 */
.action-cell { display: inline-flex; align-items: center; gap: 4px; flex-wrap: nowrap; }

/* 空状态 */
.empty-state {
  display: flex; flex-direction: column; align-items: center; gap: 12px;
  padding: 40px 0;
}
.empty-icon { font-size: 56px; color: #dcdfe6; }
.empty-text { color: #909399; font-size: 14px; }

/* 提现金额展示 */
.amount-large { font-size: 16px; font-weight: 700; color: #f56c6c; font-variant-numeric: tabular-nums; }
.amount-actual { font-size: 16px; font-weight: 700; color: #67c23a; font-variant-numeric: tabular-nums; }

/* 暗黑模式 */
html.dark .stat-card,
html.dark .toolbar,
html.dark .withdraw-table {
  background: #1f1f1f;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.2);
}
html.dark .stat-value,
html.dark .user-name,
html.dark .info-name { color: #e5e7eb; }
html.dark .sn-text { background: #2a2a2a; color: #cfcfcf; }
</style>
