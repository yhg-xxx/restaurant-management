<template>
  <div class="container">
    <!-- 搜索和添加工具栏 -->
    <div class="header">
      <h1>桌台管理</h1>
      <div class="toolbar">
        <el-input
            v-model="searchKeyword"
            placeholder="输入桌台号搜索"
            style="width: 200px; margin-right: 10px;"
            clearable
            @clear="handleSearch"
            @keyup.enter="handleSearch"
        />
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button type="success" @click="openAddDialog">添加桌台</el-button>
      </div>
    </div>
    <!-- 桌台数据表格 -->
    <el-table
        :data="tables"
        stripe
        style="width: 100%"
        v-loading="loading"
    >
      <el-table-column prop="tableId" label="ID" width="80" sortable />
      <el-table-column prop="tableNumber" label="桌台号" width="120" sortable />
      <el-table-column prop="capacity" label="容量" width="100" sortable />
      <el-table-column prop="location" label="位置" />
      <el-table-column prop="status" label="状态" width="120">
        <template #default="{ row }">
          <el-tag
              :type="statusTagType(row.status)"
              size="default"
          >
            {{ formatStatus(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column fixed="right" label="操作" width="180">
        <template #default="{ row }">
          <el-button
              type="primary"
              size="small"
              @click="openEditDialog(row)"
              :icon="Edit"
          >编辑</el-button>
          <el-button
              type="danger"
              size="small"
              @click="confirmDelete(row.tableId)"
              :icon="Delete"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          v-model:currentPage="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[5, 10, 20, 50]"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
      />
    </div>

    <!-- 添加桌台对话框 -->
    <el-dialog
        v-model="addDialogVisible"
        title="添加新桌台"
        width="500px"
        @closed="resetAddForm"
    >
      <el-form
          :model="addForm"
          :rules="formRules"
          ref="addFormRef"
          label-width="100px"
      >
        <el-form-item label="桌台号" prop="tableNumber">
          <el-input v-model="addForm.tableNumber" placeholder="如：A01" />
        </el-form-item>
        <el-form-item label="容量" prop="capacity">
          <el-input-number
              v-model="addForm.capacity"
              :min="1"
              :max="20"
              controls-position="right"
          />
        </el-form-item>
        <el-form-item label="位置" prop="location">
          <el-input v-model="addForm.location" placeholder="如：靠窗位置" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="addForm.status" placeholder="选择状态">
            <el-option
                v-for="status in statusOptions"
                :key="status.value"
                :label="status.label"
                :value="status.value"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAddForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 编辑桌台对话框 -->
    <el-dialog
        v-model="editDialogVisible"
        title="编辑桌台信息"
        width="500px"
        @closed="resetEditForm"
    >
      <el-form
          :model="editForm"
          :rules="formRules"
          ref="editFormRef"
          label-width="100px"
      >
        <el-form-item label="桌台ID">
          <el-input v-model="editForm.tableId" disabled />
        </el-form-item>
        <el-form-item label="桌台号" prop="tableNumber">
          <el-input v-model="editForm.tableNumber" />
        </el-form-item>
        <el-form-item label="容量" prop="capacity">
          <el-input-number
              v-model="editForm.capacity"
              :min="1"
              :max="20"
              controls-position="right"
          />
        </el-form-item>
        <el-form-item label="位置" prop="location">
          <el-input v-model="editForm.location" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="editForm.status">
            <el-option
                v-for="status in statusOptions"
                :key="status.value"
                :label="status.label"
                :value="status.value"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEditForm">保存修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {ref, onMounted, computed} from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Edit,
  Delete,
} from '@element-plus/icons-vue'

// 状态选项
const statusOptions = [
  { value: 'available', label: '可用' },
  { value: 'occupied', label: '已占用' },
  { value: 'reserved', label: '已预订' }
]
// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchKeyword = ref('')
// 数据状态
const allTables = ref([]) // 存储所有数据
const tables = ref([])
const loading = ref(false)

// 对话框状态
const addDialogVisible = ref(false)
const editDialogVisible = ref(false)

// 表单数据
const addForm = ref({
  tableNumber: '',
  capacity: 4,
  location: '',
  status: 'available'
})

const editForm = ref({
  tableId: null,
  tableNumber: '',
  capacity: 4,
  location: '',
  status: 'available'
})

// 表单验证规则
const formRules = {
  tableNumber: [
    { required: true, message: '请输入桌台号', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  capacity: [
    { required: true, message: '请选择容量', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

// 表单引用
const addFormRef = ref(null)
const editFormRef = ref(null)

// 计算属性：排序后的表格数据（按ID升序）
const sortedTables = computed(() => {
  return [...allTables.value].sort((a, b) => a.tableId - b.tableId)
})

// 计算属性：分页后的数据
const paginatedTables = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return sortedTables.value.slice(start, end)
})
// 生命周期钩子
onMounted(() => {
  fetchTables()
})

// 获取桌台数据
const fetchTables = async () => {
  try {
    loading.value = true
    let response
    if (searchKeyword.value && searchKeyword.value.trim() !== '') {
      // 添加 encodeURIComponent 处理特殊字符
      response = await axios.get(`/api/tables/search?keyword=${encodeURIComponent(searchKeyword.value)}`)
    } else {
      response = await axios.get('/api/tables')
    }
    allTables.value = response.data
    total.value = allTables.value.length
    updateTableData()
  } catch (error) {
    ElMessage.error('获取桌台数据失败: ' + error.message)
  } finally {
    loading.value = false
  }
}
// 更新表格数据
const updateTableData = () => {
  tables.value = paginatedTables.value
}

// 搜索处理
const handleSearch = () => {
  currentPage.value = 1
  fetchTables()
}

// 每页条数改变
const handleSizeChange = (newSize) => {
  pageSize.value = newSize
  currentPage.value = 1
  updateTableData()
}

// 当前页改变
const handleCurrentChange = (newPage) => {
  currentPage.value = newPage
  updateTableData()
}


// 打开添加对话框
const openAddDialog = () => {
  addDialogVisible.value = true
}

// 打开编辑对话框
const openEditDialog = (table) => {
  editForm.value = { ...table }
  editDialogVisible.value = true
}

// 提交添加表单
const submitAddForm = async () => {
  try {
    await addFormRef.value.validate()

    await axios.post('/api/tables', addForm.value)


    ElMessage.success('桌台添加成功')
    addDialogVisible.value = false
    await fetchTables()
  } catch (error) {
    if (error.response && error.response.status === 400) {
      ElMessage.error('桌台号已存在')
    } else {
      ElMessage.error('添加桌台失败: ' + error.message)
    }
  }
}

// 提交编辑表单
const submitEditForm = async () => {
  try {
    await editFormRef.value.validate()

    await axios.put(`/api/tables/${editForm.value.tableId}`, editForm.value)


    ElMessage.success('桌台信息更新成功')
    editDialogVisible.value = false
    await fetchTables()
  } catch (error) {
    ElMessage.error('更新桌台失败: ' + error.message)
  }
}

// 确认删除
const confirmDelete = (id) => {
  ElMessageBox.confirm('确定要删除这个桌台吗？此操作不可撤销。', '警告', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning',
    center: true
  }).then(async () => {
    await deleteTable(id)

  }).catch(() => {})
}

// 删除桌台
const deleteTable = async (id) => {
  try {
    await axios.delete(`/api/tables/${id}`)

    ElMessage.success('桌台删除成功')
    await fetchTables()
  } catch (error) {
    ElMessage.error('删除桌台失败: ' + error.message)
  }
}

// 重置添加表单
const resetAddForm = () => {
  addForm.value = {
    tableNumber: '',
    capacity: 4,
    location: '',
    status: 'available'
  }
}

// 重置编辑表单
const resetEditForm = () => {
  editForm.value = {
    tableId: null,
    tableNumber: '',
    capacity: 4,
    location: '',
    status: 'available'
  }
}

// 格式化状态显示
const formatStatus = (status) => {
  const mapping = {
    'available': '可用',
    'occupied': '已占用',
    'reserved': '已预订'
  }
  return mapping[status] || status
}

// 状态标签类型
const statusTagType = (status) => {
  const mapping = {
    'available': 'success',
    'occupied': 'danger',
    'reserved': 'warning'
  }
  return mapping[status] || 'info'
}
</script>

<style scoped>
.container {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.toolbar {
  display: flex;
  align-items: center;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.header h1 {
  font-size: 24px;
  color: #303133;
  margin: 0;
}

.el-table :deep(.el-table__row) {
  transition: background-color 0.3s;
}

.el-table :deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}
</style>