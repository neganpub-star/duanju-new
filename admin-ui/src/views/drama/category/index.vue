<template>
  <div class="app-container">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" icon="Plus" @click="handleAdd">新增分类</el-button>
      </el-col>
    </el-row>

    <el-table v-loading="loading" :data="list">
      <el-table-column label="ID" prop="id" width="80" />
      <el-table-column label="名称(默认)" prop="name" width="120" />
      <el-table-column label="多语言名称" min-width="200">
        <template #default="{ row }">
          <div v-if="row.nameI18n" style="font-size:12px;color:#666">
            <div v-for="(v, k) in parseJson(row.nameI18n)" :key="k">
              <el-tag size="small" style="margin-right:4px">{{ k }}</el-tag>{{ v }}
            </div>
          </div>
          <span v-else style="color:#ccc">—</span>
        </template>
      </el-table-column>
      <el-table-column label="图标" prop="image" show-overflow-tooltip />
      <el-table-column label="排序" prop="weigh" width="80" align="center" />
      <el-table-column label="状态" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 'normal' ? 'success' : 'info'">
            {{ row.status === 'normal' ? '正常' : '隐藏' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" icon="Edit" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="dialog.title" v-model="dialog.visible" width="540px" append-to-body>
      <el-tabs v-model="activeTab">
        <!-- 基本信息 -->
        <el-tab-pane label="基本信息" name="basic">
          <el-form ref="formRef" :model="form" :rules="rules" label-width="90px" style="margin-top:8px">
            <el-form-item label="默认名称" prop="name">
              <el-input v-model="form.name" placeholder="中文名称（必填，作为默认回退）" />
            </el-form-item>
            <el-form-item label="图标">
              <el-input v-model="form.image" placeholder="图标URL或class" />
            </el-form-item>
            <el-form-item label="排序">
              <el-input-number v-model="form.weigh" :min="0" />
            </el-form-item>
            <el-form-item label="状态">
              <el-radio-group v-model="form.status">
                <el-radio value="normal">正常</el-radio>
                <el-radio value="hidden">隐藏</el-radio>
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
            <el-form-item label="分类名称">
              <el-input
                v-model="i18nForm[lang.code]"
                :placeholder="`${lang.label}分类名称`"
              />
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
import { listCategory, addCategory, updateCategory, deleteCategory } from '@/api/drama/category'
import { listConfig } from '@/api/system/config'
import { ElMessage, ElMessageBox } from 'element-plus'

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

function parseJson(str) {
  try { return str ? JSON.parse(str) : {} } catch { return {} }
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
