<template>
  <div class="app-container member-page">
    <!-- 工具栏 -->
    <div class="toolbar">
      <el-form :model="queryParams" :inline="true" ref="queryRef" class="toolbar-form">
        <el-form-item label="手机号" prop="mobile">
          <el-input v-model="queryParams.mobile" placeholder="手机号" clearable style="width:160px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="queryParams.nickname" placeholder="昵称" clearable style="width:150px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="VIP" prop="isVip">
          <el-select v-model="queryParams.isVip" placeholder="全部" clearable style="width:120px">
            <el-option label="有效VIP" value="1" />
            <el-option label="非VIP" value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleQuery">搜索</el-button>
          <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 表格 -->
    <el-table v-loading="loading" :data="list" stripe class="member-table" :header-cell-style="headerStyle">
      <el-table-column label="ID" prop="id" width="80" align="center">
        <template #default="{ row }">
          <span class="id-badge">#{{ row.id }}</span>
        </template>
      </el-table-column>

      <el-table-column label="用户" width="280">
        <template #default="{ row }">
          <div class="user-cell">
            <el-image v-if="row.avatar" :src="row.avatar" class="user-avatar" fit="cover">
              <template #error><div class="avatar-empty" title="头像加载失败">无</div></template>
              <template #placeholder><div class="avatar-empty">无</div></template>
            </el-image>
            <div v-else class="avatar-empty" title="未设置头像">无</div>
            <div class="user-info">
              <span class="user-name">{{ row.nickname || '—' }}</span>
              <span class="user-mobile">{{ row.mobile || '未绑定' }}</span>
            </div>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="点数余额" min-width="140" align="center">
        <template #default="{ row }">
          <div class="pts-badge">
            <span class="pts-num">{{ row.usable || 0 }}</span>
            <span class="pts-unit">点</span>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="VIP 状态" min-width="200" align="center">
        <template #default="{ row }">
          <div v-if="vipState(row).valid" class="vip-chip vip-chip--active">
            <el-icon><GoldMedal /></el-icon>
            <div class="vip-info">
              <span class="vip-label">VIP</span>
              <span class="vip-expire">剩 {{ vipState(row).daysLeft }} 天</span>
            </div>
          </div>
          <el-tag v-else-if="row.vipExpireTime" type="info" effect="light" size="small">
            已过期
          </el-tag>
          <span v-else class="cell-empty">无</span>
        </template>
      </el-table-column>

      <el-table-column label="分销身份" min-width="180" align="center">
        <template #default="{ row }">
          <div v-if="resellerState(row).valid" class="reseller-chip" :style="{ background: levelBg(row.resellerLevel), color: levelColor(row.resellerLevel) }">
            <el-icon><Medal /></el-icon>
            <span>Lv{{ row.resellerLevel }} 分销商</span>
          </div>
          <span v-else class="cell-empty">无</span>
        </template>
      </el-table-column>

      <el-table-column label="注册时间" min-width="180" align="center">
        <template #default="{ row }">
          <div class="time-cell">
            <span class="time-abs">{{ row.createTime ? row.createTime.slice(0,10) : '—' }}</span>
            <span v-if="row.createTime" class="time-rel">{{ relativeTime(row.createTime) }}</span>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="120" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="warning" size="small" :icon="Present" @click="handleGivePoints(row)">赠点数</el-button>
        </template>
      </el-table-column>

      <template #empty>
        <div class="empty-state">
          <el-icon class="empty-icon"><User /></el-icon>
          <div class="empty-text">暂无用户数据</div>
        </div>
      </template>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 赠送点数弹窗 -->
    <el-dialog
      v-model="givePointsVisible"
      width="480px"
      append-to-body
      class="app-dialog"
      :show-close="false"
    >
      <template #header>
        <div class="app-dialog__header">
          <div class="app-dialog__title">
            <el-icon class="app-dialog__icon"><Present /></el-icon>
            <span>赠送解锁点数</span>
            <el-tag v-if="currentUser?.id" effect="light" type="primary" round size="small" class="app-dialog__tag">#{{ currentUser.id }}</el-tag>
          </div>
          <el-icon class="app-dialog__close" @click="givePointsVisible = false"><Close /></el-icon>
        </div>
      </template>

      <div class="dialog-hint">
        <el-icon><InfoFilled /></el-icon>
        <span>点数到账后可用于解锁付费剧集</span>
      </div>

      <div class="app-section">
        <div class="app-section__title"><span class="app-section__bar"></span>用户信息</div>
        <el-form label-width="92px">
          <el-form-item label="用户">
            <div class="dialog-user">
              <el-image v-if="currentUser?.avatar" :src="currentUser.avatar" class="dialog-avatar" fit="cover">
                <template #error><div class="avatar-empty small">无</div></template>
              </el-image>
              <div v-else class="avatar-empty small">无</div>
              <div class="dialog-user-info">
                <span class="dialog-user-name">{{ currentUser?.nickname || '—' }}</span>
                <span class="dialog-user-mobile">{{ currentUser?.mobile || '未绑定' }}</span>
              </div>
            </div>
          </el-form-item>
          <el-form-item label="当前点数">
            <div class="pts-badge">
              <span class="pts-num">{{ currentUser?.usable || 0 }}</span>
              <span class="pts-unit">点</span>
            </div>
          </el-form-item>
        </el-form>
      </div>

      <div class="app-section app-section--warning">
        <div class="app-section__title"><span class="app-section__bar"></span>赠送内容</div>
        <el-form label-width="92px">
          <el-form-item label="赠送数量" required>
            <el-input-number v-model="giveForm.amount" :min="1" :precision="0" :step="10" style="width:100%" />
            <span class="app-hint">单位：点</span>
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="giveForm.memo" placeholder="如：活动赠送、补偿等" />
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <div class="app-dialog__footer">
          <el-button @click="givePointsVisible = false">取消</el-button>
          <el-button type="warning" :icon="Present" :loading="submitting" @click="submitGivePoints">确认赠送</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { listDramaUser, rechargeUser } from '@/api/duanju/user'
import { ElMessage } from 'element-plus'
import {
  Search, Refresh, Present, User, GoldMedal, Medal, InfoFilled, Close,
} from '@element-plus/icons-vue'

const loading = ref(false)
const submitting = ref(false)
const list = ref([])
const total = ref(0)
const queryParams = reactive({ pageNum: 1, pageSize: 10, siteId: 1, mobile: '', nickname: '', isVip: '' })
const queryRef = ref()

const currentUser = ref(null)
const givePointsVisible = ref(false)
const giveForm = reactive({ amount: 100, memo: '' })

const headerStyle = { background: '#f7f8fa', color: '#303133', fontWeight: 600 }

// VIP 状态判断与剩余天数
function vipState(row) {
  if (!row.vipExpireTime) return { valid: false, daysLeft: 0 }
  const expire = new Date(row.vipExpireTime)
  const now = new Date()
  if (expire <= now) return { valid: false, daysLeft: 0 }
  const days = Math.ceil((expire - now) / 86400000)
  return { valid: true, daysLeft: days }
}

// 分销身份判断
function resellerState(row) {
  if (!row.resellerLevel || !row.resellerExpireTime) return { valid: false }
  return { valid: new Date(row.resellerExpireTime) > new Date() }
}

// 等级颜色（与分销套餐页保持视觉一致）
const LEVEL_CHIP_BG = { 1: '#f5f5f5', 2: '#f0f9eb', 3: '#ecf5ff', 4: '#f0e7ff', 5: '#fff0f6', 6: '#fff7e6', 7: '#fff3e0', 8: '#ffe7e7', 9: '#e7eaff' }
const LEVEL_CHIP_COLOR = { 1: '#606266', 2: '#67c23a', 3: '#409eff', 4: '#722ed1', 5: '#c41d7f', 6: '#d46b08', 7: '#e65100', 8: '#cf1322', 9: '#1d39c4' }
function levelBg(lv) { return LEVEL_CHIP_BG[lv] || LEVEL_CHIP_BG[1] }
function levelColor(lv) { return LEVEL_CHIP_COLOR[lv] || LEVEL_CHIP_COLOR[1] }

// 相对时间
function relativeTime(t) {
  if (!t) return ''
  const diff = (Date.now() - new Date(t).getTime()) / 1000
  if (diff < 60) return '刚刚'
  if (diff < 3600) return Math.floor(diff / 60) + ' 分钟前'
  if (diff < 86400) return Math.floor(diff / 3600) + ' 小时前'
  if (diff < 30 * 86400) return Math.floor(diff / 86400) + ' 天前'
  if (diff < 365 * 86400) return Math.floor(diff / (30 * 86400)) + ' 个月前'
  return Math.floor(diff / (365 * 86400)) + ' 年前'
}

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
    await rechargeUser(currentUser.value.id, { walletType: 'usable', amount: giveForm.amount, memo: giveForm.memo || '' })
    ElMessage.success(`已向 ${currentUser.value.nickname} 赠送 ${giveForm.amount} 点数`)
    givePointsVisible.value = false
    getList()
  } finally {
    submitting.value = false
  }
}

getList()
</script>

<style scoped>
.member-page { padding: 12px; }

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
.member-table {
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}
.member-table :deep(.el-table__inner-wrapper)::before { display: none; }
.member-table :deep(.cell) { padding: 6px 10px; line-height: 1.5; }
.member-table :deep(.el-table__row) td { padding: 7px 0; }
.member-table :deep(th.el-table__cell) { padding: 8px 0; }

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
.user-cell { display: flex; align-items: center; gap: 10px; }
.user-avatar {
  width: 36px; height: 36px;
  border-radius: 50%;
  flex-shrink: 0;
  background: #f0f2f5;
  overflow: hidden;
}
.user-avatar :deep(img) { width: 100%; height: 100%; object-fit: cover; }
.avatar-empty {
  width: 36px; height: 36px;
  border-radius: 50%;
  background: #f4f6f9;
  border: 1px dashed #dcdfe6;
  color: #909399;
  font-size: 12px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  user-select: none;
}
.avatar-empty.small { width: 32px; height: 32px; font-size: 11px; }
.user-info { display: flex; flex-direction: column; gap: 0; line-height: 1.35; min-width: 0; }
.user-name {
  font-size: 13px; font-weight: 600; color: #303133;
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}
.user-mobile { font-size: 12px; color: #909399; }

/* 点数胶囊 */
.pts-badge {
  display: inline-flex;
  align-items: baseline;
  gap: 2px;
  padding: 3px 12px;
  border-radius: 14px;
  background: linear-gradient(135deg, #fff7e6, #ffe7ba);
  color: #d46b08;
}
.pts-num { font-size: 15px; font-weight: 700; font-variant-numeric: tabular-nums; }
.pts-unit { font-size: 11px; opacity: 0.7; }

/* VIP 状态 */
.vip-chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  border-radius: 14px;
  font-size: 12px;
  font-weight: 500;
}
.vip-chip--active {
  background: linear-gradient(135deg, #fef0e2 0%, #ffe7ba 100%);
  color: #d46b08;
  box-shadow: 0 1px 4px rgba(212, 107, 8, 0.15);
}
.vip-info { display: flex; flex-direction: column; line-height: 1.2; }
.vip-label { font-size: 12px; font-weight: 700; }
.vip-expire { font-size: 10px; opacity: 0.8; }

/* 分销身份 */
.reseller-chip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

/* 空值占位（VIP/分销 表内） */
.cell-empty {
  display: inline-block;
  padding: 3px 14px;
  border-radius: 12px;
  background: #f4f6f9;
  border: 1px dashed #dcdfe6;
  color: #909399;
  font-size: 11px;
  user-select: none;
}

/* 时间 */
.time-cell { display: flex; flex-direction: column; gap: 0; line-height: 1.3; }
.time-abs { font-size: 12px; color: #606266; font-variant-numeric: tabular-nums; }
.time-rel { font-size: 11px; color: #909399; }

/* 空状态 */
.empty-state {
  display: flex; flex-direction: column; align-items: center; gap: 12px;
  padding: 40px 0;
}
.empty-icon { font-size: 56px; color: #dcdfe6; }
.empty-text { color: #909399; font-size: 14px; }

/* 弹窗内提示 */
.dialog-hint {
  display: flex; align-items: center; gap: 6px;
  padding: 10px 12px;
  background: #f4f6f9;
  border-radius: 8px;
  color: #606266;
  font-size: 13px;
}
.dialog-user {
  display: flex; align-items: center; gap: 8px;
}
.dialog-avatar {
  width: 32px; height: 32px;
  border-radius: 50%;
  flex-shrink: 0;
  background: #f0f2f5;
  overflow: hidden;
}
.dialog-avatar :deep(img) { width: 100%; height: 100%; object-fit: cover; }
.dialog-user-info { display: flex; flex-direction: column; line-height: 1.3; }
.dialog-user-name { font-size: 13px; font-weight: 600; color: #303133; }
.dialog-user-mobile { font-size: 12px; color: #909399; }

/* 暗黑模式 */
html.dark .toolbar,
html.dark .member-table {
  background: #1f1f1f;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.2);
}
html.dark .user-name,
html.dark .dialog-user-name { color: #e5e7eb; }
html.dark .id-badge { background: rgba(80, 72, 229, 0.15); color: #a5b4fc; }
html.dark .pts-badge { background: rgba(212, 107, 8, 0.15); color: #fdba74; }
</style>
