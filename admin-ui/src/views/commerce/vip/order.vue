<template>
  <div class="app-container">
    <el-form :model="queryParams" :inline="true" ref="queryRef">
      <el-form-item label="手机号" prop="mobile">
        <el-input v-model="queryParams.mobile" placeholder="用户手机号" clearable style="width:140px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="昵称" prop="nickname">
        <el-input v-model="queryParams.nickname" placeholder="用户昵称" clearable style="width:130px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable style="width:110px">
          <el-option label="待支付" :value="0" />
          <el-option label="已支付" :value="1" />
          <el-option label="已取消" :value="-1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="list" stripe border>
      <el-table-column label="订单号" prop="orderSn" width="200" show-overflow-tooltip />
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
      <el-table-column label="天数" prop="days" width="70" align="center" />
      <el-table-column label="实付" width="90" align="right">
        <template #default="{ row }">
          <span class="money-text">¥{{ row.payFee }}</span>
        </template>
      </el-table-column>
      <el-table-column label="支付方式" prop="payType" width="90" align="center" />
      <el-table-column label="状态" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : row.status === 0 ? 'warning' : 'info'" size="small">
            {{ row.status === 1 ? '已支付' : row.status === 0 ? '待支付' : '已取消' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="支付时间" prop="payTime" width="160" />
      <el-table-column label="创建时间" prop="createTime" width="160" />
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script setup>
import { listVipOrder } from '@/api/commerce/vip'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const queryParams = reactive({ pageNum: 1, pageSize: 10, siteId: 1, mobile: '', nickname: '', status: '' })
const queryRef = ref()

async function getList() {
  loading.value = true
  try {
    const res = await listVipOrder(queryParams)
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
.user-cell { display: flex; align-items: center; gap: 8px; }
.user-avatar { flex-shrink: 0; background: #f0f2f5; }
.user-info { display: flex; flex-direction: column; gap: 2px; min-width: 0; }
.user-name { font-size: 13px; font-weight: 600; color: #303133; }
.user-mobile { font-size: 12px; color: #909399; }
.money-text { font-size: 14px; font-weight: 700; color: #f56c6c; }
</style>
