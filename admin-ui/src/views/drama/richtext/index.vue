<template>
  <div class="app-container richtext-page">
    <!-- 顶部提示 -->
    <div class="page-hint">
      <el-icon class="hint-icon"><InfoFilled /></el-icon>
      <div class="hint-text">
        <span class="hint-title">协议管理</span>
        <span class="hint-desc">左侧选择文档，右侧切换语言后编辑内容并保存；语言徽章亮起代表该语言已翻译。</span>
      </div>
    </div>

    <div class="richtext-layout">
      <!-- 左侧：文档列表 -->
      <div class="doc-list-card">
        <div class="card-header">
          <el-icon><Document /></el-icon>
          <span>协议文档</span>
          <span class="card-count">{{ docKeys.length }}</span>
        </div>
        <div class="doc-list">
          <div
            v-for="doc in docKeys"
            :key="doc.docKey"
            class="doc-item"
            :class="{ 'doc-item--active': doc.docKey === activeDocKey }"
            @click="handleSelect(doc.docKey)"
          >
            <div class="doc-icon"><el-icon><Tickets /></el-icon></div>
            <div class="doc-info">
              <div class="doc-title">{{ doc.title }}</div>
              <div class="doc-key">{{ doc.docKey }}</div>
            </div>
            <div class="doc-langs">
              <span
                v-for="lang in LANGS"
                :key="lang.code"
                class="lang-dot"
                :class="{ 'lang-dot--on': hasLang(doc.docKey, lang.code) }"
                :title="`${lang.label}${hasLang(doc.docKey, lang.code) ? ' 已翻译' : ' 未翻译'}`"
              >{{ lang.code }}</span>
            </div>
          </div>
          <div v-if="!docKeys.length" class="empty-state">
            <el-icon class="empty-icon"><Document /></el-icon>
            <div class="empty-text">暂无协议文档</div>
          </div>
        </div>
      </div>

      <!-- 右侧：编辑区 -->
      <div class="editor-card" v-if="activeDocKey">
        <div class="editor-header">
          <div class="editor-title-wrap">
            <span class="editor-title">{{ currentDocTitle }}</span>
            <span class="editor-doc-key">{{ activeDocKey }}</span>
          </div>
          <div class="editor-actions">
            <el-radio-group v-model="currentLang" size="small" @change="onLangChange">
              <el-radio-button
                v-for="lang in LANGS"
                :key="lang.code"
                :value="lang.code"
              >{{ lang.label }}</el-radio-button>
            </el-radio-group>
            <el-button type="primary" :icon="DocumentChecked" :loading="saving" @click="handleSave">保存</el-button>
          </div>
        </div>

        <div class="editor-body" v-loading="contentLoading">
          <div v-if="isNew" class="new-alert">
            <el-icon><Warning /></el-icon>
            <span>
              当前语言（<b>{{ LANG_LABELS[currentLang] }}</b>）暂无内容，保存后将自动创建。
            </span>
          </div>

          <el-form label-width="60px">
            <el-form-item label="标题">
              <el-input
                v-model="activeForm.title"
                :placeholder="`${LANG_LABELS[currentLang]}标题`"
                size="default"
                style="max-width: 600px"
              />
            </el-form-item>
            <el-form-item label="内容">
              <el-input
                v-model="activeForm.content"
                type="textarea"
                :rows="18"
                placeholder="支持 HTML 标签"
                class="content-textarea"
              />
            </el-form-item>
          </el-form>

          <div class="preview-section">
            <div class="preview-title">
              <el-icon><View /></el-icon>
              <span>内容预览</span>
            </div>
            <div class="preview-box" v-html="activeForm.content || '<span style=\'color:#c0c4cc\'>暂无内容</span>'" />
          </div>
        </div>
      </div>

      <!-- 未选中文档时 -->
      <div class="editor-card empty-card" v-else>
        <el-empty description="请从左侧选择协议文档" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { listDocKeys, getByDocKey, saveByDocKey } from '@/api/drama/richtext'
import { ElMessage } from 'element-plus'
import {
  Document, Tickets, InfoFilled, Warning, View, DocumentChecked,
} from '@element-plus/icons-vue'

const LANG_LABELS = { 'zh-CN': '简体中文', 'zh-TW': '繁體中文', 'en': 'English' }
const LANGS = [
  { code: 'zh-CN', label: '简体中文' },
  { code: 'zh-TW', label: '繁體中文' },
  { code: 'en',    label: 'English' },
]

const saving         = ref(false)
const contentLoading = ref(false)
const docKeys        = ref([])
const activeDocKey   = ref('')
const currentLang    = ref('zh-CN')
const activeForm     = ref({ title: '', content: '' })
const isNew          = ref(false)

// docKey -> Set<lang>
const existsMap = reactive({})

const currentDocTitle = computed(() => {
  const doc = docKeys.value.find(d => d.docKey === activeDocKey.value)
  return doc?.title || activeDocKey.value
})

function hasLang(docKey, lang) {
  return existsMap[docKey]?.has(lang) ?? false
}

async function loadDocKeys() {
  const res = await listDocKeys()
  docKeys.value = res.data || []
  docKeys.value.forEach(doc => {
    if (!existsMap[doc.docKey]) existsMap[doc.docKey] = new Set()
    existsMap[doc.docKey].add('zh-CN')
  })
  if (docKeys.value.length > 0 && !activeDocKey.value) {
    activeDocKey.value = docKeys.value[0].docKey
    await loadContent()
  }
}

async function loadContent() {
  if (!activeDocKey.value) return
  contentLoading.value = true
  try {
    const res = await getByDocKey(activeDocKey.value, currentLang.value)
    const data = res.data || {}
    activeForm.value = { title: data.title || '', content: data.content || '' }
    isNew.value = !data.id
    if (!isNew.value) {
      if (!existsMap[activeDocKey.value]) existsMap[activeDocKey.value] = new Set()
      existsMap[activeDocKey.value].add(currentLang.value)
    }
  } finally {
    contentLoading.value = false
  }
}

async function handleSelect(docKey) {
  activeDocKey.value = docKey
  await loadContent()
}

async function onLangChange() {
  await loadContent()
}

async function handleSave() {
  if (!activeForm.value.title.trim()) {
    ElMessage.warning('请填写标题')
    return
  }
  saving.value = true
  try {
    await saveByDocKey(activeDocKey.value, currentLang.value, {
      title: activeForm.value.title,
      content: activeForm.value.content,
    })
    ElMessage.success('保存成功')
    isNew.value = false
    if (!existsMap[activeDocKey.value]) existsMap[activeDocKey.value] = new Set()
    existsMap[activeDocKey.value].add(currentLang.value)
  } finally {
    saving.value = false
  }
}

onMounted(loadDocKeys)
</script>

<style scoped>
.richtext-page { padding: 12px; }

/* 顶部提示 */
.page-hint {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  margin-bottom: 12px;
  background: linear-gradient(135deg, #eef2ff 0%, #e0f2fe 100%);
  border-radius: 10px;
  border-left: 3px solid #5048e5;
}
.hint-icon { font-size: 22px; color: #5048e5; flex-shrink: 0; }
.hint-text { display: flex; flex-direction: column; line-height: 1.4; }
.hint-title { font-size: 14px; font-weight: 600; color: #303133; }
.hint-desc { font-size: 12px; color: #606266; }

/* 主布局 */
.richtext-layout {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 12px;
  align-items: flex-start;
}

/* 左侧卡片 */
.doc-list-card,
.editor-card {
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  overflow: hidden;
}
.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  background: #f7f8fa;
}
.card-count {
  margin-left: auto;
  padding: 1px 8px;
  border-radius: 10px;
  background: #fff;
  color: #909399;
  font-size: 11px;
  font-weight: 500;
}

/* 文档列表 */
.doc-list {
  padding: 8px;
  max-height: calc(100vh - 220px);
  overflow-y: auto;
}
.doc-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.15s;
  margin-bottom: 4px;
}
.doc-item:hover { background: #f5f7fa; }
.doc-item--active {
  background: linear-gradient(135deg, #eef2ff, #e0e7ff);
}
.doc-icon {
  width: 32px; height: 32px;
  border-radius: 8px;
  background: #fff;
  display: flex; align-items: center; justify-content: center;
  color: #5048e5;
  flex-shrink: 0;
}
.doc-item--active .doc-icon {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  box-shadow: 0 2px 6px rgba(102, 126, 234, 0.3);
}
.doc-info { flex: 1; min-width: 0; line-height: 1.3; }
.doc-title { font-size: 13px; font-weight: 600; color: #303133; }
.doc-item--active .doc-title { color: #5048e5; }
.doc-key {
  font-size: 11px;
  color: #909399;
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
}
.doc-langs {
  display: flex;
  flex-direction: column;
  gap: 2px;
  align-items: flex-end;
}
.lang-dot {
  display: inline-block;
  padding: 1px 6px;
  border-radius: 8px;
  font-size: 9px;
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
  background: #f4f6f9;
  color: #c0c4cc;
  font-weight: 600;
  letter-spacing: 0.3px;
}
.lang-dot--on { background: #f0f9eb; color: #67c23a; }

/* 编辑区 */
.editor-card.empty-card {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 400px;
}
.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  background: #f7f8fa;
}
.editor-title-wrap { display: flex; flex-direction: column; line-height: 1.3; }
.editor-title { font-size: 15px; font-weight: 600; color: #303133; }
.editor-doc-key {
  font-size: 11px;
  color: #909399;
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
}
.editor-actions { display: flex; align-items: center; gap: 10px; }

.editor-body { padding: 16px; }

.new-alert {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  margin-bottom: 16px;
  background: #fff7e6;
  border-left: 3px solid #d46b08;
  border-radius: 6px;
  color: #d46b08;
  font-size: 13px;
}
.new-alert b { color: #c41d7f; }

.content-textarea :deep(textarea) {
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
  font-size: 13px;
  line-height: 1.6;
}

/* 预览 */
.preview-section { margin-top: 16px; }
.preview-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  color: #606266;
  margin-bottom: 8px;
  padding-left: 4px;
  border-left: 3px solid #5048e5;
}
.preview-box {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 16px;
  min-height: 140px;
  line-height: 1.8;
  font-size: 14px;
  color: #333;
  background: #fafbfc;
}

/* 空状态 */
.empty-state {
  display: flex; flex-direction: column; align-items: center; gap: 8px;
  padding: 30px 0;
}
.empty-icon { font-size: 40px; color: #dcdfe6; }
.empty-text { color: #909399; font-size: 13px; }

/* 暗黑模式 */
html.dark .page-hint {
  background: linear-gradient(135deg, rgba(80, 72, 229, 0.15) 0%, rgba(64, 158, 255, 0.15) 100%);
}
html.dark .hint-title,
html.dark .editor-title,
html.dark .doc-title { color: #e5e7eb; }
html.dark .doc-list-card,
html.dark .editor-card {
  background: #1f1f1f;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.2);
}
html.dark .card-header,
html.dark .editor-header { background: #2a2a2a; }
html.dark .doc-item:hover { background: #2a2a2a; }
html.dark .doc-item--active { background: rgba(80, 72, 229, 0.15); }
html.dark .preview-box { background: #2a2a2a; color: #cfcfcf; }
</style>
