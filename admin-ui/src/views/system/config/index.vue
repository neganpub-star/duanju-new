<template>
  <div class="app-container config-page">
    <!-- 顶部提示 -->
    <div class="page-hint">
      <el-icon class="hint-icon"><InfoFilled /></el-icon>
      <div class="hint-text">
        <span class="hint-title">参数配置</span>
        <span class="hint-desc">修改后点击底部"保存配置"即可生效，无需重启服务。</span>
      </div>
    </div>

    <!-- 配置卡片 -->
    <div class="config-card" v-loading="loading">
      <el-tabs v-model="activeTab" class="config-tabs">
        <el-tab-pane
          v-for="[group, items] in groupedEntries"
          :key="group"
          :name="group"
        >
          <template #label>
            <span class="tab-label">
              <el-icon><component :is="GROUP_ICONS[group] || Setting" /></el-icon>
              <span>{{ GROUP_LABELS[group] || group }}</span>
            </span>
          </template>
        <!-- 通用配置：表格 + 增删 -->
        <template v-if="group === 'general'">
          <div class="general-toolbar">
            <el-button type="primary" :icon="Plus" @click="openAddDialog">新增配置</el-button>
          </div>
          <el-table :data="items" size="small" class="config-table" :header-cell-style="headerStyle">
            <el-table-column label="键名" prop="configKey" width="260">
              <template #default="{ row }">
                <span class="config-key">{{ row.configKey }}</span>
              </template>
            </el-table-column>
            <el-table-column label="名称" prop="configName" width="160" />
            <el-table-column label="值">
              <template #default="{ row }">
                <el-input v-model="form[row.configKey]" size="small" />
              </template>
            </el-table-column>
            <el-table-column label="备注" prop="remark" min-width="180" show-overflow-tooltip>
              <template #default="{ row }">
                <span v-if="row.remark" class="config-remark-text">{{ row.remark }}</span>
                <span v-else class="text-muted">—</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="90" align="center">
              <template #default="{ row }">
                <el-button link type="danger" size="small" :icon="Delete" @click="handleDelete(row.configKey)">删除</el-button>
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
    </div>

    <!-- 浮动保存按钮 -->
    <div class="action-bar">
      <el-button size="large" type="primary" :icon="DocumentChecked" :loading="saving" @click="handleSave">保存配置</el-button>
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
import {
  Plus, Delete, Setting, InfoFilled, DocumentChecked,
  Tools, FolderOpened, VideoPlay, Cellphone,
} from '@element-plus/icons-vue'

const loading = ref(false)
const saving = ref(false)
const configs = ref([])
const form = reactive({})
const activeTab = ref('')

const headerStyle = { background: '#f7f8fa', color: '#303133', fontWeight: 600 }

const GROUP_LABELS = {
  general:   '通用配置',
  storage:   '存储配置',
  transcode: '转码配置',
  platform:  '平台配置',
}
const GROUP_ICONS = {
  general:   Tools,
  storage:   FolderOpened,
  transcode: VideoPlay,
  platform:  Cellphone,
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
.config-page {
  padding: 12px;
  padding-bottom: 80px;
}

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

/* 配置卡片 */
.config-card {
  background: #fff;
  border-radius: 10px;
  padding: 4px 16px 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  min-height: 400px;
}

/* tabs */
.config-tabs :deep(.el-tabs__header) { margin-bottom: 8px; }
.config-tabs :deep(.el-tabs__nav-wrap::after) { background-color: #f0f0f0; }
.tab-label {
  display: inline-flex; align-items: center; gap: 6px;
  font-size: 14px;
}
.tab-label .el-icon { font-size: 16px; }

/* 通用配置工具栏与表格 */
.general-toolbar { margin: 14px 0 12px; }
.config-table {
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #f0f0f0;
}
.config-table :deep(.el-table__inner-wrapper)::before { display: none; }
.config-key {
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
  font-size: 12px;
  color: #606266;
  background: #f4f6f9;
  padding: 2px 8px;
  border-radius: 6px;
}
.config-remark-text { font-size: 12px; color: #909399; }
.text-muted { color: #c0c4cc; font-size: 12px; }

/* 分组分割线 */
.divider-label { font-size: 12px; color: #909399; font-weight: 600; }

/* 表单内的备注 */
.config-remark {
  margin-left: 8px;
  color: #909399;
  font-size: 12px;
  line-height: 1.4;
  margin-top: 4px;
}

/* 浮动保存按钮 */
.action-bar {
  position: sticky;
  bottom: 0;
  margin: 24px -12px -12px;
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(8px);
  border-top: 1px solid #f0f0f0;
  text-align: center;
  z-index: 9;
}
.action-bar :deep(.el-button) {
  min-width: 200px;
}

/* 暗黑模式 */
html.dark .page-hint {
  background: linear-gradient(135deg, rgba(80, 72, 229, 0.15) 0%, rgba(64, 158, 255, 0.15) 100%);
}
html.dark .hint-title { color: #e5e7eb; }
html.dark .config-card,
html.dark .action-bar {
  background: #1f1f1f;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.2);
}
html.dark .config-key { background: #2a2a2a; color: #cfcfcf; }
</style>
