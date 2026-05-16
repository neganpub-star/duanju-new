<template>
  <div class="app-container">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" icon="Plus" @click="handleAdd">新增套餐</el-button>
      </el-col>
    </el-row>

    <el-table v-loading="loading" :data="list" border>
      <el-table-column label="ID" prop="id" width="60" align="center" />
      <el-table-column label="套餐名(中文)" prop="title" width="130" />
      <el-table-column label="多语言名称" min-width="160">
        <template #default="{ row }">
          <div v-if="row.titleI18n" style="font-size:12px;color:#666">
            <div v-for="(v, k) in parseJson(row.titleI18n)" :key="k">
              <el-tag size="small" style="margin-right:4px">{{ k }}</el-tag>{{ v }}
            </div>
          </div>
          <span v-else style="color:#ccc">—</span>
        </template>
      </el-table-column>
      <el-table-column label="点数" prop="usable" width="80" align="center" />
      <el-table-column label="赠送" prop="giveUsable" width="80" align="center" />
      <el-table-column label="价格" width="90" align="right">
        <template #default="{ row }">¥{{ row.price }}</template>
      </el-table-column>
      <el-table-column label="首充价" width="90" align="right">
        <template #default="{ row }">
          <span v-if="row.firstPrice">¥{{ row.firstPrice }}</span>
          <span v-else style="color:#ccc">—</span>
        </template>
      </el-table-column>
      <el-table-column label="排序" prop="weigh" width="70" align="center" />
      <el-table-column label="状态" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === '1' ? 'success' : 'info'">{{ row.status === '1' ? '上架' : '下架' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right" align="center">
        <template #default="{ row }">
          <el-space :size="4">
            <el-button size="small" type="primary" icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" icon="Delete" @click="handleDelete(row)">删除</el-button>
          </el-space>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="dialog.title" v-model="dialog.visible" width="600px" append-to-body>
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

const rules = {
  title:  [{ required: true, message: '请输入套餐名称', trigger: 'blur' }],
  usable: [{ required: true, message: '请输入点数', trigger: 'blur' }],
  price:  [{ required: true, message: '请输入价格', trigger: 'blur' }],
}

function parseJson(str) {
  try { return str ? JSON.parse(str) : {} } catch { return {} }
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
  dialog.title = '新增积分套餐'
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
  dialog.title = '编辑积分套餐'
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
</style>
