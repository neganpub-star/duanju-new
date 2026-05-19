<template>
  <div class="app-container reseller-page">
    <!-- 工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <el-input
          v-model="filterText"
          placeholder="搜索套餐名"
          clearable
          style="width: 240px"
          :prefix-icon="Search"
        />
        <el-select v-model="filterStatus" placeholder="全部状态" clearable style="width: 140px">
          <el-option label="上架" value="normal" />
          <el-option label="下架" value="hidden" />
        </el-select>
        <el-select v-model="filterLevel" placeholder="全部等级" clearable style="width: 140px">
          <el-option v-for="lv in availableLevels" :key="lv" :label="`Lv${lv}`" :value="lv" />
        </el-select>
      </div>
      <div class="toolbar-right">
        <el-button :icon="Refresh" circle @click="getList" />
        <el-button type="primary" :icon="Plus" @click="handleAdd">新增套餐</el-button>
      </div>
    </div>

    <!-- 表格 -->
    <el-table v-loading="loading" :data="filteredList" stripe class="reseller-table" :header-cell-style="headerStyle">
      <el-table-column label="ID" prop="id" width="80" align="center">
        <template #default="{ row }">
          <span class="id-badge">#{{ row.id }}</span>
        </template>
      </el-table-column>

      <el-table-column label="套餐信息" min-width="220">
        <template #default="{ row }">
          <div class="pkg-cell">
            <div class="pkg-avatar" :style="{ background: levelGradient(row.level) }">
              <el-icon><Medal /></el-icon>
            </div>
            <div class="pkg-meta">
              <div class="pkg-name">{{ row.name || '—' }}</div>
              <div class="pkg-sub">
                <span class="level-chip" :style="{ background: levelChipBg(row.level), color: levelChipColor(row.level) }">
                  Lv{{ row.level }} 分销商
                </span>
              </div>
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

      <el-table-column label="有效期" width="110" align="center">
        <template #default="{ row }">
          <div class="days-badge">
            <span class="days-num">{{ row.expire }}</span>
            <span class="days-unit">天</span>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="价格" width="110" align="center">
        <template #default="{ row }">
          <span class="price-now">¥{{ row.price }}</span>
        </template>
      </el-table-column>

      <el-table-column label="分佣比例" width="160" align="center">
        <template #default="{ row }">
          <div class="commission-cell">
            <div class="commission-row commission-direct">
              <span class="commission-label">直接</span>
              <span class="commission-value">{{ row.direct }}%</span>
            </div>
            <div class="commission-row commission-indirect">
              <span class="commission-label">间接</span>
              <span class="commission-value">{{ row.indirect }}%</span>
            </div>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 'normal' ? 'success' : 'info'" effect="light" size="small" class="status-tag">
            <el-icon class="status-icon">
              <CircleCheck v-if="row.status === 'normal'" />
              <Hide v-else />
            </el-icon>
            <span>{{ row.status === 'normal' ? '上架' : '下架' }}</span>
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
          <el-icon class="empty-icon"><Medal /></el-icon>
          <div class="empty-text">暂无分销套餐</div>
          <el-button type="primary" :icon="Plus" @click="handleAdd">新增第一个套餐</el-button>
        </div>
      </template>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 编辑对话框 -->
    <el-dialog :title="dialog.title" v-model="dialog.visible" width="540px" append-to-body class="reseller-dialog">
      <el-tabs v-model="activeTab">
        <!-- 基本信息 -->
        <el-tab-pane label="基本信息" name="basic">
          <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" style="margin-top:8px">
            <el-form-item label="套餐名" prop="name">
              <el-input v-model="form.name" placeholder="简体中文名称（作为默认回退）" />
            </el-form-item>
            <el-form-item label="等级" prop="level">
              <el-input-number v-model="form.level" :min="1" :max="9" />
            </el-form-item>
            <el-form-item label="有效天数" prop="expire">
              <el-input-number v-model="form.expire" :min="1" />
            </el-form-item>
            <el-form-item label="价格" prop="price">
              <el-input-number v-model="form.price" :min="0" :precision="2" />
            </el-form-item>
            <el-form-item label="直接佣金(%)" prop="direct">
              <el-input-number v-model="form.direct" :min="0" :max="100" :precision="2" />
            </el-form-item>
            <el-form-item label="间接佣金(%)" prop="indirect">
              <el-input-number v-model="form.indirect" :min="0" :max="100" :precision="2" />
            </el-form-item>
            <el-form-item label="状态">
              <el-radio-group v-model="form.status">
                <el-radio value="normal">上架</el-radio>
                <el-radio value="hidden">下架</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 多语言 tab -->
        <el-tab-pane
          v-for="lang in supportedLangs"
          :key="lang.code"
          :label="lang.label"
          :name="lang.code"
        >
          <el-form label-width="100px" style="margin-top:8px">
            <el-form-item label="套餐名称">
              <el-input
                v-model="i18nForm[lang.code].name"
                :placeholder="`${lang.label}套餐名称（留空则显示默认中文名）`"
              />
            </el-form-item>
            <el-form-item label="套餐描述">
              <el-input
                v-model="i18nForm[lang.code].content"
                type="textarea"
                :rows="4"
                :placeholder="`${lang.label}套餐描述（支持 HTML，留空则不展示）`"
              />
            </el-form-item>
            <el-form-item label="描述预览" v-if="i18nForm[lang.code].content">
              <div class="preview-box" v-html="i18nForm[lang.code].content" />
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
import { listReseller, addReseller, updateReseller, deleteReseller } from '@/api/commerce/reseller'
import { listConfig } from '@/api/system/config'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Edit, Delete, Search, Refresh, Medal, CircleCheck, Hide,
} from '@element-plus/icons-vue'

const LANG_LABELS = { 'zh-CN': '简体中文', 'zh-TW': '繁體中文', en: 'English' }

const loading = ref(false)
const list = ref([])
const total = ref(0)
const queryParams = reactive({ pageNum: 1, pageSize: 10, siteId: 1 })
const dialog = reactive({ visible: false, title: '' })
const form = ref({})
const formRef = ref()
const activeTab = ref('basic')
const supportedLangs = ref([{ code: 'zh-CN', label: '简体中文' }, { code: 'zh-TW', label: '繁體中文' }, { code: 'en', label: 'English' }])
const i18nForm = reactive({})

// 筛选
const filterText = ref('')
const filterStatus = ref('')
const filterLevel = ref('')

// 表头样式
const headerStyle = { background: '#f7f8fa', color: '#303133', fontWeight: 600 }

const rules = { name: [{ required: true, message: '请输入套餐名', trigger: 'blur' }] }

// 可选等级（基于当前数据）
const availableLevels = computed(() => {
  const set = new Set(list.value.map(i => i.level).filter(v => v != null))
  return Array.from(set).sort((a, b) => a - b)
})

const filteredList = computed(() => {
  let arr = list.value
  if (filterText.value) {
    const kw = filterText.value.trim().toLowerCase()
    arr = arr.filter(row => {
      if ((row.name || '').toLowerCase().includes(kw)) return true
      const map = parseJson(row.nameI18n)
      return Object.values(map).some(v => String(v).toLowerCase().includes(kw))
    })
  }
  if (filterStatus.value) arr = arr.filter(row => row.status === filterStatus.value)
  if (filterLevel.value) arr = arr.filter(row => row.level === filterLevel.value)
  return arr
})

function parseJson(str) {
  try { return str ? JSON.parse(str) : {} } catch { return {} }
}

function i18nValue(row, code) {
  const map = parseJson(row.nameI18n)
  return map[code] || ''
}

// 等级渐变（不同等级不同色系，呼应"段位"语义）
const LEVEL_GRADIENTS = {
  1: 'linear-gradient(135deg, #909399 0%, #c0c4cc 100%)',          // 青铜灰
  2: 'linear-gradient(135deg, #909399 0%, #67c23a 100%)',          // 白银
  3: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',          // 黄金（蓝）
  4: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',          // 铂金（紫）
  5: 'linear-gradient(135deg, #f5576c 0%, #fa709a 100%)',          // 钻石（粉红）
  6: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',          // 大师（橙金）
  7: 'linear-gradient(135deg, #fee140 0%, #f56c6c 100%)',          // 宗师（金红）
  8: 'linear-gradient(135deg, #f56c6c 0%, #5e2ced 100%)',          // 王者（红紫）
  9: 'linear-gradient(135deg, #1e3c72 0%, #2a5298 50%, #f5576c 100%)', // 至尊（深蓝→红）
}
const LEVEL_CHIP_BG = {
  1: '#f5f5f5', 2: '#f0f9eb', 3: '#ecf5ff', 4: '#f0e7ff',
  5: '#fff0f6', 6: '#fff7e6', 7: '#fff3e0', 8: '#ffe7e7', 9: '#e7eaff',
}
const LEVEL_CHIP_COLOR = {
  1: '#606266', 2: '#67c23a', 3: '#409eff', 4: '#722ed1',
  5: '#c41d7f', 6: '#d46b08', 7: '#e65100', 8: '#cf1322', 9: '#1d39c4',
}
function levelGradient(lv) { return LEVEL_GRADIENTS[lv] || LEVEL_GRADIENTS[1] }
function levelChipBg(lv)   { return LEVEL_CHIP_BG[lv]   || LEVEL_CHIP_BG[1] }
function levelChipColor(lv){ return LEVEL_CHIP_COLOR[lv]|| LEVEL_CHIP_COLOR[1] }

function langTagType(code) {
  const map = { 'zh-CN': '', 'zh-TW': 'success', en: 'warning' }
  return map[code] ?? 'info'
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
  supportedLangs.value.forEach(l => { if (!i18nForm[l.code]) i18nForm[l.code] = { name: '', content: '' } })
}

async function getList() {
  loading.value = true
  try {
    const res = await listReseller(queryParams)
    list.value = res.data.rows
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  form.value = { status: 'normal', level: 1, expire: 365, price: 99, direct: 10, indirect: 5, weigh: 0, siteId: 1 }
  supportedLangs.value.forEach(l => { i18nForm[l.code] = { name: '', content: '' } })
  activeTab.value = 'basic'
  dialog.title = '新增分销套餐'
  dialog.visible = true
}

function handleEdit(row) {
  form.value = { ...row }
  const nameMap = parseJson(row.nameI18n)
  const contentMap = parseJson(row.contentI18n)
  supportedLangs.value.forEach(l => {
    i18nForm[l.code] = {
      name: nameMap[l.code] || (l.code === 'zh-CN' ? row.name : ''),
      content: contentMap[l.code] || (l.code === 'zh-CN' ? row.content : ''),
    }
  })
  activeTab.value = 'basic'
  dialog.title = '编辑分销套餐'
  dialog.visible = true
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除"${row.name}"？`, '警告', { type: 'warning' })
  await deleteReseller(row.id)
  ElMessage.success('删除成功')
  getList()
}

async function submitForm() {
  await formRef.value?.validate()
  const nameI18n = {}
  const contentI18n = {}
  supportedLangs.value.forEach(l => {
    if (i18nForm[l.code]?.name) nameI18n[l.code] = i18nForm[l.code].name
    if (i18nForm[l.code]?.content) contentI18n[l.code] = i18nForm[l.code].content
  })
  const payload = {
    ...form.value,
    nameI18n: Object.keys(nameI18n).length ? JSON.stringify(nameI18n) : null,
    contentI18n: Object.keys(contentI18n).length ? JSON.stringify(contentI18n) : null,
  }
  if (payload.id) {
    await updateReseller(payload.id, payload)
  } else {
    await addReseller(payload)
  }
  ElMessage.success('操作成功')
  dialog.visible = false
  getList()
}

loadSupportedLangs()
getList()
</script>

<style scoped>
.reseller-page { padding: 16px; }

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
.reseller-table {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}
.reseller-table :deep(.el-table__inner-wrapper)::before { display: none; }
.reseller-table :deep(.cell) { padding: 12px 12px; }

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
.pkg-meta { display: flex; flex-direction: column; gap: 4px; min-width: 0; }
.pkg-name { font-size: 14px; font-weight: 600; color: #303133; }
.pkg-sub { display: flex; align-items: center; gap: 6px; }
.level-chip {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 11px;
  font-weight: 600;
}

/* 多语言 */
.i18n-list { display: flex; flex-direction: column; gap: 4px; }
.i18n-row { display: flex; align-items: center; gap: 8px; font-size: 13px; }
.i18n-tag { width: 52px; text-align: center; font-family: ui-monospace, SFMono-Regular, Menlo, monospace; }
.i18n-text { color: #606266; }
.i18n-empty { color: #c0c4cc; font-style: italic; font-size: 12px; }

/* 天数胶囊 */
.days-badge {
  display: inline-flex;
  align-items: baseline;
  gap: 2px;
  padding: 4px 12px;
  border-radius: 14px;
  background: linear-gradient(135deg, #e3f2fd, #bbdefb);
  color: #1565c0;
}
.days-num { font-size: 16px; font-weight: 700; font-variant-numeric: tabular-nums; }
.days-unit { font-size: 11px; opacity: 0.7; }

/* 价格 */
.price-now {
  font-size: 17px;
  font-weight: 700;
  color: #f56c6c;
  font-variant-numeric: tabular-nums;
}

/* 佣金双行 */
.commission-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
  align-items: center;
}
.commission-row {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 2px 10px;
  border-radius: 10px;
  font-size: 12px;
  min-width: 100px;
  justify-content: space-between;
}
.commission-direct   { background: #f0f9eb; color: #67c23a; }
.commission-indirect { background: #fff7e6; color: #d46b08; }
.commission-label { font-size: 11px; opacity: 0.8; }
.commission-value { font-weight: 700; font-variant-numeric: tabular-nums; }

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
  min-height: 48px;
  line-height: 1.8;
  font-size: 13px;
  color: #333;
  width: 100%;
}

/* 弹窗 */
.reseller-dialog :deep(.el-dialog__header) {
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
  margin-right: 0;
}

/* 暗黑模式 */
html.dark .toolbar,
html.dark .reseller-table {
  background: #1f1f1f;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.2);
}
html.dark .pkg-name { color: #e5e7eb; }
html.dark .id-badge { background: rgba(80, 72, 229, 0.15); color: #a5b4fc; }
html.dark .days-badge { background: rgba(21, 101, 192, 0.15); color: #90caf9; }
</style>
