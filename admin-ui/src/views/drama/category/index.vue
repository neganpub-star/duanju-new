<template>
  <div class="app-container category-page">
    <!-- 顶部统计卡片 -->
    <div class="stat-cards">
      <div class="stat-card stat-card--total">
        <div class="stat-icon">
          <el-icon><Menu /></el-icon>
        </div>
        <div class="stat-body">
          <div class="stat-label">分类总数</div>
          <div class="stat-value">{{ list.length }}</div>
        </div>
      </div>
      <div class="stat-card stat-card--normal">
        <div class="stat-icon">
          <el-icon><CircleCheck /></el-icon>
        </div>
        <div class="stat-body">
          <div class="stat-label">正常</div>
          <div class="stat-value">{{ normalCount }}</div>
        </div>
      </div>
      <div class="stat-card stat-card--hidden">
        <div class="stat-icon">
          <el-icon><Hide /></el-icon>
        </div>
        <div class="stat-body">
          <div class="stat-label">隐藏</div>
          <div class="stat-value">{{ hiddenCount }}</div>
        </div>
      </div>
      <div class="stat-card stat-card--i18n">
        <div class="stat-icon">
          <el-icon><ChatLineSquare /></el-icon>
        </div>
        <div class="stat-body">
          <div class="stat-label">支持语言</div>
          <div class="stat-value">{{ supportedLangs.length }}</div>
        </div>
      </div>
    </div>

    <!-- 工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <el-input
          v-model="filterText"
          placeholder="搜索分类名称"
          clearable
          style="width: 240px"
          :prefix-icon="Search"
        />
        <el-select v-model="filterStatus" placeholder="全部状态" clearable style="width: 140px">
          <el-option label="正常" value="normal" />
          <el-option label="隐藏" value="hidden" />
        </el-select>
      </div>
      <div class="toolbar-right">
        <el-button :icon="Refresh" circle @click="getList" />
        <el-button type="primary" :icon="Plus" @click="handleAdd">新增分类</el-button>
      </div>
    </div>

    <!-- 表格 -->
    <el-table v-loading="loading" :data="filteredList" stripe class="category-table" :header-cell-style="headerStyle">
      <el-table-column label="ID" prop="id" width="80" align="center">
        <template #default="{ row }">
          <span class="id-badge">#{{ row.id }}</span>
        </template>
      </el-table-column>

      <el-table-column label="分类名称" min-width="180">
        <template #default="{ row }">
          <div class="cat-cell">
            <div class="cat-avatar" :style="{ background: avatarColor(row.id) }">
              {{ (row.name || '?').charAt(0) }}
            </div>
            <div class="cat-meta">
              <div class="cat-name">{{ row.name || '—' }}</div>
              <div class="cat-sub">默认名称</div>
            </div>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="多语言" min-width="280">
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

      <el-table-column label="图标" width="120" align="center">
        <template #default="{ row }">
          <div v-if="isImageUrl(row.image)" class="icon-preview">
            <el-image :src="row.image" :preview-src-list="[row.image]" preview-teleported fit="cover">
              <template #error>
                <div class="icon-empty" title="图片加载失败">无</div>
              </template>
              <template #placeholder>
                <div class="icon-empty">无</div>
              </template>
            </el-image>
          </div>
          <el-tooltip v-else-if="row.image" :content="row.image" placement="top">
            <div class="icon-class">
              <el-icon><Picture /></el-icon>
              <span>{{ row.image }}</span>
            </div>
          </el-tooltip>
          <div v-else class="icon-empty" title="未设置图标">无</div>
        </template>
      </el-table-column>

      <el-table-column label="排序" width="100" align="center">
        <template #default="{ row }">
          <span class="weigh-badge" :class="weighClass(row.weigh)">{{ row.weigh ?? 0 }}</span>
        </template>
      </el-table-column>

      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 'normal' ? 'success' : 'info'" effect="light" size="small" class="status-tag">
            <el-icon class="status-icon">
              <CircleCheck v-if="row.status === 'normal'" />
              <Hide v-else />
            </el-icon>
            <span>{{ row.status === 'normal' ? '正常' : '隐藏' }}</span>
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
          <el-icon class="empty-icon"><Menu /></el-icon>
          <div class="empty-text">暂无分类数据</div>
          <el-button type="primary" :icon="Plus" @click="handleAdd">新增第一个分类</el-button>
        </div>
      </template>
    </el-table>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialog.visible"
      width="600px"
      append-to-body
      class="app-dialog"
      :show-close="false"
    >
      <template #header>
        <div class="app-dialog__header">
          <div class="app-dialog__title">
            <el-icon class="app-dialog__icon"><Menu /></el-icon>
            <span>{{ dialog.title }}</span>
            <el-tag v-if="form.id" effect="light" type="primary" round size="small" class="app-dialog__tag">#{{ form.id }}</el-tag>
          </div>
          <el-icon class="app-dialog__close" @click="dialog.visible = false"><Close /></el-icon>
        </div>
      </template>

      <el-tabs v-model="activeTab" class="app-dialog__tabs">
        <el-tab-pane label="基本信息" name="basic">
          <el-form ref="formRef" :model="form" :rules="rules" label-width="92px">
            <div class="app-section">
              <div class="app-section__title"><span class="app-section__bar"></span>基础信息</div>
              <el-form-item label="默认名称" prop="name">
                <el-input v-model="form.name" placeholder="中文名称（必填，作为多语言回退）" />
              </el-form-item>
            </div>

            <div class="app-section app-section--highlight">
              <div class="app-section__title">
                <span class="app-section__bar"></span>分类图标
                <el-tag v-if="form.image" type="success" size="small" effect="light" round>已设置</el-tag>
                <el-tag v-else type="info" size="small" effect="light" round>未设置</el-tag>
              </div>
              <el-form-item label="图标">
                <div class="icon-uploader">
                  <el-upload
                    action="#"
                    :show-file-list="false"
                    :before-upload="beforeIconUpload"
                    :http-request="(opt) => handleIconUpload(opt.file)"
                    accept="image/*"
                  >
                    <div v-if="form.image" class="icon-preview-wrap">
                      <el-image :src="form.image" class="icon-preview-img" fit="cover" />
                      <div class="icon-mask">
                        <el-icon><Edit /></el-icon>
                        <span>更换</span>
                      </div>
                    </div>
                    <div v-else class="upload-placeholder">
                      <el-icon class="upload-icon"><Plus /></el-icon>
                      <span>点击上传图标</span>
                    </div>
                  </el-upload>
                  <el-button
                    v-if="form.image"
                    link
                    type="danger"
                    size="small"
                    :icon="Delete"
                    class="icon-clear-btn"
                    @click="form.image = ''"
                  >移除图标</el-button>
                </div>
              </el-form-item>
            </div>

            <div class="app-section">
              <div class="app-section__title"><span class="app-section__bar"></span>显示与排序</div>
              <el-form-item label="显示状态">
                <el-radio-group v-model="form.status" class="app-segment">
                  <el-radio-button value="normal">
                    <el-icon><CircleCheck /></el-icon> 正常
                  </el-radio-button>
                  <el-radio-button value="hidden">
                    <el-icon><Hide /></el-icon> 隐藏
                  </el-radio-button>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="排序">
                <el-input-number v-model="form.weigh" :min="0" style="width:160px" />
                <span class="app-hint">数字越大排越前</span>
              </el-form-item>
            </div>
          </el-form>
        </el-tab-pane>

        <el-tab-pane
          v-for="lang in supportedLangs"
          :key="lang.code"
          :label="lang.label"
          :name="lang.code"
        >
          <el-form label-width="92px">
            <div class="app-section">
              <div class="app-section__title"><span class="app-section__bar"></span>{{ lang.label }} 内容</div>
              <el-form-item label="分类名称">
                <el-input v-model="i18nForm[lang.code]" :placeholder="`${lang.label}分类名称`" />
              </el-form-item>
            </div>
          </el-form>
        </el-tab-pane>
      </el-tabs>

      <template #footer>
        <div class="app-dialog__footer">
          <el-button @click="dialog.visible = false">取消</el-button>
          <el-button type="primary" :icon="Check" @click="submitForm">确定保存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { listCategory, addCategory, updateCategory, deleteCategory } from '@/api/drama/category'
import { listConfig, uploadFile } from '@/api/system/config'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Edit, Delete, Search, Refresh, Menu, CircleCheck, Hide, Picture, ChatLineSquare, Close, Check,
} from '@element-plus/icons-vue'

const LANG_LABELS = { 'zh-CN': '简体中文', 'zh-TW': '繁體中文', en: 'English' }

const loading = ref(false)
const list = ref([])
const dialog = reactive({ visible: false, title: '' })
const form = ref({})
const formRef = ref()
const activeTab = ref('basic')
const rules = { name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }] }

const supportedLangs = ref([{ code: 'zh-CN', label: '简体中文' }, { code: 'en', label: 'English' }])
const i18nForm = reactive({})

// 筛选
const filterText = ref('')
const filterStatus = ref('')

// 表头样式
const headerStyle = { background: '#f7f8fa', color: '#303133', fontWeight: 600 }

// 统计
const normalCount = computed(() => list.value.filter(i => i.status === 'normal').length)
const hiddenCount = computed(() => list.value.filter(i => i.status !== 'normal').length)

// 过滤后的列表
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
  if (filterStatus.value) {
    arr = arr.filter(row => row.status === filterStatus.value)
  }
  return arr
})

function parseJson(str) {
  try { return str ? JSON.parse(str) : {} } catch { return {} }
}

function i18nValue(row, code) {
  const map = parseJson(row.nameI18n)
  return map[code] || ''
}

function isImageUrl(v) {
  if (!v) return false
  return /^https?:\/\//i.test(v) || /\.(png|jpe?g|gif|webp|svg)$/i.test(v)
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
  // 不同语言对应不同 tag 色
  const map = { 'zh-CN': '', 'zh-TW': 'success', en: 'warning' }
  return map[code] ?? 'info'
}

function weighClass(w) {
  const v = Number(w) || 0
  if (v >= 100) return 'weigh-high'
  if (v >= 50) return 'weigh-mid'
  return 'weigh-low'
}

function initI18nForm(langs) {
  langs.forEach(l => { if (i18nForm[l.code] === undefined) i18nForm[l.code] = '' })
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
  initI18nForm(supportedLangs.value)
}

async function getList() {
  loading.value = true
  try {
    const res = await listCategory({ siteId: 1 })
    list.value = res.data.rows || res.data
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  form.value = { status: 'normal', weigh: 0, siteId: 1 }
  supportedLangs.value.forEach(l => { i18nForm[l.code] = '' })
  activeTab.value = 'basic'
  dialog.title = '新增分类'
  dialog.visible = true
}

function handleEdit(row) {
  form.value = { ...row }
  const nameMap = parseJson(row.nameI18n)
  supportedLangs.value.forEach(l => { i18nForm[l.code] = nameMap[l.code] || '' })
  activeTab.value = 'basic'
  dialog.title = '编辑分类'
  dialog.visible = true
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除分类"${row.name}"？`, '警告', { type: 'warning' })
  await deleteCategory(row.id)
  ElMessage.success('删除成功')
  getList()
}

function beforeIconUpload(file) {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isImage) ElMessage.error('只能上传图片文件')
  if (!isLt2M) ElMessage.error('图标不能超过 2MB')
  return isImage && isLt2M
}

async function handleIconUpload(file) {
  const res = await uploadFile(file)
  form.value.image = res.data
  ElMessage.success('上传成功')
}

async function submitForm() {
  await formRef.value?.validate()
  const nameI18n = {}
  supportedLangs.value.forEach(l => { if (i18nForm[l.code]) nameI18n[l.code] = i18nForm[l.code] })
  const payload = {
    ...form.value,
    nameI18n: Object.keys(nameI18n).length ? JSON.stringify(nameI18n) : null,
  }
  if (payload.id) {
    await updateCategory(payload.id, payload)
  } else {
    await addCategory(payload)
  }
  ElMessage.success('操作成功')
  dialog.visible = false
  getList()
}

loadSupportedLangs()
getList()
</script>

<style scoped>
.category-page {
  padding: 16px;
}

/* 统计卡片 */
.stat-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}
.stat-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 20px;
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  transition: transform 0.2s, box-shadow 0.2s;
  position: relative;
  overflow: hidden;
}
.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.08);
}
.stat-card::before {
  content: '';
  position: absolute;
  inset: 0;
  opacity: 0.06;
  pointer-events: none;
}
.stat-card--total::before { background: linear-gradient(135deg, #667eea, #764ba2); }
.stat-card--normal::before { background: linear-gradient(135deg, #43e97b, #38f9d7); }
.stat-card--hidden::before { background: linear-gradient(135deg, #909399, #c8c9cc); }
.stat-card--i18n::before { background: linear-gradient(135deg, #fa709a, #fee140); }

.stat-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: #fff;
  flex-shrink: 0;
}
.stat-card--total .stat-icon { background: linear-gradient(135deg, #667eea, #764ba2); }
.stat-card--normal .stat-icon { background: linear-gradient(135deg, #43e97b, #38f9d7); }
.stat-card--hidden .stat-icon { background: linear-gradient(135deg, #909399, #c8c9cc); }
.stat-card--i18n .stat-icon { background: linear-gradient(135deg, #fa709a, #fee140); }

.stat-body { display: flex; flex-direction: column; gap: 2px; }
.stat-label { font-size: 12px; color: #909399; }
.stat-value { font-size: 22px; font-weight: 700; color: #303133; line-height: 1.2; }

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
.category-table {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}
.category-table :deep(.el-table__inner-wrapper)::before { display: none; }
.category-table :deep(.cell) { padding: 12px 12px; }
.category-table :deep(.el-table__row) { transition: background 0.15s; }

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

/* 分类名称单元格 */
.cat-cell { display: flex; align-items: center; gap: 12px; }
.cat-avatar {
  width: 40px; height: 40px;
  border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  color: #fff;
  font-size: 16px;
  font-weight: 700;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}
.cat-meta { display: flex; flex-direction: column; gap: 2px; min-width: 0; }
.cat-name { font-size: 14px; font-weight: 600; color: #303133; }
.cat-sub { font-size: 11px; color: #c0c4cc; }

/* 多语言 */
.i18n-list { display: flex; flex-direction: column; gap: 4px; }
.i18n-row { display: flex; align-items: center; gap: 8px; font-size: 13px; }
.i18n-tag { width: 52px; text-align: center; font-family: ui-monospace, SFMono-Regular, Menlo, monospace; }
.i18n-text { color: #606266; }
.i18n-empty { color: #c0c4cc; font-style: italic; font-size: 12px; }

/* 图标列 */
.icon-preview :deep(.el-image) {
  width: 48px; height: 48px;
  border-radius: 8px;
  border: 1px solid #eee;
  background: #f5f7fa;
}
.icon-class {
  display: inline-flex; align-items: center; gap: 4px;
  max-width: 100%;
  padding: 4px 10px;
  border-radius: 14px;
  background: #f4f6f9;
  font-size: 12px;
  color: #606266;
}
.icon-class span {
  max-width: 70px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
/* 图标缺失/加载失败的占位：保持与图片同尺寸，对齐表格视觉 */
.icon-empty {
  width: 48px; height: 48px;
  border-radius: 8px;
  background: #f4f6f9;
  border: 1px dashed #dcdfe6;
  color: #909399;
  font-size: 13px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  user-select: none;
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

/* 状态标签：图标 + 文字单行 */
.status-tag :deep(.el-tag__content),
.status-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  white-space: nowrap;
}
.status-icon { font-size: 12px; }

/* 文本柔化 */
.text-muted { color: #c0c4cc; font-size: 12px; }

/* 空状态 */
.empty-state {
  display: flex; flex-direction: column; align-items: center; gap: 12px;
  padding: 40px 0;
}
.empty-icon { font-size: 56px; color: #dcdfe6; }
.empty-text { color: #909399; font-size: 14px; }

/* 弹窗内 - 图标上传 */
.icon-uploader { display: flex; flex-direction: column; align-items: flex-start; gap: 6px; }
.icon-preview-wrap {
  position: relative;
  width: 96px; height: 96px;
  border-radius: 10px;
  overflow: hidden;
  border: 1px solid #e6e8eb;
  cursor: pointer;
}
.icon-preview-img { width: 100%; height: 100%; display: block; }
.icon-mask {
  position: absolute; inset: 0;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  gap: 4px;
  color: #fff; font-size: 12px;
  background: rgba(0, 0, 0, 0.45);
  opacity: 0;
  transition: opacity 0.2s;
}
.icon-preview-wrap:hover .icon-mask { opacity: 1; }
.icon-mask .el-icon { font-size: 18px; }
.upload-placeholder {
  width: 96px; height: 96px;
  border: 1px dashed #d9d9d9; border-radius: 10px;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  cursor: pointer; color: #8c939d; font-size: 12px; gap: 6px;
  transition: border-color 0.2s, color 0.2s;
}
.upload-placeholder:hover { border-color: var(--el-color-primary); color: var(--el-color-primary); }
.upload-icon { font-size: 22px; }
.icon-clear-btn { padding: 0; height: auto; }

/* 暗黑模式适配 */
html.dark .stat-card,
html.dark .toolbar,
html.dark .category-table {
  background: #1f1f1f;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.2);
}
html.dark .stat-value { color: #e5e7eb; }
html.dark .cat-name { color: #e5e7eb; }
html.dark .icon-class { background: #2a2a2a; color: #cfcfcf; }
html.dark .id-badge { background: rgba(80, 72, 229, 0.15); color: #a5b4fc; }
</style>
