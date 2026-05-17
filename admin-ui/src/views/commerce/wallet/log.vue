<template>
  <div class="app-container">
    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stats-row">
      <el-col :span="6" v-for="item in statCards" :key="item.label">
        <div class="stat-card" :style="{ borderTop: '3px solid ' + item.color }">
          <div class="stat-label">{{ item.label }}</div>
          <div class="stat-row">
            <div class="stat-block">
              <div class="stat-sub">入金</div>
              <div class="stat-num income">¥{{ item.income }}</div>
            </div>
            <div class="stat-divider" />
            <div class="stat-block">
              <div class="stat-sub">提现</div>
              <div class="stat-num withdraw">¥{{ item.withdraw }}</div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-form :model="queryParams" :inline="true" ref="queryRef">
      <el-form-item label="手机号" prop="mobile">
        <el-input v-model="queryParams.mobile" placeholder="用户手机号" clearable style="width:140px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="昵称" prop="nickname">
        <el-input v-model="queryParams.nickname" placeholder="用户昵称" clearable style="width:130px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="钱包类型" prop="walletType">
        <el-select v-model="queryParams.walletType" placeholder="全部" clearable style="width:110px">
          <el-option label="余额" value="money" />
          <el-option label="点数" value="usable" />
          <el-option label="积分" value="score" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="list" stripe border>
      <el-table-column label="ID" prop="id" width="80" />
      <el-table-column label="用户" min-width="160">
        <template #default="{ row }">
          <div class="user-cell">
            <el-avatar :size="32" :src="row.avatar" class="user-avatar" />
            <div class="user-info">
              <span class="user-name">{{ row.nickname || '—' }}</span>
              <span class="user-mobile">{{ row.mobile || 'ID:' + row.userId }}</span>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="钱包类型" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="row.walletType === 'money' ? 'success' : 'warning'" effect="plain" size="small">
            {{ walletTypeLabel(row.walletType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="变动" width="110" align="center">
        <template #default="{ row }">
          <span :class="row.wallet >= 0 ? 'text-success' : 'text-danger'" style="font-size:15px;font-weight:700">
            {{ row.wallet >= 0 ? '+' : '' }}{{ row.wallet }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="变动前" prop="before" width="90" align="center">
        <template #default="{ row }">
          <span style="color:#909399">{{ row.before }}</span>
        </template>
      </el-table-column>
      <el-table-column label="变动后" prop="after" width="90" align="center">
        <template #default="{ row }">
          <span style="font-weight:600;color:#303133">{{ row.after }}</span>
        </template>
      </el-table-column>
      <el-table-column label="类型" width="130">
        <template #default="{ row }">{{ logTypeLabel(row.type) }}</template>
      </el-table-column>
      <el-table-column label="备注" prop="memo" show-overflow-tooltip />
      <el-table-column label="时间" prop="createTime" width="160" />
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script setup>
import { listWalletLog, getWalletStats } from '@/api/commerce/wallet'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const queryParams = reactive({ pageNum: 1, pageSize: 20, siteId: 1, mobile: '', nickname: '', walletType: '' })
const queryRef = ref()

const WALLET_TYPE_MAP = { money: '余额', usable: '点数', score: '积分' }
const LOG_TYPE_MAP = {
  episode_unlock: '解锁剧集',
  usable_recharge: '充值点数',
  admin_recharge: '平台赠送',
  vip_recharge: '购买VIP',
  reseller_buy: '购买分销商',
  withdraw: '提现申请',
  withdraw_reject: '提现驳回',
}

function walletTypeLabel(type) { return WALLET_TYPE_MAP[type] || type }
function logTypeLabel(type) { return LOG_TYPE_MAP[type] || type }

// 统计数据
const statsColors = ['#409eff', '#67c23a', '#e6a23c', '#f56c6c']
const statsRaw = reactive({ todayIncome: 0, weekIncome: 0, monthIncome: 0, yearIncome: 0, todayWithdraw: 0, weekWithdraw: 0, monthWithdraw: 0, yearWithdraw: 0 })

const statCards = computed(() => [
  { label: '今日', income: statsRaw.todayIncome, withdraw: statsRaw.todayWithdraw, color: statsColors[0] },
  { label: '本周', income: statsRaw.weekIncome, withdraw: statsRaw.weekWithdraw, color: statsColors[1] },
  { label: '本月', income: statsRaw.monthIncome, withdraw: statsRaw.monthWithdraw, color: statsColors[2] },
  { label: '本年', income: statsRaw.yearIncome, withdraw: statsRaw.yearWithdraw, color: statsColors[3] },
])

async function loadStats() {
  const res = await getWalletStats({ siteId: 1 })
  Object.assign(statsRaw, res.data)
}

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

loadStats()
getList()
</script>

<style scoped>
.stats-row { margin-bottom: 20px; }
.stat-card {
  background: #fff;
  border-radius: 6px;
  padding: 16px 20px;
  box-shadow: 0 1px 6px rgba(0,0,0,.06);
}
.stat-label { font-size: 13px; color: #909399; margin-bottom: 12px; font-weight: 600; }
.stat-row { display: flex; align-items: center; gap: 12px; }
.stat-block { flex: 1; text-align: center; }
.stat-sub { font-size: 11px; color: #c0c4cc; margin-bottom: 4px; }
.stat-num { font-size: 18px; font-weight: 700; }
.stat-num.income { color: #67c23a; }
.stat-num.withdraw { color: #f56c6c; }
.stat-divider { width: 1px; height: 36px; background: #ebeef5; }
.text-success { color: #67c23a; }
.text-danger { color: #f56c6c; }
.user-cell { display: flex; align-items: center; gap: 8px; }
.user-avatar { flex-shrink: 0; background: #f0f2f5; }
.user-info { display: flex; flex-direction: column; gap: 2px; min-width: 0; }
.user-name { font-size: 13px; font-weight: 600; color: #303133; }
.user-mobile { font-size: 12px; color: #909399; }
</style>
