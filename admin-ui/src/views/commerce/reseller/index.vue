<template>
  <div class="app-container">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" icon="Plus" @click="handleAdd">新增套餐</el-button>
      </el-col>
    </el-row>

    <el-table v-loading="loading" :data="list" stripe border>
      <el-table-column label="ID" prop="id" width="70" align="center" />
      <el-table-column label="套餐名 / 等级" min-width="180">
        <template #default="{ row }">
          <div style="font-weight:600;color:#303133;font-size:13px">{{ row.name }}</div>
          <div v-if="row.nameI18n" class="i18n-tags">
            <span v-for="(v, k) in parseJson(row.nameI18n)" :key="k" class="i18n-item">
              <el-tag size="small" type="info" effect="plain">{{ k }}</el-tag> {{ v }}
            </span>
          </div>
          <el-tag size="small" type="primary" effect="plain" style="margin-top:4px">Lv{{ row.level }} 分销商</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="有效天数" prop="expire" width="90" align="center">
        <template #default="{ row }">
          <span style="font-size:15px;font-weight:700;color:#409eff">{{ row.expire }}</span>
          <span style="font-size:12px;color:#909399"> 天</span>
        </template>
      </el-table-column>
      <el-table-column label="价格" width="90" align="center">
        <template #default="{ row }">
          <span style="font-size:15px;font-weight:700;color:#f56c6c">¥{{ row.price }}</span>
        </template>
      </el-table-column>
      <el-table-column label="直接佣金" prop="direct" width="100" align="center">
        <template #default="{ row }">
          <el-tag type="success" effect="plain" size="small">{{ row.direct }}%</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="间接佣金" prop="indirect" width="100" align="center">
        <template #default="{ row }">
          <el-tag type="warning" effect="plain" size="small">{{ row.indirect }}%</el-tag>
        </template>
      </el-table-column>
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

    <el-dialog :title="dialog.title" v-model="dialog.visible" width="520px" append-to-body>
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
// 每个语言存 { name, content }
const i18nForm = reactive({})

const rules = { name: [{ required: true, message: '请输入套餐名', trigger: 'blur' }] }

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
.i18n-tags { display: flex; flex-wrap: wrap; gap: 6px; margin-top: 4px; }
.i18n-item { font-size: 12px; color: #909399; display: flex; align-items: center; gap: 3px; }
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
</style>
