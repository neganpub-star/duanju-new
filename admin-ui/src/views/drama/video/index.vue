<template>
  <div class="app-container video-page">
    <!-- 工具栏 -->
    <div class="toolbar">
      <el-form :model="queryParams" ref="queryRef" :inline="true" class="toolbar-form">
        <el-form-item label="标题" prop="title">
          <el-input v-model="queryParams.title" placeholder="请输入视频标题" clearable style="width:200px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="全部" clearable style="width:120px">
            <el-option label="上架" :value="1" />
            <el-option label="下架" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleQuery">搜索</el-button>
          <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
          <el-button type="primary" :icon="Plus" @click="handleAdd">新增视频</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 表格 -->
    <el-table v-loading="loading" :data="list" stripe class="video-table" :header-cell-style="headerStyle">
      <el-table-column label="ID" prop="id" width="80" align="center">
        <template #default="{ row }">
          <span class="id-badge">#{{ row.id }}</span>
        </template>
      </el-table-column>

      <el-table-column label="封面" width="100" align="center">
        <template #default="{ row }">
          <el-image
            v-if="row.cover"
            :src="row.cover"
            :preview-src-list="[row.cover]"
            preview-teleported
            class="cover-thumb"
            fit="cover"
          >
            <template #error>
              <div class="cover-empty" title="封面加载失败">无</div>
            </template>
            <template #placeholder>
              <div class="cover-empty">无</div>
            </template>
          </el-image>
          <div v-else class="cover-empty" title="未设置封面">无</div>
        </template>
      </el-table-column>

      <el-table-column label="短剧信息" min-width="220">
        <template #default="{ row }">
          <div class="drama-title">{{ row.title }}</div>
          <div class="drama-meta">
            <el-tag size="small" effect="light" :type="row.status === 1 ? 'success' : 'info'" class="status-tag">
              <el-icon class="status-icon">
                <CircleCheck v-if="row.status === 1" />
                <Hide v-else />
              </el-icon>
              <span>{{ row.status === 1 ? '上架' : '下架' }}</span>
            </el-tag>
            <el-tag v-if="row.is_recommend === 1" size="small" type="warning" effect="light" class="rec-tag">
              <el-icon class="rec-icon"><Star /></el-icon>
              <span>推荐</span>
            </el-tag>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="集数" width="110" align="center">
        <template #default="{ row }">
          <div class="ep-badge">
            <span class="ep-num">{{ row.seriesCount || 0 }}</span>
            <span class="ep-unit">集</span>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="权重" width="100" align="center">
        <template #default="{ row }">
          <span class="weigh-badge" :class="weighClass(row.weigh)">{{ row.weigh ?? 0 }}</span>
        </template>
      </el-table-column>

      <el-table-column label="创建时间" width="180" align="center">
        <template #default="{ row }">
          <div class="time-cell">
            <span class="time-abs">{{ row.createTime ? row.createTime.slice(0,10) : '—' }}</span>
            <span v-if="row.createTime" class="time-rel">{{ relativeTime(row.createTime) }}</span>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="220" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="primary" size="small" :icon="VideoPlay" @click="openEpisodes(row)">分集</el-button>
          <el-divider direction="vertical" />
          <el-button link type="primary" size="small" :icon="Edit" @click="handleEdit(row)">编辑</el-button>
          <el-divider direction="vertical" />
          <el-button link type="danger" size="small" :icon="Delete" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>

      <template #empty>
        <div class="empty-state">
          <el-icon class="empty-icon"><VideoPlay /></el-icon>
          <div class="empty-text">暂无视频</div>
          <el-button type="primary" :icon="Plus" @click="handleAdd">新增第一部短剧</el-button>
        </div>
      </template>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/编辑弹窗 -->
    <el-dialog :title="dialog.title" v-model="dialog.visible" width="620px" append-to-body>
      <el-tabs v-model="activeTab">
        <!-- 基本信息 -->
        <el-tab-pane label="基本信息" name="basic">
          <el-form ref="formRef" :model="form" :rules="rules" label-width="80px" style="margin-top:8px">
            <el-form-item label="默认标题" prop="title">
              <el-input v-model="form.title" placeholder="中文标题（必填，作为默认回退）" />
            </el-form-item>
            <el-form-item label="封面" prop="cover">
              <div class="cover-uploader">
                <el-upload
                  action="#"
                  :show-file-list="false"
                  :before-upload="beforeUpload"
                  :http-request="(opt) => handleUpload(opt.file)"
                  accept="image/*"
                >
                  <el-image v-if="form.cover" :src="form.cover" class="cover-preview" fit="cover" />
                  <div v-else class="upload-placeholder">
                    <el-icon class="upload-icon"><Plus /></el-icon>
                    <span>点击上传封面</span>
                  </div>
                </el-upload>
                <el-input v-model="form.cover" placeholder="或直接粘贴图片URL" style="margin-top:6px" />
              </div>
            </el-form-item>
            <el-form-item label="默认描述">
              <el-input v-model="form.description" type="textarea" :rows="3" placeholder="中文描述（作为默认回退）" />
            </el-form-item>
            <el-form-item label="状态">
              <el-radio-group v-model="form.status">
                <el-radio :value="1">上架</el-radio>
                <el-radio :value="0">下架</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="推荐展示">
              <el-switch v-model="form.is_recommend" :active-value="1" :inactive-value="0" active-text="推荐到发现页" />
            </el-form-item>
            <el-form-item label="排序权重">
              <el-input-number v-model="form.weigh" :min="0" :max="9999" style="width:160px" />
              <span style="margin-left:8px;color:#999;font-size:12px">数字越大排越前（推荐的排在非推荐前面）</span>
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
          <el-form label-width="80px" style="margin-top:8px">
            <el-form-item label="标题">
              <el-input
                v-model="i18nForm[lang.code].title"
                :placeholder="`${lang.label}标题`"
              />
            </el-form-item>
            <el-form-item label="描述">
              <el-input
                v-model="i18nForm[lang.code].desc"
                type="textarea"
                :rows="4"
                :placeholder="`${lang.label}描述`"
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

    <!-- 分集管理抽屉 -->
    <el-drawer
      v-model="epDrawer.visible"
      :title="`《${epDrawer.videoTitle}》— 分集管理`"
      size="1060px"
      destroy-on-close
    >
      <div class="ep-toolbar">
        <el-button type="primary" icon="Plus" @click="openEpForm()">添加分集</el-button>
        <el-button icon="Setting" @click="openBatchSet()">整部剧设置</el-button>
      </div>

      <el-table v-loading="epLoading" :data="episodes" size="small">
        <el-table-column label="集数" prop="episodeNum" width="80" align="center" sortable />
        <el-table-column label="标题" prop="title" show-overflow-tooltip sortable />
        <el-table-column label="时长" width="70" align="center">
          <template #default="{ row }">
            {{ row.duration ? Math.floor(row.duration/60) + 'min' : '—' }}
          </template>
        </el-table-column>
        <el-table-column label="免费" width="60" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.isFree ? 'success' : 'info'">{{ row.isFree ? '免费' : '付费' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="60" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status ? 'success' : 'warning'">{{ row.status ? '显示' : '隐藏' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="转码" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.transcodeStatus === 'done'" type="success" size="small">已转码</el-tag>
            <el-tag v-else-if="row.transcodeStatus === 'processing'" type="warning" size="small">转码中</el-tag>
            <el-tag v-else-if="row.transcodeStatus === 'pending'" type="info" size="small">排队中</el-tag>
            <el-tooltip v-else-if="row.transcodeStatus === 'failed'" :content="row.transcodeMsg || '转码失败'" placement="top">
              <el-tag type="danger" size="small">失败</el-tag>
            </el-tooltip>
            <span v-else style="color:#ccc">—</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300" fixed="right" align="center">
          <template #default="{ row }">
            <el-space :size="4">
              <el-button size="small" type="success" icon="VideoPlay" @click="previewEpisode(row)">预览</el-button>
              <el-button size="small" icon="Cpu" :loading="row._transcoding"
                :disabled="row.transcodeStatus === 'processing' || row.transcodeStatus === 'pending'"
                @click="handleTranscode(row)">转码</el-button>
              <el-button size="small" type="danger" icon="Delete" @click="handleDeleteEp(row)">删除</el-button>
              <el-button size="small" type="primary" icon="Edit" @click="openEpForm(row)">编辑</el-button>
            </el-space>
          </template>
        </el-table-column>
      </el-table>

      <!-- 整部剧批量设置 -->
      <el-dialog title="整部剧批量设置" v-model="batchSetVisible" width="480px" append-to-body>
        <el-alert type="info" :closable="false" style="margin-bottom:16px">
          批量修改该剧所有（或指定区间）分集的解锁方式和积分价格。
        </el-alert>
        <el-form :model="batchForm" label-width="100px">
          <el-form-item label="解锁方式">
            <el-radio-group v-model="batchForm.isFree">
              <el-radio :value="1">全部免费</el-radio>
              <el-radio :value="0">付费解锁</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item v-if="batchForm.isFree === 0" label="积分价格">
            <el-input-number v-model="batchForm.price" :min="0" :precision="0" :step="10" style="width:160px" />
            <span style="margin-left:8px;color:#999;font-size:12px">
              0 = VIP专属，&gt;0 = 可用积分单集解锁
            </span>
          </el-form-item>
          <el-form-item label="应用范围">
            <el-radio-group v-model="batchForm.rangeType">
              <el-radio value="all">全部分集</el-radio>
              <el-radio value="range">指定区间</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item v-if="batchForm.rangeType === 'range'" label="集数区间">
            <el-input-number v-model="batchForm.fromEpisode" :min="1" :precision="0" style="width:120px" placeholder="从第几集" />
            <span style="margin:0 8px;color:#999">到</span>
            <el-input-number v-model="batchForm.toEpisode" :min="1" :precision="0" style="width:120px" placeholder="到第几集" />
            <span style="margin-left:8px;color:#999;font-size:12px">集</span>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="batchSetVisible = false">取消</el-button>
          <el-button type="primary" :loading="batchSubmitting" @click="submitBatchSet">确认应用</el-button>
        </template>
      </el-dialog>

      <!-- 分集表单 -->
      <el-dialog
        :title="epForm.id ? '编辑分集' : '添加分集'"
        v-model="epFormVisible"
        width="560px"
        append-to-body
      >
        <el-tabs v-model="epActiveTab">
          <!-- 基本信息 -->
          <el-tab-pane label="基本信息" name="basic">
            <el-form ref="epFormRef" :model="epForm" :rules="epRules" label-width="80px" style="margin-top:8px">
              <el-form-item label="集数" prop="episodeNum">
                <el-input-number v-model="epForm.episodeNum" :min="1" style="width:140px" />
              </el-form-item>
              <el-form-item label="默认标题" prop="title">
                <el-input v-model="epForm.title" placeholder="如：第1集（作为默认回退）" />
              </el-form-item>
              <el-form-item label="视频URL" prop="url">
                <el-input v-model="epForm.url" placeholder="视频播放地址（mp4/m3u8）" />
              </el-form-item>
              <el-form-item label="HLS地址">
                <el-input v-model="epForm.hlsUrl" placeholder="HLS m3u8 地址（可选）" />
              </el-form-item>
              <el-form-item label="时长(秒)">
                <el-input-number v-model="epForm.duration" :min="0" style="width:140px" />
              </el-form-item>
              <el-form-item label="解锁方式">
                <el-radio-group v-model="epForm.isFree">
                  <el-radio :value="1">免费</el-radio>
                  <el-radio :value="0">付费</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item v-if="epForm.isFree === 0" label="解锁价格">
                <el-input-number v-model="epForm.price" :min="0" :precision="0" :step="10" style="width:160px" />
                <span style="margin-left:8px;color:#999;font-size:12px">
                  积分（0 = VIP专属，>0 = 可用积分单集解锁）
                </span>
              </el-form-item>
              <el-form-item label="状态">
                <el-radio-group v-model="epForm.status">
                  <el-radio :value="1">显示</el-radio>
                  <el-radio :value="0">隐藏</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-form>
          </el-tab-pane>

          <!-- 每个语言 tab -->
          <el-tab-pane
            v-for="lang in supportedLangs"
            :key="lang.code"
            :label="lang.label"
            :name="`ep_${lang.code}`"
          >
            <el-form label-width="80px" style="margin-top:8px">
              <el-form-item label="标题">
                <el-input
                  v-model="epI18nForm[lang.code]"
                  :placeholder="`${lang.label}标题，如 Episode ${epForm.episodeNum || 'N'}`"
                />
              </el-form-item>
            </el-form>
          </el-tab-pane>
        </el-tabs>

        <template #footer>
          <el-button @click="epFormVisible = false">取消</el-button>
          <el-button type="primary" @click="submitEpForm">确定</el-button>
        </template>
      </el-dialog>
    </el-drawer>

  <!-- 视频预览弹窗 -->
  <el-dialog
    v-model="previewVisible"
    :title="previewTitle"
    width="780px"
    append-to-body
    destroy-on-close
    @close="destroyPlayer"
  >
    <div class="preview-wrap">
      <!-- 清晰度/格式选择 -->
      <div v-if="previewQualities.length > 0" class="quality-bar">
        <span class="quality-label">选择格式：</span>
        <el-radio-group v-model="previewUrl" size="small" @change="switchQuality">
          <el-radio-button
            v-for="q in previewQualities"
            :key="q.url"
            :value="q.url"
          >{{ q.definition }}</el-radio-button>
        </el-radio-group>
      </div>
      <video
        ref="previewVideoRef"
        class="preview-video"
        controls
        autoplay
        playsinline
      />
      <div class="preview-url">
        <el-tag size="small" :type="previewUrlType">{{ previewUrlLabel }}</el-tag>
        <span class="url-text">{{ previewUrl }}</span>
      </div>
    </div>
  </el-dialog>
</div>
</template>

<script setup>
import { listVideo, addVideo, updateVideo, deleteVideo } from '@/api/drama/video'
import { listEpisodes, addEpisode, updateEpisode, deleteEpisode, transcodeEpisode, batchSetEpisodes } from '@/api/drama/episode'
import { uploadFile, listConfig } from '@/api/system/config'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Edit, Delete, Search, Refresh, VideoPlay, CircleCheck, Hide, Star,
} from '@element-plus/icons-vue'

const headerStyle = { background: '#f7f8fa', color: '#303133', fontWeight: 600 }

function weighClass(w) {
  const v = Number(w) || 0
  if (v >= 100) return 'weigh-high'
  if (v >= 50) return 'weigh-mid'
  return 'weigh-low'
}
function relativeTime(t) {
  if (!t) return ''
  const diff = (Date.now() - new Date(t).getTime()) / 1000
  if (diff < 60) return '刚刚'
  if (diff < 3600) return Math.floor(diff / 60) + ' 分钟前'
  if (diff < 86400) return Math.floor(diff / 3600) + ' 小时前'
  if (diff < 30 * 86400) return Math.floor(diff / 86400) + ' 天前'
  if (diff < 365 * 86400) return Math.floor(diff / (30 * 86400)) + ' 个月前'
  return Math.floor(diff / (365 * 86400)) + ' 年前'
}

const LANG_LABELS = { 'zh-CN': '简体中文', 'zh-TW': '繁體中文', en: 'English' }

// ===== 视频列表 =====
const loading = ref(false)
const list = ref([])
const total = ref(0)
const queryParams = reactive({ pageNum: 1, pageSize: 10, siteId: 1, title: '', status: '' })
const dialog = reactive({ visible: false, title: '' })
const form = ref({})
const formRef = ref()
const queryRef = ref()
const activeTab = ref('basic')
const rules = { title: [{ required: true, message: '请输入标题', trigger: 'blur' }] }

// 支持的语种
const supportedLangs = ref([{ code: 'zh-CN', label: '简体中文' }, { code: 'en', label: 'English' }])
const i18nForm = reactive({})

function parseJson(str) {
  try { return str ? JSON.parse(str) : {} } catch { return {} }
}

function initI18nForm(langs) {
  langs.forEach(l => { if (!i18nForm[l.code]) i18nForm[l.code] = { title: '', desc: '' } })
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
    const res = await listVideo(queryParams)
    list.value = res.data.rows
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function handleQuery() { queryParams.pageNum = 1; getList() }
function resetQuery() { queryRef.value?.resetFields(); handleQuery() }

function handleAdd() {
  form.value = { status: 1, siteId: 1 }
  supportedLangs.value.forEach(l => { i18nForm[l.code] = { title: '', desc: '' } })
  activeTab.value = 'basic'
  dialog.title = '新增视频'
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
  dialog.title = '编辑视频'
  dialog.visible = true
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除《${row.title}》？`, '警告', { type: 'warning' })
  await deleteVideo(row.id)
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
    titleI18n: Object.keys(titleI18n).length ? JSON.stringify(titleI18n) : null,
    descI18n:  Object.keys(descI18n).length  ? JSON.stringify(descI18n)  : null,
  }
  if (payload.id) {
    await updateVideo(payload.id, payload)
  } else {
    await addVideo(payload)
  }
  ElMessage.success('操作成功')
  dialog.visible = false
  getList()
}

function beforeUpload(file) {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isImage) ElMessage.error('只能上传图片文件')
  if (!isLt5M) ElMessage.error('图片不能超过 5MB')
  return isImage && isLt5M
}

async function handleUpload(file) {
  const res = await uploadFile(file)
  form.value.cover = res.data
  ElMessage.success('上传成功')
}

// ===== 分集管理 =====
const epDrawer = reactive({ visible: false, videoId: null, videoTitle: '' })
const epLoading = ref(false)
const episodes = ref([])
const epFormVisible = ref(false)
const epForm = ref({})
const epFormRef = ref()
const epActiveTab = ref('basic')
const epI18nForm = reactive({})
const epRules = {
  episodeNum: [{ required: true, message: '请输入集数', trigger: 'blur' }],
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  url: [{ required: true, message: '请输入视频地址', trigger: 'blur' }],
}

async function openEpisodes(row) {
  epDrawer.videoId = row.id
  epDrawer.videoTitle = row.title
  epDrawer.visible = true
  await loadEpisodes()
}

async function loadEpisodes() {
  epLoading.value = true
  try {
    const res = await listEpisodes(epDrawer.videoId)
    episodes.value = res.data || []
  } finally {
    epLoading.value = false
  }
}

function openEpForm(row) {
  if (row) {
    epForm.value = { ...row }
    const titleMap = parseJson(row.titleI18n)
    supportedLangs.value.forEach(l => { epI18nForm[l.code] = titleMap[l.code] || '' })
  } else {
    const nextNum = episodes.value.length > 0
      ? Math.max(...episodes.value.map(e => e.episodeNum)) + 1
      : 1
    epForm.value = { episodeNum: nextNum, isFree: 0, price: 0, status: 1 }
    supportedLangs.value.forEach(l => { epI18nForm[l.code] = '' })
  }
  epActiveTab.value = 'basic'
  epFormVisible.value = true
}

async function submitEpForm() {
  await epFormRef.value?.validate()
  const titleI18n = {}
  supportedLangs.value.forEach(l => { if (epI18nForm[l.code]) titleI18n[l.code] = epI18nForm[l.code] })
  const payload = {
    ...epForm.value,
    titleI18n: Object.keys(titleI18n).length ? JSON.stringify(titleI18n) : null,
  }
  if (payload.id) {
    await updateEpisode(payload.id, payload)
  } else {
    await addEpisode(epDrawer.videoId, payload)
  }
  ElMessage.success('操作成功')
  epFormVisible.value = false
  loadEpisodes()
}

// ===== 整部剧批量设置 =====
const batchSetVisible = ref(false)
const batchSubmitting = ref(false)
const batchForm = reactive({ isFree: 0, price: 50, rangeType: 'all', fromEpisode: 1, toEpisode: null })

function openBatchSet() {
  batchForm.isFree = 0
  batchForm.price = 50
  batchForm.rangeType = 'all'
  batchForm.fromEpisode = 1
  batchForm.toEpisode = episodes.value.length || null
  batchSetVisible.value = true
}

async function submitBatchSet() {
  const payload = {
    isFree: batchForm.isFree,
    price: batchForm.isFree === 0 ? batchForm.price : 0,
    fromEpisode: batchForm.rangeType === 'range' ? batchForm.fromEpisode : null,
    toEpisode: batchForm.rangeType === 'range' ? batchForm.toEpisode : null,
  }
  batchSubmitting.value = true
  try {
    await batchSetEpisodes(epDrawer.videoId, payload)
    ElMessage.success('批量设置成功')
    batchSetVisible.value = false
    loadEpisodes()
  } finally {
    batchSubmitting.value = false
  }
}

async function handleDeleteEp(row) {
  await ElMessageBox.confirm(`确认删除第 ${row.episodeNum} 集《${row.title}》？`, '警告', { type: 'warning' })
  await deleteEpisode(row.id)
  ElMessage.success('删除成功')
  loadEpisodes()
}

async function handleTranscode(row) {
  if (!row.url) { ElMessage.warning('请先设置视频地址'); return }
  row._transcoding = true
  try {
    const res = await transcodeEpisode(row.id)
    if (res.code === 200) {
      ElMessage.success('转码任务已提交，后台处理中')
      row.transcodeStatus = 'pending'
    } else {
      ElMessage.error(res.msg || '提交失败')
    }
  } finally {
    row._transcoding = false
  }
}

// ===== 视频预览 =====
const previewVisible = ref(false)
const previewTitle = ref('')
const previewUrl = ref('')
const previewQualities = ref([])   // [{definition, url}] 有多个时显示选择器
const previewVideoRef = ref(null)
let hlsInstance = null

const previewUrlType = computed(() => previewUrl.value.includes('.m3u8') ? 'warning' : 'success')
const previewUrlLabel = computed(() => previewUrl.value.includes('.m3u8') ? 'HLS/M3U8' : 'MP4')

function previewEpisode(row) {
  // 构建清晰度列表
  let qualities = []
  if (row.playInfo) {
    const infos = typeof row.playInfo === 'string' ? JSON.parse(row.playInfo) : row.playInfo
    if (Array.isArray(infos) && infos.length > 0) qualities = infos
  }
  // 没有 playInfo 时，把原始地址也纳入列表
  if (qualities.length === 0) {
    if (row.hlsUrl) qualities.push({ definition: 'HLS', url: row.hlsUrl })
    if (row.url)    qualities.push({ definition: 'MP4', url: row.url })
  }
  if (qualities.length === 0) { ElMessage.warning('该集暂无播放地址'); return }

  previewQualities.value = qualities
  // 默认播最高清晰度（列表末尾）
  const defaultUrl = qualities[qualities.length - 1].url
  previewTitle.value = `预览 — 第${row.episodeNum}集《${row.title || ''}》`
  previewUrl.value = defaultUrl
  previewVisible.value = true
  nextTick(() => initPlayer(defaultUrl))
}

function switchQuality(url) {
  destroyPlayer()
  nextTick(() => initPlayer(url))
}

async function initPlayer(url) {
  const video = previewVideoRef.value
  if (!video) return

  const isHls = url.includes('.m3u8')

  if (!isHls) {
    // MP4 直接赋值
    video.src = url
    video.load()
    return
  }

  // M3U8：Safari 原生支持，其他浏览器用 HLS.js
  if (video.canPlayType('application/vnd.apple.mpegurl')) {
    video.src = url
    video.load()
    return
  }

  // 动态加载 HLS.js
  if (!window.Hls) {
    await loadScript('https://cdn.bootcdn.net/ajax/libs/hls.js/1.5.8/hls.min.js')
  }
  if (!window.Hls.isSupported()) {
    ElMessage.error('当前浏览器不支持 HLS 播放')
    return
  }
  hlsInstance = new window.Hls()
  hlsInstance.loadSource(url)
  hlsInstance.attachMedia(video)
}

function loadScript(src) {
  return new Promise((resolve, reject) => {
    const s = document.createElement('script')
    s.src = src
    s.onload = resolve
    s.onerror = reject
    document.head.appendChild(s)
  })
}

function destroyPlayer() {
  if (hlsInstance) {
    hlsInstance.destroy()
    hlsInstance = null
  }
  const video = previewVideoRef.value
  if (video) {
    video.pause()
    video.src = ''
    video.load()
  }
}

loadSupportedLangs()
getList()
</script>

<style scoped>
.video-page { padding: 12px; }

/* 工具栏 */
.toolbar {
  padding: 10px 14px 0;
  margin-bottom: 10px;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}
.toolbar-form :deep(.el-form-item) { margin-bottom: 10px; }

/* 表格 */
.video-table {
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}
.video-table :deep(.el-table__inner-wrapper)::before { display: none; }
.video-table :deep(.cell) { padding: 6px 10px; line-height: 1.5; }
.video-table :deep(.el-table__row) td { padding: 8px 0; }
.video-table :deep(th.el-table__cell) { padding: 8px 0; }

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

/* 封面 */
.cover-thumb {
  width: 54px; height: 76px;
  border-radius: 6px;
  display: block;
  margin: 0 auto;
  border: 1px solid #f0f0f0;
}
.cover-thumb :deep(img) { object-fit: cover; }
.cover-empty {
  width: 54px; height: 76px;
  border-radius: 6px;
  background: #f4f6f9;
  border: 1px dashed #dcdfe6;
  color: #909399;
  font-size: 13px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
  user-select: none;
}

/* 短剧信息 */
.drama-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 6px;
  line-height: 1.4;
}
.drama-meta { display: flex; align-items: center; flex-wrap: wrap; gap: 6px; }
.status-tag :deep(.el-tag__content),
.status-tag {
  display: inline-flex; align-items: center; gap: 4px;
  white-space: nowrap;
}
.status-icon { font-size: 12px; }
.rec-tag :deep(.el-tag__content),
.rec-tag {
  display: inline-flex; align-items: center; gap: 4px;
  white-space: nowrap;
}
.rec-icon { font-size: 12px; }

/* 集数胶囊 */
.ep-badge {
  display: inline-flex;
  align-items: baseline;
  gap: 2px;
  padding: 3px 12px;
  border-radius: 14px;
  background: linear-gradient(135deg, #e3f2fd, #bbdefb);
  color: #1565c0;
}
.ep-num { font-size: 16px; font-weight: 700; font-variant-numeric: tabular-nums; }
.ep-unit { font-size: 11px; opacity: 0.7; }

/* 权重徽章 */
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

/* 时间 */
.time-cell { display: flex; flex-direction: column; gap: 0; line-height: 1.3; }
.time-abs { font-size: 12px; color: #606266; font-variant-numeric: tabular-nums; }
.time-rel { font-size: 11px; color: #909399; }

/* 空状态 */
.empty-state {
  display: flex; flex-direction: column; align-items: center; gap: 12px;
  padding: 40px 0;
}
.empty-icon { font-size: 56px; color: #dcdfe6; }
.empty-text { color: #909399; font-size: 14px; }

/* 上传 - 弹窗内 */
.cover-uploader .cover-preview { width: 120px; height: 160px; display: block; }
.upload-placeholder {
  width: 120px; height: 160px; border: 1px dashed #d9d9d9; border-radius: 4px;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  cursor: pointer; color: #8c939d; font-size: 12px; gap: 6px;
}
.upload-placeholder:hover { border-color: var(--el-color-primary); }
.upload-icon { font-size: 24px; }

/* 分集抽屉 */
.ep-toolbar { margin-bottom: 12px; }

/* 视频预览 */
.preview-wrap { display: flex; flex-direction: column; gap: 10px; }
.quality-bar { display: flex; align-items: center; gap: 8px; }
.quality-label { font-size: 13px; color: #606266; white-space: nowrap; }
.preview-video {
  width: 100%; max-height: 420px;
  background: #000; border-radius: 6px;
  display: block;
}
.preview-url {
  display: flex; align-items: center; gap: 8px;
  font-size: 12px; color: #666;
  background: #f5f7fa; border-radius: 4px; padding: 6px 10px;
}
.url-text { flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

/* 暗黑模式 */
html.dark .toolbar,
html.dark .video-table {
  background: #1f1f1f;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.2);
}
html.dark .drama-title { color: #e5e7eb; }
html.dark .id-badge { background: rgba(80, 72, 229, 0.15); color: #a5b4fc; }
html.dark .ep-badge { background: rgba(21, 101, 192, 0.15); color: #90caf9; }
</style>
