<template>
  <div class="app-container">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" icon="Plus" @click="handleAdd">新增分类</el-button>
      </el-col>
    </el-row>

    <el-table v-loading="loading" :data="list">
      <el-table-column label="ID" prop="id" width="80" />
      <el-table-column label="名称" prop="name" />
      <el-table-column label="图标" prop="icon" />
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

    <el-dialog :title="dialog.title" v-model="dialog.visible" width="480px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="分类名称" />
        </el-form-item>
        <el-form-item label="图标" prop="icon">
          <el-input v-model="form.icon" placeholder="图标URL或class" />
        </el-form-item>
        <el-form-item label="排序" prop="weigh">
          <el-input-number v-model="form.weigh" :min="0" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio value="normal">正常</el-radio>
            <el-radio value="hidden">隐藏</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { listCategory, addCategory, updateCategory, deleteCategory } from '@/api/drama/category'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const list = ref([])
const dialog = reactive({ visible: false, title: '' })
const form = ref({})
const formRef = ref()
const rules = { name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }] }

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
  dialog.title = '新增分类'
  dialog.visible = true
}

function handleEdit(row) {
  form.value = { ...row }
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
  if (form.value.id) {
    await updateCategory(form.value.id, form.value)
  } else {
    await addCategory(form.value)
  }
  ElMessage.success('操作成功')
  dialog.visible = false
  getList()
}

getList()
</script>
