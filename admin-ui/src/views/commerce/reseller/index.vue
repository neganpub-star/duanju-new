<template>
  <div class="app-container">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" icon="Plus" @click="handleAdd">新增套餐</el-button>
      </el-col>
    </el-row>

    <el-table v-loading="loading" :data="list" stripe border>
      <el-table-column label="ID" prop="id" width="70" align="center" />
      <el-table-column label="套餐名 / 等级" min-width="150">
        <template #default="{ row }">
          <div style="font-weight:600;color:#303133;font-size:13px">{{ row.name }}</div>
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

    <el-dialog :title="dialog.title" v-model="dialog.visible" width="500px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="套餐名" prop="name">
          <el-input v-model="form.name" />
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
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { listReseller, addReseller, updateReseller, deleteReseller } from '@/api/commerce/reseller'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const queryParams = reactive({ pageNum: 1, pageSize: 10, siteId: 1 })
const dialog = reactive({ visible: false, title: '' })
const form = ref({})
const formRef = ref()
const rules = { name: [{ required: true, message: '请输入套餐名', trigger: 'blur' }] }

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
  dialog.title = '新增分销套餐'
  dialog.visible = true
}

function handleEdit(row) {
  form.value = { ...row }
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
  if (form.value.id) {
    await updateReseller(form.value.id, form.value)
  } else {
    await addReseller(form.value)
  }
  ElMessage.success('操作成功')
  dialog.visible = false
  getList()
}

getList()
</script>
