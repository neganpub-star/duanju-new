<template>
  <div class="app-container">
    <el-row :gutter="16">
      <!-- 左侧列表 -->
      <el-col :span="6">
        <el-card header="协议列表" shadow="never">
          <el-menu :default-active="String(active.id)" @select="handleSelect">
            <el-menu-item v-for="item in list" :key="item.id" :index="String(item.id)">
              {{ item.title }}
            </el-menu-item>
          </el-menu>
        </el-card>
      </el-col>

      <!-- 右侧编辑区 -->
      <el-col :span="18">
        <el-card shadow="never" v-if="active.id">
          <template #header>
            <div style="display:flex;align-items:center;justify-content:space-between">
              <span>编辑：{{ active.title }}</span>
              <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
            </div>
          </template>

          <el-form label-width="80px">
            <el-form-item label="标题">
              <el-input v-model="active.title" style="width:300px" />
            </el-form-item>
            <el-form-item label="内容">
              <el-input
                v-model="active.content"
                type="textarea"
                :rows="20"
                placeholder="请输入 HTML 内容"
                style="font-family:monospace;font-size:13px"
              />
            </el-form-item>
          </el-form>

          <!-- 预览 -->
          <el-divider content-position="left">内容预览</el-divider>
          <div class="preview-box" v-html="active.content"></div>
        </el-card>
        <el-empty description="请从左侧选择协议" v-else />
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { listRichText, updateRichText } from '@/api/drama/richtext'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const saving = ref(false)
const list = ref([])
const active = ref({})

async function load() {
  loading.value = true
  try {
    const res = await listRichText()
    list.value = res.data || []
    if (list.value.length > 0) {
      active.value = { ...list.value[0] }
    }
  } finally {
    loading.value = false
  }
}

function handleSelect(index) {
  const item = list.value.find(i => String(i.id) === index)
  if (item) active.value = { ...item }
}

async function handleSave() {
  saving.value = true
  try {
    await updateRichText(active.value.id, {
      title: active.value.title,
      content: active.value.content,
    })
    const idx = list.value.findIndex(i => i.id === active.value.id)
    if (idx !== -1) list.value[idx] = { ...active.value }
    ElMessage.success('保存成功')
  } finally {
    saving.value = false
  }
}

onMounted(load)
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
