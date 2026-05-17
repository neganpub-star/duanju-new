<template>
  <div class="app-container">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" icon="Plus" @click="handleAdd">新增套餐</el-button>
      </el-col>
    </el-row>

    <el-table v-loading="loading" :data="list" stripe border>
      <el-table-column label="ID" prop="id" width="60" align="center" />
      <el-table-column label="套餐名称" min-width="200">
        <template #default="{ row }">
          <div class="pkg-name">{{ row.title }}</div>
          <div v-if="row.titleI18n" class="pkg-i18n">
            <span v-for="(v, k) in parseJson(row.titleI18n)" :key="k" class="i18n-item">
              <el-tag size="small" type="info" effect="plain">{{ k }}</el-tag> {{ v }}
            </span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="时长" width="90" align="center">
        <template #default="{ row }">
          <span class="pkg-days">{{ row.days }}</span><span style="font-size:12px;color:#909399"> 天</span>
        </template>
      </el-table-column>
      <el-table-column label="售价 / 划线价" width="150" align="center">
        <template #default="{ row }">
          <span class="pkg-price">¥{{ row.price }}</span>
          <span v-if="row.originalPrice" class="pkg-original">¥{{ row.originalPrice }}</span>
        </template>
      </el-table-column>
      <el-table-column label="排序" prop="weigh" width="70" align="center" />
      <el-table-column label="状态" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 'normal' ? 'success' : 'info'" size="small" effect="light">
            {{ row.status === 'normal' ? '上架' : '下架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="130" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
          <el-divider direction="vertical" />
          <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 编辑对话框 -->
    <el-dialog :title="dialog.title" v-model="dialog.visible" width="600px" append-to-body>
      <el-tabs v-model="activeTab">
        <!-- 基本信息 -->
        <el-tab-pane label="基本信息" name="basic">
          <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
            <el-form-item label="默认名称" prop="title">
              <el-input v-model="form.title" placeholder="简体中文名称（必填，作为默认回退）" />
            </el-form-item>
            <el-form-item label="天数" prop="days">
              <el-input-number v-model="form.days" :min="1" />
            </el-form-item>
            <el-form-item label="价格" prop="price">
              <el-input-number v-model="form.price" :min="0" :precision="2" />
            </el-form-item>
            <el-form-item label="划线价" prop="originalPrice">
              <el-input-number v-model="form.originalPrice" :min="0" :precision="2" />
            </el-form-item>
            <el-form-item label="排序">
              <el-input-number v-model="form.weigh" :min="0" />
            </el-form-item>
            <el-form-item label="状态">
              <el-radio-group v-model="form.status">
                <el-radio value="normal">上架</el-radio>
                <el-radio value="hidden">下架</el-radio>
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
import { listVip, addVip, updateVip, deleteVip } from '@/api/commerce/vip'
import { listConfig } from '@/api/system/config'
import { ElMessage, ElMessageBox } from 'element-plus'

const LANG_LABELS = { 'zh-CN': '简体中文', 'zh-TW': '繁體中文', en: 'English' }

const loading = ref(false)
const list = ref([])
const total = ref(0)
const queryParams = reactive({ pageNum: 1, pageSize: 10, siteId: 1 })
const dialog = reactive({ visible: false, title: '' })
const form = ref({})
const formRef = ref()
const activeTab = ref('basic')

// 支持的语种（从 sys_config 读取）
const supportedLangs = ref([{ code: 'zh-CN', label: '简体中文' }, { code: 'en', label: 'English' }])

// 各语言的名称 + 描述表单
const i18nForm = reactive({})

const rules = {
  title: [{ required: true, message: '请输入套餐名称', trigger: 'blur' }],
  days:  [{ required: true, message: '请输入天数', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
}

function parseJson(str) {
  try { return str ? JSON.parse(str) : {} } catch { return {} }
}

function initI18nForm(langs) {
  langs.forEach(l => {
    if (!i18nForm[l.code]) i18nForm[l.code] = { title: '', desc: '' }
  })
}

async function loadSupportedLangs() {
  try {
    const res = await listConfig()
    const configs = res.data || []
    const langsEntry = configs.find(c => c.configKey === 'i18n.supported_langs')
    if (langsEntry) {
      let codes = []
      try { codes = JSON.parse(langsEntry.configValue) } catch { codes = langsEntry.configValue.split(',').map(s => s.trim()) }
      supportedLangs.value = codes.filter(Boolean).map(code => ({ code, label: LANG_LABELS[code] || code }))
    }
  } catch { /* 使用默认值 */ }
  initI18nForm(supportedLangs.value)
}

async function getList() {
  loading.value = true
  try {
    const res = await listVip(queryParams)
    list.value = Array.isArray(res.data) ? res.data : (res.data.rows || [])
    total.value = Array.isArray(res.data) ? res.data.length : (res.data.total || 0)
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  form.value = { status: 'normal', days: 30, price: 9.9, originalPrice: 19.9, weigh: 0, siteId: 1 }
  supportedLangs.value.forEach(l => { i18nForm[l.code] = { title: '', desc: '' } })
  activeTab.value = 'basic'
  dialog.title = '新增VIP套餐'
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
  dialog.title = '编辑VIP套餐'
  dialog.visible = true
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除套餐"${row.title}"？`, '警告', { type: 'warning' })
  await deleteVip(row.id)
  ElMessage.success('删除成功')
  getList()
}

async function submitForm() {
  await formRef.value?.validate()

  // 合并多语言 JSON
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
    await updateVip(payload.id, payload)
  } else {
    await addVip(payload)
  }
  ElMessage.success('操作成功')
  dialog.visible = false
  getList()
}

loadSupportedLangs()
getList()
</script>

<style scoped>
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
.pkg-name { font-size: 13px; font-weight: 600; color: #303133; }
.pkg-i18n { display: flex; flex-wrap: wrap; gap: 6px; margin-top: 4px; }
.i18n-item { font-size: 12px; color: #909399; display: flex; align-items: center; gap: 3px; }
.pkg-days { font-size: 16px; font-weight: 700; color: #409eff; }
.pkg-price { font-size: 15px; font-weight: 700; color: #f56c6c; }
.pkg-original { font-size: 12px; color: #c0c4cc; text-decoration: line-through; margin-left: 6px; }
</style>
