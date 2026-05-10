<template>
  <div class="app-container">
    <el-form :model="queryParams" :inline="true" ref="queryRef">
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable style="width:120px">
          <el-option label="待审核" :value="0" />
          <el-option label="处理中" :value="1" />
          <el-option label="已完成" :value="2" />
          <el-option label="已拒绝" :value="-1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="list">
      <el-table-column label="申请单号" prop="applySn" width="200" show-overflow-tooltip />
      <el-table-column label="用户ID" prop="userId" width="90" />
      <el-table-column label="申请金额" prop="money" width="100" align="right">
        <template #default="{ row }">¥{{ row.money }}</template>
      </el-table-column>
      <el-table-column label="实际到账" prop="actualMoney" width="100" align="right">
        <template #default="{ row }">¥{{ row.actualMoney }}</template>
      </el-table-column>
      <el-table-column label="提现类型" prop="applyType" width="90" />
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 2 ? 'success' : row.status === -1 ? 'danger' : row.status === 1 ? 'primary' : 'warning'">
            {{ { '-1': '已拒绝', 0: '待审核', 1: '处理中', 2: '已完成' }[row.status] }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="申请时间" prop="createTime" width="160" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <template v-if="row.status === 0">
            <el-button link type="primary" @click="handleApprove(row, 1)">通过</el-button>
            <el-button link type="danger" @click="handleApprove(row, -1)">拒绝</el-button>
          </template>
          <template v-else-if="row.status === 1">
            <el-button link type="success" @click="handleApprove(row, 2)">完成</el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog title="审核提现" v-model="approveVisible" width="400px" append-to-body>
      <el-form label-width="80px">
        <el-form-item label="操作">
          <el-tag :type="approveForm.status === -1 ? 'danger' : approveForm.status === 1 ? 'primary' : 'success'">
            {{ { '-1': '拒绝', 1: '通过', 2: '完成' }[approveForm.status] }}
          </el-tag>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="approveForm.remark" type="textarea" :rows="3" placeholder="备注信息（选填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approveVisible = false">取消</el-button>
        <el-button type="primary" @click="submitApprove">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { listWithdraw, handleWithdraw } from '@/api/commerce/wallet'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const queryParams = reactive({ pageNum: 1, pageSize: 20, siteId: 1, status: '' })
const queryRef = ref()
const approveVisible = ref(false)
const currentId = ref(null)
const approveForm = reactive({ status: 1, remark: '' })

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

function handleApprove(row, status) {
  currentId.value = row.id
  approveForm.status = status
  approveForm.remark = ''
  approveVisible.value = true
}

async function submitApprove() {
  await handleWithdraw(currentId.value, approveForm)
  ElMessage.success('操作成功')
  approveVisible.value = false
  getList()
}

getList()
</script>
