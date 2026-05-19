<template>
  <div class="app-container wallet-log-page">
    <!-- 工具栏 -->
    <div class="toolbar">
      <el-form :model="queryParams" :inline="true" ref="queryRef" class="toolbar-form">
        <el-form-item label="手机号" prop="mobile">
          <el-input v-model="queryParams.mobile" placeholder="用户手机号" clearable style="width:160px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="queryParams.nickname" placeholder="用户昵称" clearable style="width:150px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="钱包类型" prop="walletType">
          <el-select v-model="queryParams.walletType" placeholder="全部" clearable style="width:130px">
            <el-option label="余额" value="money" />
            <el-option label="点数" value="usable" />
            <el-option label="积分" value="score" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleQuery">搜索</el-button>
          <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 表格 -->
    <el-table v-loading="loading" :data="list" stripe class="wallet-table" :header-cell-style="headerStyle">
      <el-table-column label="ID" prop="id" width="80" align="center">
        <template #default="{ row }">
          <span class="id-badge">#{{ row.id }}</span>
        </template>
      </el-table-column>

      <el-table-column label="用户" min-width="160">
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

      <el-table-column label="钱包类型" width="110" align="center">
        <template #default="{ row }">
          <span class="wallet-chip" :class="`wallet-${row.walletType}`">
            <el-icon class="wallet-icon"><component :is="walletIcon(row.walletType)" /></el-icon>
            {{ walletTypeLabel(row.walletType) }}
          </span>
        </template>
      </el-table-column>

      <el-table-column label="变动" width="120" align="center">
        <template #default="{ row }">
          <span class="delta" :class="row.wallet >= 0 ? 'delta-up' : 'delta-down'">
            <el-icon class="delta-arrow"><component :is="row.wallet >= 0 ? CaretTop : CaretBottom" /></el-icon>
            {{ row.wallet >= 0 ? '+' : '' }}{{ row.wallet }}
          </span>
        </template>
      </el-table-column>

      <el-table-column label="变动前 → 变动后" width="240" align="center">
        <template #default="{ row }">
          <div class="balance-flow">
            <span class="balance-before">{{ row.before }}</span>
            <el-icon class="flow-arrow"><Right /></el-icon>
            <span class="balance-after">{{ row.after }}</span>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="类型" width="130">
        <template #default="{ row }">
          <el-tag :type="logTypeTag(row.type)" effect="light" size="small" class="type-tag">
            {{ logTypeLabel(row.type) }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="备注" prop="memo" min-width="160" show-overflow-tooltip>
        <template #default="{ row }">
          <span v-if="row.memo" class="memo-text">{{ row.memo }}</span>
          <span v-else class="text-muted">—</span>
        </template>
      </el-table-column>

      <el-table-column label="时间" prop="createTime" width="160" align="center">
        <template #default="{ row }">
          <span class="time-text">{{ row.createTime }}</span>
        </template>
      </el-table-column>

      <template #empty>
        <div class="empty-state">
          <el-icon class="empty-icon"><Wallet /></el-icon>
          <div class="empty-text">暂无钱包流水</div>
        </div>
      </template>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script setup>
import { listWalletLog } from '@/api/commerce/wallet'
import {
  Search, Refresh, Wallet, Coin, Medal, CaretTop, CaretBottom, Right,
} from '@element-plus/icons-vue'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const queryParams = reactive({ pageNum: 1, pageSize: 20, siteId: 1, mobile: '', nickname: '', walletType: '' })
const queryRef = ref()

const headerStyle = { background: '#f7f8fa', color: '#303133', fontWeight: 600 }

const WALLET_TYPE_MAP = { money: '余额', usable: '点数', score: '积分' }
const WALLET_ICON_MAP = { money: Wallet, usable: Coin, score: Medal }
const LOG_TYPE_MAP = {
  episode_unlock: '解锁剧集',
  usable_recharge: '充值点数',
  admin_recharge: '平台赠送',
  vip_recharge: '购买VIP',
  reseller_buy: '购买分销商',
  withdraw: '提现申请',
  withdraw_reject: '提现驳回',
}
// 类型对应 el-tag 色
const LOG_TYPE_TAG = {
  episode_unlock: 'info',
  usable_recharge: 'success',
  admin_recharge: 'warning',
  vip_recharge: 'success',
  reseller_buy: 'success',
  withdraw: 'danger',
  withdraw_reject: 'danger',
}

function walletTypeLabel(type) { return WALLET_TYPE_MAP[type] || type }
function walletIcon(type) { return WALLET_ICON_MAP[type] || Wallet }
function logTypeLabel(type) { return LOG_TYPE_MAP[type] || type }
function logTypeTag(type) { return LOG_TYPE_TAG[type] || 'info' }

async function getList() {
  loading.value = true
  try {
    const res = await listWalletLog(queryParams)
    list.value = res.data.rows
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function handleQuery() { queryParams.pageNum = 1; getList() }
function resetQuery() { queryRef.value?.resetFields(); handleQuery() }

getList()
</script>

<style scoped>
.wallet-log-page { padding: 12px; }

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
.wallet-table {
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}
.wallet-table :deep(.el-table__inner-wrapper)::before { display: none; }
.wallet-table :deep(.cell) { padding: 6px 10px; line-height: 1.5; }
.wallet-table :deep(.el-table__row) td { padding: 7px 0; }
.wallet-table :deep(th.el-table__cell) { padding: 8px 0; }

/* ID 徽章 */
.id-badge {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 12px;
  background: linear-gradient(135deg, #eef2ff, #e0e7ff);
  color: #5048e5;
  font-size: 12px;
  font-weight: 600;
  font-variant-numeric: tabular-nums;
}

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
  color: #909399;
  font-size: 11px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  user-select: none;
}
.user-info { display: flex; flex-direction: column; gap: 0; line-height: 1.35; min-width: 0; }
.user-name { font-size: 13px; font-weight: 600; color: #303133; }
.user-mobile { font-size: 11px; color: #909399; }

/* 钱包类型 chip */
.wallet-chip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  white-space: nowrap;
}
.wallet-icon { font-size: 12px; }
.wallet-money  { background: #f0f9eb; color: #67c23a; }
.wallet-usable { background: #fff7e6; color: #d46b08; }
.wallet-score  { background: #fff0f6; color: #c41d7f; }

/* 变动金额（增/减） */
.delta {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
}
.delta-arrow { font-size: 12px; }
.delta-up   { background: #f0f9eb; color: #67c23a; }
.delta-down { background: #fef0f0; color: #f56c6c; }

/* 余额流向 */
.balance-flow {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-variant-numeric: tabular-nums;
}
.balance-before { color: #909399; font-size: 13px; }
.balance-after  { color: #303133; font-size: 14px; font-weight: 700; }
.flow-arrow { color: #c0c4cc; font-size: 12px; }

/* 类型标签 */
.type-tag { font-size: 11px; }

/* 备注 / 时间 */
.memo-text { font-size: 12px; color: #606266; }
.time-text {
  font-size: 11px;
  color: #606266;
  font-variant-numeric: tabular-nums;
  white-space: nowrap;
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
html.dark .toolbar,
html.dark .wallet-table {
  background: #1f1f1f;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.2);
}
html.dark .user-name,
html.dark .balance-after { color: #e5e7eb; }
html.dark .id-badge { background: rgba(80, 72, 229, 0.15); color: #a5b4fc; }
</style>
