<template>
  <div class="app-container">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" icon="Plus" @click="handleAdd">新增套餐</el-button>
      </el-col>
    </el-row>

    <el-table v-loading="loading" :data="list" border>
      <el-table-column label="ID" prop="id" width="60" align="center" />
      <el-table-column label="套餐名" prop="title" width="150" />
      <el-table-column label="繁体名" prop="titleZhTw" width="150" show-overflow-tooltip />
      <el-table-column label="英文名" prop="titleEn" width="150" show-overflow-tooltip />
      <el-table-column label="点数" prop="usable" width="90" align="center" />
      <el-table-column label="赠送点数" prop="giveUsable" width="90" align="center" />
      <el-table-column label="价格" width="90" align="right">
        <template #default="{ row }">¥{{ row.price }}</template>
      </el-table-column>
      <el-table-column label="首充价" width="90" align="right">
        <template #default="{ row }">
          <span v-if="row.firstPrice">¥{{ row.firstPrice }}</span>
          <span v-else style="color:#999">-</span>
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

    <el-dialog :title="dialog.title" v-model="dialog.visible" width="560px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="套餐名(中文)" prop="title">
          <el-input v-model="form.title" placeholder="如：100点数" />
        </el-form-item>
        <el-form-item label="套餐名(繁体)">
          <el-input v-model="form.titleZhTw" placeholder="如：100點數" />
        </el-form-item>
        <el-form-item label="套餐名(英文)">
          <el-input v-model="form.titleEn" placeholder="如：100 Points" />
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
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { listUsable, addUsable, updateUsable, deleteUsable } from '@/api/commerce/usable'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const queryParams = reactive({ pageNum: 1, pageSize: 20, siteId: 1 })
const dialog = reactive({ visible: false, title: '' })
const form = ref({})
const formRef = ref()
const rules = {
  title: [{ required: true, message: '请输入套餐名', trigger: 'blur' }],
  usable: [{ required: true, message: '请输入点数', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }]
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
  dialog.title = '新增积分套餐'
  dialog.visible = true
}

function handleEdit(row) {
  form.value = { ...row }
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
  if (form.value.id) {
    await updateUsable(form.value.id, form.value)
  } else {
    await addUsable(form.value)
  }
  ElMessage.success('操作成功')
  dialog.visible = false
  getList()
}

getList()
</script>
