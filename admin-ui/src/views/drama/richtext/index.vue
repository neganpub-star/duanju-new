<template>
  <div class="app-container">
    <el-row :gutter="16">
      <!-- 左侧：文档列表（按 doc_key 去重展示，始终显示中文文档名）-->
      <el-col :span="6">
        <el-card shadow="never">
          <template #header>
            <span>协议文档</span>
          </template>
          <el-menu :default-active="activeDocKey" @select="handleSelect">
            <el-menu-item
              v-for="doc in docKeys"
              :key="doc.docKey"
              :index="doc.docKey"
            >
              <span>{{ doc.title }}</span>
              <el-tag
                v-if="hasLang(doc.docKey, currentLang)"
                size="small"
                type="success"
                style="margin-left:8px;float:right"
              >已译</el-tag>
            </el-menu-item>
          </el-menu>
        </el-card>
      </el-col>

      <!-- 右侧：编辑区 -->
      <el-col :span="18">
        <el-card shadow="never" v-if="activeDocKey">
          <template #header>
            <div style="display:flex;align-items:center;justify-content:space-between">
              <div style="display:flex;align-items:center;gap:16px">
                <span style="font-weight:600">{{ currentDocTitle }}</span>
                <!-- 语言切换 Tab -->
                <el-radio-group v-model="currentLang" size="small" @change="onLangChange">
                  <el-radio-button label="zh-CN">简体中文</el-radio-button>
                  <el-radio-button label="zh-TW">繁體中文</el-radio-button>
                  <el-radio-button label="en">English</el-radio-button>
                </el-radio-group>
              </div>
              <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
            </div>
          </template>

          <el-alert
            v-if="isNew"
            type="info"
            :closable="false"
            show-icon
            style="margin-bottom:16px"
          >
            <template #title>
              当前语言（{{ LANG_LABELS[currentLang] }}）暂无内容，保存后将自动创建。
            </template>
          </el-alert>

          <el-form label-width="80px" v-loading="contentLoading">
            <el-form-item label="标题">
              <el-input v-model="activeForm.title" style="width:400px" :placeholder="`${LANG_LABELS[currentLang]}标题`" />
            </el-form-item>
            <el-form-item label="内容">
              <el-input
                v-model="activeForm.content"
                type="textarea"
                :rows="22"
                placeholder="请输入 HTML 内容"
                style="font-family:monospace;font-size:13px;width:100%"
              />
            </el-form-item>
          </el-form>

          <el-divider content-position="left">内容预览</el-divider>
          <div class="preview-box" v-html="activeForm.content" />
        </el-card>
        <el-empty description="请从左侧选择协议文档" v-else />
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { listDocKeys, getByDocKey, saveByDocKey } from '@/api/drama/richtext'
import { ElMessage } from 'element-plus'

const LANG_LABELS = { 'zh-CN': '简体中文', 'zh-TW': '繁體中文', 'en': 'English' }

const saving       = ref(false)
const contentLoading = ref(false)
const docKeys      = ref([])          // [{docKey, title}] 基准列表（zh-CN）
const activeDocKey = ref('')
const currentLang  = ref('zh-CN')
const activeForm   = ref({ title: '', content: '' })
const isNew        = ref(false)       // 当前语言是否尚未创建

// 已存在的语言缓存 Map: docKey -> Set<lang>
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
  // 初始化 existsMap（zh-CN 全部存在）
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
    // 判断是否已有内容（id 存在说明数据库有记录）
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
.preview-box {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 16px;
  min-height: 120px;
  line-height: 1.8;
  font-size: 14px;
  color: #333;
}
</style>
