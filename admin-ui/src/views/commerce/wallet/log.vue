<template>
  <div class="app-container">
    <el-form :model="queryParams" :inline="true" ref="queryRef">
      <el-form-item label="用户ID" prop="userId">
        <el-input v-model="queryParams.userId" placeholder="用户ID" clearable style="width:120px" />
      </el-form-item>
      <el-form-item label="钱包类型" prop="walletType">
        <el-select v-model="queryParams.walletType" placeholder="全部" clearable style="width:120px">
          <el-option label="余额" value="money" />
          <el-option label="积分" value="score" />
          <el-option label="点数" value="usable" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="list">
      <el-table-column label="ID" prop="id" width="80" />
      <el-table-column label="用户ID" prop="userId" width="90" />
      <el-table-column label="钱包类型" prop="walletType" width="90" align="center" />
      <el-table-column label="变动" prop="wallet" width="100" align="right">
        <template #default="{ row }">
          <span :class="row.wallet >= 0 ? 'text-success' : 'text-danger'">
            {{ row.wallet >= 0 ? '+' : '' }}{{ row.wallet }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="变动前" prop="before" width="100" align="right" />
      <el-table-column label="变动后" prop="after" width="100" align="right" />
      <el-table-column label="类型" prop="type" width="120" />
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
