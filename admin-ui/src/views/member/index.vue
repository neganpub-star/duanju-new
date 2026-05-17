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
      <el-table-column label="余额(元)" prop="money" width="90" align="right">
        <template #default="{ row }">¥{{ row.money }}</template>
      </el-table-column>
      <el-table-column label="解锁积分" prop="usable" width="90" align="center">
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
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button link type="warning" @click="handleGivePoints(row)">赠积分</el-button>
          <el-button link type="primary" @click="handleRecharge(row)">充值</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 赠积分弹窗：专门给解锁积分(usable)快速加分 -->
    <el-dialog title="赠送解锁积分" v-model="givePointsVisible" width="380px" append-to-body>
      <div style="margin-bottom:16px;color:#666;font-size:13px;">
        积分用于解锁付费剧集，赠送后立即到账。
      </div>
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
          <el-input v-model="giveForm.memo" placeholder="备注（选填，如：活动赠送）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="givePointsVisible = false">取消</el-button>
        <el-button type="warning" :loading="submitting" @click="submitGivePoints">确认赠送</el-button>
      </template>
    </el-dialog>

    <!-- 通用充值弹窗 -->
    <el-dialog title="账户充值" v-model="rechargeVisible" width="380px" append-to-body>
      <el-form label-width="90px">
        <el-form-item label="用户">
          <span style="font-weight:600">{{ currentUser?.nickname }}</span>
          <span style="color:#999;margin-left:8px">{{ currentUser?.mobile }}</span>
        </el-form-item>
        <el-form-item label="充值类型">
          <el-select v-model="rechargeForm.walletType" style="width:100%" @change="onWalletTypeChange">
            <el-option label="余额（元，可提现）" value="money" />
            <el-option label="解锁积分（usable，用于解锁剧集）" value="usable" />
            <el-option label="积分（score，任务获得）" value="score" />
          </el-select>
        </el-form-item>
        <el-form-item label="金额/数量" required>
          <el-input-number
            v-model="rechargeForm.amount"
            :min="rechargeForm.walletType === 'money' ? 0.01 : 1"
            :precision="rechargeForm.walletType === 'money' ? 2 : 0"
            style="width:100%"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="rechargeForm.memo" placeholder="备注（选填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rechargeVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitRecharge">确认充值</el-button>
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

// 赠积分（usable）
const givePointsVisible = ref(false)
const giveForm = reactive({ amount: 100, memo: '' })

// 通用充值
const rechargeVisible = ref(false)
const rechargeForm = reactive({ walletType: 'usable', amount: 100, memo: '' })

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

function handleRecharge(row) {
  currentUser.value = row
  rechargeForm.walletType = 'usable'
  rechargeForm.amount = 100
  rechargeForm.memo = ''
  rechargeVisible.value = true
}

function onWalletTypeChange() {
  rechargeForm.amount = rechargeForm.walletType === 'money' ? 10 : 100
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

async function submitRecharge() {
  submitting.value = true
  try {
    await rechargeUser(currentUser.value.id, rechargeForm)
    ElMessage.success('充值成功')
    rechargeVisible.value = false
    getList()
  } finally {
    submitting.value = false
  }
}

getList()
</script>
