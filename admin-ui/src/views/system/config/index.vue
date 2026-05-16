<template>
  <div class="app-container">
    <el-alert type="info" :closable="false" show-icon style="margin-bottom:16px">
      <template #title>修改后点击"保存配置"生效，无需重启服务。</template>
    </el-alert>

    <el-tabs v-model="activeTab" v-loading="loading">
      <el-tab-pane
        v-for="[group, items] in groupedEntries"
        :key="group"
        :label="GROUP_LABELS[group] || group"
        :name="group"
      >
        <!-- 通用配置：表格 + 增删 -->
        <template v-if="group === 'general'">
          <div style="margin:16px 0 12px">
            <el-button type="primary" icon="Plus" @click="openAddDialog">新增配置</el-button>
          </div>
          <el-table :data="items" size="small" border style="max-width:900px">
            <el-table-column label="键名" prop="configKey" width="240" />
            <el-table-column label="名称" prop="configName" width="140" />
            <el-table-column label="值">
              <template #default="{ row }">
                <el-input v-model="form[row.configKey]" size="small" />
              </template>
            </el-table-column>
            <el-table-column label="备注" prop="remark" width="200" show-overflow-tooltip />
            <el-table-column label="操作" width="80" align="center">
              <template #default="{ row }">
                <el-button link type="danger" @click="handleDelete(row.configKey)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </template>

        <!-- 其他配置：表单 -->
        <template v-else>
          <el-form label-width="200px" size="default" style="max-width:750px;margin-top:16px">
            <template v-for="item in items" :key="item.configKey">

              <!-- 转码提供商 -->
              <template v-if="item.configKey === 'transcode.provider'">
                <el-form-item :label="item.configName">
                  <el-select v-model="form[item.configKey]" style="width:200px">
                    <el-option label="FFmpeg 本地转码" value="ffmpeg" />
                    <el-option label="阿里云 VOD" value="aliyun" />
                  </el-select>
                </el-form-item>
                <template v-if="form['transcode.provider'] === 'ffmpeg' || !form['transcode.provider']">
                  <el-divider content-position="left"><span class="divider-label">FFmpeg 配置</span></el-divider>
                </template>
                <template v-if="form['transcode.provider'] === 'aliyun'">
                  <el-divider content-position="left"><span class="divider-label">阿里云 VOD 配置</span></el-divider>
                </template>
              </template>

              <!-- FFmpeg 配置项 -->
              <el-form-item
                v-else-if="item.configKey.startsWith('transcode.ffmpeg.')"
                v-show="form['transcode.provider'] === 'ffmpeg' || !form['transcode.provider']"
                :label="item.configName"
              >
                <el-input v-if="item.configType === 'json'" v-model="form[item.configKey]"
                  type="textarea" :rows="5" style="width:100%" spellcheck="false" />
                <el-input v-else v-model="form[item.configKey]" style="width:420px" :placeholder="item.remark" />
                <div class="config-remark">{{ item.remark }}</div>
              </el-form-item>

              <!-- 阿里云 VOD 配置项 -->
              <el-form-item
                v-else-if="item.configKey.startsWith('transcode.aliyun.')"
                v-show="form['transcode.provider'] === 'aliyun'"
                :label="item.configName"
              >
                <el-input v-if="item.configType === 'password'" v-model="form[item.configKey]"
                  type="password" show-password style="width:420px" />
                <el-input v-else v-model="form[item.configKey]" style="width:420px" :placeholder="item.remark" />
                <span v-if="item.remark" class="config-remark">{{ item.remark }}</span>
              </el-form-item>

              <!-- 存储提供商 -->
              <el-form-item v-else-if="item.configKey === 'storage.provider'" :label="item.configName">
                <el-select v-model="form[item.configKey]" style="width:200px">
                  <el-option label="阿里云 OSS" value="alioss" />
                  <el-option label="MinIO" value="minio" />
                </el-select>
              </el-form-item>

              <!-- 平台配置：微信/抖音/快手 AppID 分组 -->
              <template v-else-if="item.configKey === 'platform.wechat.appid'">
                <el-divider content-position="left"><span class="divider-label">微信小程序</span></el-divider>
                <el-form-item :label="item.configName">
                  <el-input v-model="form[item.configKey]" style="width:320px" :placeholder="item.remark" />
                </el-form-item>
              </template>
              <template v-else-if="item.configKey === 'platform.toutiao.appid'">
                <el-divider content-position="left"><span class="divider-label">抖音小程序</span></el-divider>
                <el-form-item :label="item.configName">
                  <el-input v-model="form[item.configKey]" style="width:320px" :placeholder="item.remark" />
                </el-form-item>
              </template>
              <template v-else-if="item.configKey === 'platform.kuaishou.appid'">
                <el-divider content-position="left"><span class="divider-label">快手小程序</span></el-divider>
                <el-form-item :label="item.configName">
                  <el-input v-model="form[item.configKey]" style="width:320px" :placeholder="item.remark" />
                </el-form-item>
              </template>
              <template v-else-if="item.configKey === 'platform.app.android.download-url'">
                <el-divider content-position="left"><span class="divider-label">App 下载 / 更新</span></el-divider>
                <el-form-item :label="item.configName">
                  <el-input v-model="form[item.configKey]" style="width:420px" :placeholder="item.remark" />
                </el-form-item>
              </template>

              <!-- JSON 多行文本 -->
              <el-form-item v-else-if="item.configType === 'json'" :label="item.configName">
                <el-input v-model="form[item.configKey]" type="textarea" :rows="5"
                  style="width:100%" spellcheck="false" />
                <div class="config-remark">{{ item.remark }}</div>
              </el-form-item>

              <!-- 密码类 -->
              <el-form-item v-else-if="item.configType === 'password'" :label="item.configName">
                <el-input v-model="form[item.configKey]" type="password" show-password style="width:420px" />
                <span v-if="item.remark" class="config-remark">{{ item.remark }}</span>
              </el-form-item>

              <!-- 普通文本 -->
              <el-form-item v-else :label="item.configName">
                <el-input v-model="form[item.configKey]" style="width:420px" :placeholder="item.remark" />
                <span v-if="item.remark" class="config-remark">{{ item.remark }}</span>
              </el-form-item>

            </template>
          </el-form>
        </template>
      </el-tab-pane>
    </el-tabs>

    <div class="action-bar">
      <el-button type="primary" :loading="saving" @click="handleSave">保存配置</el-button>
    </div>

    <!-- 新增配置弹窗 -->
    <el-dialog title="新增配置项" v-model="addDialogVisible" width="480px" append-to-body>
      <el-form :model="addForm" :rules="addRules" ref="addFormRef" label-width="90px">
        <el-form-item label="键名" prop="configKey">
          <el-input v-model="addForm.configKey" placeholder="如 general.xxx.yyy" />
        </el-form-item>
        <el-form-item label="名称" prop="configName">
          <el-input v-model="addForm.configName" placeholder="配置项说明" />
        </el-form-item>
        <el-form-item label="值">
          <el-input v-model="addForm.configValue" placeholder="配置值" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="addForm.remark" placeholder="用途说明（选填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="addSaving" @click="submitAdd">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { listConfig, batchUpdateConfig, addConfig, deleteConfig } from '@/api/system/config'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const saving = ref(false)
const configs = ref([])
const form = reactive({})
const activeTab = ref('')

const GROUP_LABELS = {
  general:   '通用配置',
  storage:   '存储配置',
  transcode: '转码配置',
  platform:  '平台配置',
}

// tab 排列顺序
const GROUP_ORDER = ['general', 'storage', 'transcode', 'platform']

const grouped = computed(() => {
  const map = {}
  for (const item of configs.value) {
    const g = item.configGroup || 'other'
    if (!map[g]) map[g] = []
    map[g].push(item)
  }
  return map
})

const groupedEntries = computed(() => {
  const all = Object.entries(grouped.value)
  all.sort(([a], [b]) => {
    const ai = GROUP_ORDER.indexOf(a), bi = GROUP_ORDER.indexOf(b)
    if (ai === -1 && bi === -1) return a.localeCompare(b)
    if (ai === -1) return 1
    if (bi === -1) return -1
    return ai - bi
  })
  return all
})

async function loadConfig() {
  loading.value = true
  try {
    const res = await listConfig()
    configs.value = res.data || []
    configs.value.forEach(item => { form[item.configKey] = item.configValue || '' })
    if (groupedEntries.value.length > 0 && !activeTab.value) {
      activeTab.value = groupedEntries.value[0][0]
    }
  } finally {
    loading.value = false
  }
}

async function handleSave() {
  saving.value = true
  try {
    await batchUpdateConfig({ ...form })
    ElMessage.success('配置已保存')
  } finally {
    saving.value = false
  }
}

// ===== 通用配置增删 =====
const addDialogVisible = ref(false)
const addSaving = ref(false)
const addFormRef = ref()
const addForm = reactive({ configKey: '', configName: '', configValue: '', remark: '', configGroup: 'general', configType: 'text' })
const addRules = {
  configKey:  [{ required: true, message: '请输入键名', trigger: 'blur' }],
  configName: [{ required: true, message: '请输入名称', trigger: 'blur' }],
}

function openAddDialog() {
  Object.assign(addForm, { configKey: '', configName: '', configValue: '', remark: '', configGroup: 'general', configType: 'text' })
  addDialogVisible.value = true
}

async function submitAdd() {
  await addFormRef.value?.validate()
  addSaving.value = true
  try {
    await addConfig({ ...addForm })
    ElMessage.success('新增成功')
    addDialogVisible.value = false
    await loadConfig()
    activeTab.value = 'general'
  } finally {
    addSaving.value = false
  }
}

async function handleDelete(key) {
  await ElMessageBox.confirm(`确认删除配置项 "${key}"？`, '警告', { type: 'warning' })
  await deleteConfig(key)
  ElMessage.success('删除成功')
  delete form[key]
  await loadConfig()
}

loadConfig()
</script>

<style scoped>
.divider-label { font-size: 12px; color: #909399; }
.config-remark { margin-left: 8px; color: #909399; font-size: 12px; line-height: 1.4; margin-top: 4px; }
.action-bar { text-align: center; margin-top: 24px; }
</style>
