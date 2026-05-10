<template>
  <div class="app-container">
    <el-form :model="queryParams" :inline="true" ref="queryRef">
      <el-form-item label="手机号" prop="mobile">
        <el-input v-model="queryParams.mobile" placeholder="手机号" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="昵称" prop="nickname">
        <el-input v-model="queryParams.nickname" placeholder="昵称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="list">
      <el-table-column label="ID" prop="id" width="80" />
      <el-table-column label="昵称" prop="nickname" />
      <el-table-column label="手机号" prop="mobile" width="130" />
      <el-table-column label="余额" prop="money" width="90" align="right">
        <template #default="{ row }">¥{{ row.money }}</template>
      </el-table-column>
      <el-table-column label="点数" prop="usable" width="80" align="center" />
      <el-table-column label="VIP到期" prop="vipExpireTime" width="160" />
      <el-table-column label="注册时间" prop="createTime" width="160" />
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleRecharge(row)">充值</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog title="余额充值" v-model="rechargeVisible" width="360px" append-to-body>
      <el-form label-width="80px">
        <el-form-item label="用户">{{ currentUser?.nickname }}（{{ currentUser?.mobile }}）</el-form-item>
        <el-form-item label="类型">
          <el-select v-model="rechargeForm.walletType" style="width:100%">
            <el-option label="余额(money)" value="money" />
            <el-option label="积分(score)" value="score" />
            <el-option label="点数(usable)" value="usable" />
          </el-select>
        </el-form-item>
        <el-form-item label="金额/数量">
          <el-input-number v-model="rechargeForm.amount" :min="0.01" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="rechargeForm.memo" placeholder="备注（选填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rechargeVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRecharge">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { listDramaUser, rechargeUser } from '@/api/duanju/user'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const queryParams = reactive({ pageNum: 1, pageSize: 10, siteId: 1, mobile: '', nickname: '' })
const queryRef = ref()
const rechargeVisible = ref(false)
const currentUser = ref(null)
const rechargeForm = reactive({ walletType: 'money', amount: 10, memo: '' })

async function getList() {
  loading.value = true
  try {
    const res = await listDramaUser(queryParams)
    list.value = res.data.rows
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function handleQuery() { queryParams.pageNum = 1; getList() }
function resetQuery() { queryRef.value?.resetFields(); handleQuery() }

function handleRecharge(row) {
  currentUser.value = row
  rechargeForm.walletType = 'money'
  rechargeForm.amount = 10
  rechargeForm.memo = ''
  rechargeVisible.value = true
}

async function submitRecharge() {
  await rechargeUser(currentUser.value.id, rechargeForm)
  ElMessage.success('充值成功')
  rechargeVisible.value = false
  getList()
}

getList()
</script>
