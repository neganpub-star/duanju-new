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

    <el-table v-loading="loading" :data="list" stripe border>
      <el-table-column label="申请单号" prop="applySn" width="190" show-overflow-tooltip />
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
      <el-table-column label="申请金额" width="100" align="right">
        <template #default="{ row }">
          <span class="money-text">¥{{ row.money }}</span>
        </template>
      </el-table-column>
      <el-table-column label="实际到账" width="100" align="right">
        <template #default="{ row }">
          <span>¥{{ row.actualMoney }}</span>
        </template>
      </el-table-column>
      <el-table-column label="提现方式" width="90" align="center">
        <template #default="{ row }">
          <el-tag type="info" effect="plain" size="small">{{ typeLabel(row.applyType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="收款信息" min-width="160" show-overflow-tooltip>
        <template #default="{ row }">
          <span class="apply-info">{{ formatApplyInfo(row.applyInfo) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" size="small">
            {{ statusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="申请时间" prop="createTime" width="170" />
      <el-table-column label="操作" width="165" fixed="right">
        <template #default="{ row }">
          <div class="action-cell">
            <template v-if="row.status === 0">
              <el-button link type="primary" @click="handleApprove(row, 1)">通过</el-button>
              <el-button link type="danger" @click="handleApprove(row, -1)">拒绝</el-button>
            </template>
            <template v-else-if="row.status === 1">
              <el-button link type="success" @click="handleApprove(row, 2)">完成</el-button>
            </template>
            <el-button v-if="row.applyInfo" link type="info" @click="showDetail(row)">详情</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 审核弹窗 -->
    <el-dialog title="审核提现" v-model="approveVisible" width="420px" append-to-body>
      <el-descriptions :column="1" border size="small" class="mb16">
        <el-descriptions-item label="用户">{{ currentRow?.nickname }} · {{ currentRow?.mobile }}</el-descriptions-item>
        <el-descriptions-item label="申请金额">¥{{ currentRow?.money }}</el-descriptions-item>
        <el-descriptions-item label="操作">
          <el-tag :type="approveForm.status === -1 ? 'danger' : approveForm.status === 1 ? 'primary' : 'success'">
            {{ { '-1': '拒绝', 1: '通过', 2: '完成' }[approveForm.status] }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
      <el-form label-width="60px">
        <el-form-item label="备注">
          <el-input v-model="approveForm.remark" type="textarea" :rows="3" placeholder="备注信息（选填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approveVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitApprove">确定</el-button>
      </template>
    </el-dialog>

    <!-- 收款信息详情 -->
    <el-dialog title="收款账户信息" v-model="detailVisible" width="360px" append-to-body>
      <el-descriptions :column="1" border size="small">
        <el-descriptions-item v-for="(v, k) in parsedApplyInfo" :key="k" :label="infoLabel(k)">{{ v }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { listWithdraw, handleWithdraw } from '@/api/commerce/wallet'
import { ElMessage } from 'element-plus'

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

const TYPE_MAP = { wechat: '微信', alipay: '支付宝', bank: '银行卡' }
const INFO_LABEL_MAP = { real_name: '真实姓名', account: '账号/卡号', bank_name: '开户银行' }
const STATUS_MAP = { '-1': '已拒绝', 0: '待审核', 1: '处理中', 2: '已完成' }
const STATUS_TYPE_MAP = { '-1': 'danger', 0: 'warning', 1: 'primary', 2: 'success' }

function typeLabel(t) { return TYPE_MAP[t] || t }
function statusLabel(s) { return STATUS_MAP[String(s)] || s }
function statusType(s) { return STATUS_TYPE_MAP[String(s)] || 'info' }
function infoLabel(k) { return INFO_LABEL_MAP[k] || k }

function formatApplyInfo(json) {
  try {
    const obj = JSON.parse(json || '{}')
    return [obj.real_name, obj.account, obj.bank_name].filter(Boolean).join(' · ')
  } catch { return json || '—' }
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
  } finally {
    submitting.value = false
  }
}

getList()
</script>

<style scoped>
.user-cell { display: flex; align-items: center; gap: 8px; }
.user-avatar { flex-shrink: 0; background: #f0f2f5; }
.user-info { display: flex; flex-direction: column; gap: 2px; min-width: 0; }
.user-name { font-size: 13px; font-weight: 600; color: #303133; }
.user-mobile { font-size: 12px; color: #909399; }
.money-text { font-size: 14px; font-weight: 700; color: #f56c6c; }
.apply-info { font-size: 12px; color: #606266; }
.mb16 { margin-bottom: 16px; }
.action-cell { display: flex; align-items: center; flex-wrap: nowrap; gap: 2px; }
</style>
