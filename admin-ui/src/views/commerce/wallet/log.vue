<template>
  <div class="app-container">
    <el-form :model="queryParams" :inline="true" ref="queryRef">
      <el-form-item label="用户ID" prop="userId">
        <el-input v-model="queryParams.userId" placeholder="用户ID" clearable style="width:120px" />
      </el-form-item>
      <el-form-item label="钱包类型" prop="walletType">
        <el-select v-model="queryParams.walletType" placeholder="全部" clearable style="width:120px">
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
      <el-table-column label="用户ID" prop="userId" width="90" />
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
        <template #default="{ row }">
          {{ logTypeLabel(row.type) }}
        </template>
      </el-table-column>
      <el-table-column label="备注" prop="memo" show-overflow-tooltip />
      <el-table-column label="时间" prop="createTime" width="160" />
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script setup>
import { listWalletLog } from '@/api/commerce/wallet'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const queryParams = reactive({ pageNum: 1, pageSize: 20, siteId: 1, userId: '', walletType: '' })
const queryRef = ref()

const WALLET_TYPE_MAP = {
  money: '余额',
  usable: '点数',
  score: '积分',
}

const LOG_TYPE_MAP = {
  episode_unlock: '解锁剧集',
  usable_recharge: '充值点数',
  admin_recharge: '平台赠送',
  vip_recharge: '购买VIP',
  reseller_buy: '购买分销商',
  withdraw: '提现申请',
  withdraw_reject: '提现驳回',
}

function walletTypeLabel(type) {
  return WALLET_TYPE_MAP[type] || type
}

function logTypeLabel(type) {
  return LOG_TYPE_MAP[type] || type
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

getList()
</script>

<style scoped>
.text-success { color: #67c23a; }
.text-danger { color: #f56c6c; }
</style>
