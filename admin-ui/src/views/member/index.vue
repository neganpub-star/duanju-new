<template>
  <div class="app-container">
    <el-form :model="queryParams" :inline="true" ref="queryRef">
      <el-form-item label="手机号" prop="mobile">
        <el-input v-model="queryParams.mobile" placeholder="手机号" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="昵称" prop="nickname">
        <el-input v-model="queryParams.nickname" placeholder="昵称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="VIP" prop="isVip">
        <el-select v-model="queryParams.isVip" placeholder="全部" clearable style="width:100px">
          <el-option label="有效VIP" value="1" />
          <el-option label="非VIP" value="0" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="list">
      <el-table-column label="ID" prop="id" width="80" />
      <el-table-column label="昵称" prop="nickname" min-width="100" />
      <el-table-column label="手机号" prop="mobile" width="130" />
      <el-table-column label="解锁积分" prop="usable" width="100" align="center">
        <template #default="{ row }">
          <el-tag type="warning" effect="plain">{{ row.usable }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="VIP状态" width="140" align="center">
        <template #default="{ row }">
          <template v-if="row.vipExpireTime">
            <el-tag v-if="new Date(row.vipExpireTime) > new Date()" type="success">
              VIP · {{ row.vipExpireTime.slice(0,10) }}到期
            </el-tag>
            <el-tag v-else type="info">已过期</el-tag>
          </template>
          <span v-else style="color:#ccc">—</span>
        </template>
      </el-table-column>
      <el-table-column label="分销商" width="110" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.resellerLevel && row.resellerExpireTime && new Date(row.resellerExpireTime) > new Date()" type="warning">
            Lv{{ row.resellerLevel }} 分销商
          </el-tag>
          <span v-else style="color:#ccc">—</span>
        </template>
      </el-table-column>
      <el-table-column label="注册时间" prop="createTime" width="160" />
      <el-table-column label="操作" width="90" fixed="right">
        <template #default="{ row }">
          <el-button link type="warning" @click="handleGivePoints(row)">赠积分</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog title="赠送解锁积分" v-model="givePointsVisible" width="380px" append-to-body>
      <div style="color:#999;font-size:13px;margin-bottom:16px">积分到账后可用于解锁付费剧集。</div>
      <el-form label-width="80px">
        <el-form-item label="用户">
          <span style="font-weight:600">{{ currentUser?.nickname }}</span>
          <span style="color:#999;margin-left:8px">{{ currentUser?.mobile }}</span>
        </el-form-item>
        <el-form-item label="当前积分">
          <el-tag type="warning">{{ currentUser?.usable }} 积分</el-tag>
        </el-form-item>
        <el-form-item label="赠送数量" required>
          <el-input-number v-model="giveForm.amount" :min="1" :precision="0" :step="10" style="width:100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="giveForm.memo" placeholder="如：活动赠送、补偿等" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="givePointsVisible = false">取消</el-button>
        <el-button type="warning" :loading="submitting" @click="submitGivePoints">确认赠送</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { listDramaUser, rechargeUser } from '@/api/duanju/user'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const submitting = ref(false)
const list = ref([])
const total = ref(0)
const queryParams = reactive({ pageNum: 1, pageSize: 10, siteId: 1, mobile: '', nickname: '', isVip: '' })
const queryRef = ref()

const currentUser = ref(null)
const givePointsVisible = ref(false)
const giveForm = reactive({ amount: 100, memo: '' })

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

function handleGivePoints(row) {
  currentUser.value = row
  giveForm.amount = 100
  giveForm.memo = ''
  givePointsVisible.value = true
}

async function submitGivePoints() {
  submitting.value = true
  try {
    await rechargeUser(currentUser.value.id, { walletType: 'usable', amount: giveForm.amount, memo: giveForm.memo || '后台赠送积分' })
    ElMessage.success(`已向 ${currentUser.value.nickname} 赠送 ${giveForm.amount} 积分`)
    givePointsVisible.value = false
    getList()
  } finally {
    submitting.value = false
  }
}

getList()
</script>
