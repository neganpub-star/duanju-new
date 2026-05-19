<template>
  <div class="app-container usable-page">
    <!-- 工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <el-input
          v-model="filterText"
          placeholder="搜索套餐名称"
          clearable
          style="width: 240px"
          :prefix-icon="Search"
        />
        <el-select v-model="filterStatus" placeholder="全部状态" clearable style="width: 140px">
          <el-option label="上架" value="1" />
          <el-option label="下架" value="0" />
        </el-select>
      </div>
      <div class="toolbar-right">
        <el-button :icon="Refresh" circle @click="getList" />
        <el-button type="primary" :icon="Plus" @click="handleAdd">新增套餐</el-button>
      </div>
    </div>

    <!-- 表格 -->
    <el-table v-loading="loading" :data="filteredList" stripe class="usable-table" :header-cell-style="headerStyle">
      <el-table-column label="ID" prop="id" width="80" align="center">
        <template #default="{ row }">
          <span class="id-badge">#{{ row.id }}</span>
        </template>
      </el-table-column>

      <el-table-column label="套餐信息" min-width="220">
        <template #default="{ row }">
          <div class="pkg-cell">
            <div class="pkg-avatar" :style="{ background: avatarColor(row.id) }">
              <el-icon><Coin /></el-icon>
            </div>
            <div class="pkg-meta">
              <div class="pkg-name">{{ row.title || '—' }}</div>
              <div class="pkg-sub">默认名称</div>
            </div>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="多语言" min-width="260">
        <template #default="{ row }">
          <div class="i18n-list">
            <template v-for="lang in supportedLangs" :key="lang.code">
              <div class="i18n-row">
                <el-tag size="small" :type="langTagType(lang.code)" effect="light" class="i18n-tag">
                  {{ lang.code }}
                </el-tag>
                <span class="i18n-text" :class="{ 'i18n-empty': !i18nValue(row, lang.code) }">
                  {{ i18nValue(row, lang.code) || '未配置' }}
                </span>
              </div>
            </template>
            <div v-if="!supportedLangs.length" class="i18n-empty">—</div>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="点数" width="140" align="center">
        <template #default="{ row }">
          <div class="pts-badge">
            <span class="pts-num">{{ row.usable }}</span>
            <span class="pts-unit">点</span>
          </div>
          <div v-if="row.giveUsable" class="pts-gift">
            <el-icon><Present /></el-icon>
            <span>赠 {{ row.giveUsable }}</span>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="售价 / 首充价" width="170" align="center">
        <template #default="{ row }">
          <div class="price-cell">
            <span class="price-now">¥{{ row.price }}</span>
            <span v-if="row.originalPrice && Number(row.originalPrice) > Number(row.price)" class="price-original">
              ¥{{ row.originalPrice }}
            </span>
            <span v-if="row.firstPrice" class="price-first">首充 ¥{{ row.firstPrice }}</span>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="排序" width="100" align="center">
        <template #default="{ row }">
          <span class="weigh-badge" :class="weighClass(row.weigh)">{{ row.weigh ?? 0 }}</span>
        </template>
      </el-table-column>

      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === '1' ? 'success' : 'info'" effect="light" size="small" class="status-tag">
            <el-icon class="status-icon">
              <CircleCheck v-if="row.status === '1'" />
              <Hide v-else />
            </el-icon>
            <span>{{ row.status === '1' ? '上架' : '下架' }}</span>
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="170" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="primary" size="small" :icon="Edit" @click="handleEdit(row)">编辑</el-button>
          <el-divider direction="vertical" />
          <el-button link type="danger" size="small" :icon="Delete" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>

      <template #empty>
        <div class="empty-state">
          <el-icon class="empty-icon"><Coin /></el-icon>
          <div class="empty-text">暂无点数套餐</div>
          <el-button type="primary" :icon="Plus" @click="handleAdd">新增第一个套餐</el-button>
        </div>
      </template>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 编辑对话框 -->
    <el-dialog :title="dialog.title" v-model="dialog.visible" width="600px" append-to-body class="usable-dialog">
      <el-tabs v-model="activeTab">
        <!-- 基本信息 -->
        <el-tab-pane label="基本信息" name="basic">
          <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
            <el-form-item label="默认名称" prop="title">
              <el-input v-model="form.title" placeholder="简体中文名称（必填，作为默认回退）" />
            </el-form-item>
            <el-form-item label="点数" prop="usable">
              <el-input-number v-model="form.usable" :min="1" />
            </el-form-item>
            <el-form-item label="赠送点数">
              <el-input-number v-model="form.giveUsable" :min="0" />
            </el-form-item>
            <el-form-item label="价格" prop="price">
              <el-input-number v-model="form.price" :min="0" :precision="2" />
            </el-form-item>
            <el-form-item label="首充价">
              <el-input-number v-model="form.firstPrice" :min="0" :precision="2" placeholder="留空不启用" />
            </el-form-item>
            <el-form-item label="划线价">
              <el-input-number v-model="form.originalPrice" :min="0" :precision="2" />
            </el-form-item>
            <el-form-item label="排序">
              <el-input-number v-model="form.weigh" :min="0" />
            </el-form-item>
            <el-form-item label="状态">
              <el-radio-group v-model="form.status">
                <el-radio value="1">上架</el-radio>
                <el-radio value="0">下架</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 每个语言 tab -->
        <el-tab-pane
          v-for="lang in supportedLangs"
          :key="lang.code"
          :label="lang.label"
          :name="lang.code"
        >
          <el-form label-width="90px" style="margin-top:8px">
            <el-form-item label="套餐名称">
              <el-input
                v-model="i18nForm[lang.code].title"
                :placeholder="`${lang.label}套餐名称`"
              />
            </el-form-item>
            <el-form-item label="套餐描述">
              <el-input
                v-model="i18nForm[lang.code].desc"
                type="textarea"
                :rows="5"
                :placeholder="`${lang.label}套餐描述（支持 HTML）`"
              />
            </el-form-item>
            <el-form-item label="描述预览" v-if="i18nForm[lang.code].desc">
              <div class="preview-box" v-html="i18nForm[lang.code].desc" />
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>

      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { listUsable, addUsable, updateUsable, deleteUsable } from '@/api/commerce/usable'
import { listConfig } from '@/api/system/config'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Edit, Delete, Search, Refresh, Coin, CircleCheck, Hide, Present,
} from '@element-plus/icons-vue'

const LANG_LABELS = { 'zh-CN': '简体中文', 'zh-TW': '繁體中文', en: 'English' }

const loading = ref(false)
const list = ref([])
const total = ref(0)
const queryParams = reactive({ pageNum: 1, pageSize: 20, siteId: 1 })
const dialog = reactive({ visible: false, title: '' })
const form = ref({})
const formRef = ref()
const activeTab = ref('basic')
const supportedLangs = ref([{ code: 'zh-CN', label: '简体中文' }, { code: 'en', label: 'English' }])
const i18nForm = reactive({})

// 筛选
const filterText = ref('')
const filterStatus = ref('')

// 表头样式
const headerStyle = { background: '#f7f8fa', color: '#303133', fontWeight: 600 }

const rules = {
  title:  [{ required: true, message: '请输入套餐名称', trigger: 'blur' }],
  usable: [{ required: true, message: '请输入点数', trigger: 'blur' }],
  price:  [{ required: true, message: '请输入价格', trigger: 'blur' }],
}

// 过滤后的列表
const filteredList = computed(() => {
  let arr = list.value
  if (filterText.value) {
    const kw = filterText.value.trim().toLowerCase()
    arr = arr.filter(row => {
      if ((row.title || '').toLowerCase().includes(kw)) return true
      const map = parseJson(row.titleI18n)
      return Object.values(map).some(v => String(v).toLowerCase().includes(kw))
    })
  }
  if (filterStatus.value) {
    arr = arr.filter(row => String(row.status) === filterStatus.value)
  }
  return arr
})

function parseJson(str) {
  try { return str ? JSON.parse(str) : {} } catch { return {} }
}

function i18nValue(row, code) {
  const map = parseJson(row.titleI18n)
  return map[code] || ''
}

// 头像背景色（按 ID 稳定取色）
const AVATAR_COLORS = [
  'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
  'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
  'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
  'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
  'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
  'linear-gradient(135deg, #30cfd0 0%, #330867 100%)',
  'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)',
  'linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%)',
]
function avatarColor(id) {
  return AVATAR_COLORS[(id || 0) % AVATAR_COLORS.length]
}

function langTagType(code) {
  const map = { 'zh-CN': '', 'zh-TW': 'success', en: 'warning' }
  return map[code] ?? 'info'
}

function weighClass(w) {
  const v = Number(w) || 0
  if (v >= 100) return 'weigh-high'
  if (v >= 50) return 'weigh-mid'
  return 'weigh-low'
}

async function loadSupportedLangs() {
  try {
    const res = await listConfig()
    const configs = res.data || []
    const entry = configs.find(c => c.configKey === 'i18n.supported_langs')
    if (entry) {
      let codes = []
      try { codes = JSON.parse(entry.configValue) } catch { codes = entry.configValue.split(',').map(s => s.trim()) }
      supportedLangs.value = codes.filter(Boolean).map(code => ({ code, label: LANG_LABELS[code] || code }))
    }
  } catch { /* 使用默认值 */ }
  supportedLangs.value.forEach(l => { if (!i18nForm[l.code]) i18nForm[l.code] = { title: '', desc: '' } })
}

async function getList() {
  loading.value = true
  try {
    const res = await listUsable(queryParams)
    const data = res.data
    list.value = data.rows || (Array.isArray(data) ? data : [])
    total.value = data.total || list.value.length
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  form.value = { status: '1', usable: 100, giveUsable: 0, price: 6.0, weigh: 0, siteId: 1 }
  supportedLangs.value.forEach(l => { i18nForm[l.code] = { title: '', desc: '' } })
  activeTab.value = 'basic'
  dialog.title = '新增点数套餐'
  dialog.visible = true
}

function handleEdit(row) {
  form.value = { ...row }
  const titleMap = parseJson(row.titleI18n)
  const descMap  = parseJson(row.descI18n)
  supportedLangs.value.forEach(l => {
    i18nForm[l.code] = { title: titleMap[l.code] || '', desc: descMap[l.code] || '' }
  })
  activeTab.value = 'basic'
  dialog.title = '编辑点数套餐'
  dialog.visible = true
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除套餐"${row.title}"？`, '警告', { type: 'warning' })
  await deleteUsable(row.id)
  ElMessage.success('删除成功')
  getList()
}

async function submitForm() {
  await formRef.value?.validate()
  const titleI18n = {}
  const descI18n  = {}
  supportedLangs.value.forEach(l => {
    if (i18nForm[l.code].title) titleI18n[l.code] = i18nForm[l.code].title
    if (i18nForm[l.code].desc)  descI18n[l.code]  = i18nForm[l.code].desc
  })
  const payload = {
    ...form.value,
    titleI18n: JSON.stringify(titleI18n),
    descI18n:  Object.keys(descI18n).length ? JSON.stringify(descI18n) : null,
  }
  if (payload.id) {
    await updateUsable(payload.id, payload)
  } else {
    await addUsable(payload)
  }
  ElMessage.success('操作成功')
  dialog.visible = false
  getList()
}

loadSupportedLangs()
getList()
</script>

<style scoped>
.usable-page { padding: 16px; }

/* 工具栏 */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  margin-bottom: 12px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}
.toolbar-left { display: flex; gap: 10px; align-items: center; }
.toolbar-right { display: flex; gap: 8px; align-items: center; }

/* 表格 */
.usable-table {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}
.usable-table :deep(.el-table__inner-wrapper)::before { display: none; }
.usable-table :deep(.cell) { padding: 12px 12px; }

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

/* 套餐信息 */
.pkg-cell { display: flex; align-items: center; gap: 12px; }
.pkg-avatar {
  width: 40px; height: 40px;
  border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  color: #fff;
  font-size: 20px;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}
.pkg-meta { display: flex; flex-direction: column; gap: 2px; min-width: 0; }
.pkg-name { font-size: 14px; font-weight: 600; color: #303133; }
.pkg-sub { font-size: 11px; color: #c0c4cc; }

/* 多语言 */
.i18n-list { display: flex; flex-direction: column; gap: 4px; }
.i18n-row { display: flex; align-items: center; gap: 8px; font-size: 13px; }
.i18n-tag { width: 52px; text-align: center; font-family: ui-monospace, SFMono-Regular, Menlo, monospace; }
.i18n-text { color: #606266; }
.i18n-empty { color: #c0c4cc; font-style: italic; font-size: 12px; }

/* 点数徽章（金色调，呼应"点数/金币"语义） */
.pts-badge {
  display: inline-flex;
  align-items: baseline;
  gap: 2px;
  padding: 4px 12px;
  border-radius: 14px;
  background: linear-gradient(135deg, #fff7e6, #ffe7ba);
  color: #d46b08;
}
.pts-num { font-size: 16px; font-weight: 700; font-variant-numeric: tabular-nums; }
.pts-unit { font-size: 11px; opacity: 0.7; }
.pts-gift {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  margin-top: 4px;
  padding: 2px 8px;
  border-radius: 10px;
  background: #f0f9eb;
  color: #67c23a;
  font-size: 11px;
  font-weight: 500;
}

/* 价格 */
.price-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}
.price-now {
  font-size: 17px;
  font-weight: 700;
  color: #f56c6c;
  font-variant-numeric: tabular-nums;
}
.price-original {
  font-size: 11px;
  color: #c0c4cc;
  text-decoration: line-through;
}
.price-first {
  display: inline-block;
  margin-top: 2px;
  padding: 1px 8px;
  border-radius: 10px;
  background: #ecf5ff;
  color: #409eff;
  font-size: 11px;
  font-weight: 600;
}

/* 排序徽章 */
.weigh-badge {
  display: inline-block;
  min-width: 40px;
  padding: 4px 10px;
  border-radius: 12px;
  font-weight: 600;
  font-size: 13px;
  font-variant-numeric: tabular-nums;
}
.weigh-high { background: #fff3e0; color: #e65100; }
.weigh-mid  { background: #e3f2fd; color: #1565c0; }
.weigh-low  { background: #f5f5f5; color: #606266; }

/* 状态标签 */
.status-tag :deep(.el-tag__content),
.status-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  white-space: nowrap;
}
.status-icon { font-size: 12px; }

/* 空状态 */
.empty-state {
  display: flex; flex-direction: column; align-items: center; gap: 12px;
  padding: 40px 0;
}
.empty-icon { font-size: 56px; color: #dcdfe6; }
.empty-text { color: #909399; font-size: 14px; }

/* 富文本预览 */
.preview-box {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 12px;
  min-height: 60px;
  line-height: 1.8;
  font-size: 13px;
  color: #333;
  width: 100%;
}

/* 弹窗 */
.usable-dialog :deep(.el-dialog__header) {
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
  margin-right: 0;
}

/* 暗黑模式 */
html.dark .toolbar,
html.dark .usable-table {
  background: #1f1f1f;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.2);
}
html.dark .pkg-name { color: #e5e7eb; }
html.dark .id-badge { background: rgba(80, 72, 229, 0.15); color: #a5b4fc; }
html.dark .pts-badge { background: rgba(212, 107, 8, 0.15); color: #fdba74; }
</style>
