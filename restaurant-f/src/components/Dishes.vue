<template>
  <div class="container">
    <!-- 搜索和添加工具栏 -->
    <div class="header">
      <h1>菜品管理</h1>
      <div class="toolbar">
        <el-input
            v-model="searchKeyword"
            placeholder="输入菜品名称搜索"
            style="width: 200px; margin-right: 10px;"
            clearable
            @clear="handleSearch"
            @keyup.enter="handleSearch"
        />
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button type="success" @click="openAddDialog">添加菜品</el-button>
      </div>
    </div>

    <!-- 菜品数据表格 -->
    <el-table
        :data="dishes"
        stripe
        style="width: 100%"
        v-loading="loading"
        @sort-change="handleSortChange"
        :default-sort="{ prop: 'dishId', order: 'ascending' }"
    >
      <el-table-column prop="dishId" label="ID" width="80" sortable />
      <el-table-column prop="dishName" label="菜品名称" width="150" sortable />
      <el-table-column prop="categoryName" label="类别" width="120" sortable />
      <el-table-column prop="price" label="价格" width="100" sortable>
        <template #default="{ row }">
          {{ formatPrice(row.price) }}
        </template>
      </el-table-column>
      <el-table-column prop="description" label="描述" />
      <el-table-column label="图片" width="120">
        <template #default="{ row }">
          <el-image
              v-if="row.imageUrl"
              style="width: 80px; height: 80px; border-radius: 4px"
              :src="row.imageUrl"
              :preview-src-list="[row.imageUrl]"
              fit="cover"
              preview-teleported
          />
          <span v-else>无图片</span>
        </template>
      </el-table-column>
      <el-table-column prop="available" label="状态" width="100">
        <template #default="{ row }">
          <el-tag
              :type="row.available ? 'success' : 'danger'"
              size="default"
          >
            {{ row.available ? '可用' : '不可用' }}
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
              @click="confirmDelete(row.dishId)"
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
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[5, 10, 20, 50]"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
      />
    </div>

    <!-- 添加菜品对话框 -->
    <el-dialog
        v-model="addDialogVisible"
        title="添加新菜品"
        width="600px"
        @closed="resetAddForm"
    >
      <el-form
          :model="addForm"
          :rules="formRules"
          ref="addFormRef"
          label-width="100px"
      >
        <el-form-item label="菜品名称" prop="dishName">
          <el-input v-model="addForm.dishName" placeholder="请输入菜品名称" />
        </el-form-item>
        <el-form-item label="类别" prop="categoryName">
          <el-select v-model="addForm.categoryName" placeholder="选择类别">
            <el-option
                v-for="category in categories"
                :key="category.categoryId"
                :label="category.categoryName"
                :value="category.categoryName"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number
              v-model="addForm.price"
              :min="0"
              :precision="2"
              controls-position="right"
          />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
              v-model="addForm.description"
              type="textarea"
              placeholder="请输入菜品描述"
              :rows="3"
          />
        </el-form-item>
        <el-form-item label="菜品图片">
          <el-upload
              action="http://localhost:8080/api/files/upload"
              :on-success="handleAddImageSuccess"
              :before-upload="beforeImageUpload"
              :show-file-list="false"
          >
            <el-button type="primary">点击上传</el-button>
            <div v-if="addForm.imageUrl" class="image-preview">
              <el-image
                  style="width: 100px; height: 100px"
                  :src="addForm.imageUrl"
                  fit="cover"
              />
            </div>
          </el-upload>
        </el-form-item>

        <el-form-item label="状态" prop="available">
          <el-switch v-model="addForm.available" />
          <span style="margin-left: 10px;">
            {{ addForm.available ? '可用' : '不可用' }}
          </span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAddForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 编辑菜品对话框 -->
    <el-dialog
        v-model="editDialogVisible"
        title="编辑菜品信息"
        width="600px"
        @closed="resetEditForm"
    >
      <el-form
          :model="editForm"
          :rules="formRules"
          ref="editFormRef"
          label-width="100px"
      >
        <el-form-item label="菜品ID">
          <el-input v-model="editForm.dishId" disabled />
        </el-form-item>
        <el-form-item label="菜品名称" prop="dishName">
          <el-input v-model="editForm.dishName" />
        </el-form-item>
        <el-form-item label="类别" prop="categoryName">
          <el-select v-model="editForm.categoryName">
            <el-option
                v-for="category in categories"
                :key="category.categoryId"
                :label="category.categoryName"
                :value="category.categoryName"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number
              v-model="editForm.price"
              :min="0"
              :precision="2"
              controls-position="right"
          />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
              v-model="editForm.description"
              type="textarea"
              :rows="3"
          />
        </el-form-item>
        <el-form-item label="菜品图片">
          <el-upload
              action="http://localhost:8080/api/files/upload"
              :on-success="handleEditImageSuccess"
              :before-upload="beforeImageUpload"
              :show-file-list="false"
          >
            <el-button type="primary">点击上传</el-button>
            <div v-if="editForm.imageUrl" class="image-preview">
              <el-image
                  style="width: 100px; height: 100px"
                  :src="editForm.imageUrl"
                  fit="cover"
              />
            </div>
          </el-upload>
        </el-form-item>

        <el-form-item label="状态" prop="available">
          <el-switch v-model="editForm.available" />
          <span style="margin-left: 10px;">
            {{ editForm.available ? '可用' : '不可用' }}
          </span>
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
import { Edit, Delete } from '@element-plus/icons-vue'
// 添加图片上传成功处理
const handleAddImageSuccess = (response) => {
  if (response.code === 200) {
    addForm.value.imageUrl = response.url;
    ElMessage.success('图片上传成功');
  } else {
    ElMessage.error('图片上传失败: ' + response.msg);
  }
};

// 编辑图片上传成功处理
const handleEditImageSuccess = (response) => {
  if (response.code === 200) {
    editForm.value.imageUrl = response.url;
    ElMessage.success('图片上传成功');
  } else {
    ElMessage.error('图片上传失败: ' + response.msg);
  }
};

// 图片上传前校验
const beforeImageUpload = (file) => {
  const isImage = file.type.startsWith('image/');
  const isLt2M = file.size / 1024 / 1024 < 30;

  if (!isImage) {
    ElMessage.error('只能上传图片文件!');
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过2MB!');
  }

  return isImage && isLt2M;
};
// 分页相关
const currentPage = ref(1)
const pageSize = ref(5)
const total = ref(0)
const searchKeyword = ref('')

// 数据状态
const allDishes = ref([]) // 存储所有菜品数据
const filteredDishes = ref([]) // 存储过滤后的数据
const categories = ref([])
const loading = ref(false)

// 对话框状态
const addDialogVisible = ref(false)
const editDialogVisible = ref(false)

// 排序状态
const sortProp = ref('dishId') // 默认排序字段
const sortOrder = ref('ascending') // 默认排序顺序

// 表单数据
const addForm = ref({
  dishName: '',
  categoryName: '',
  price: 0,
  description: '',
  imageUrl: '',
  available: true
})

const editForm = ref({
  dishId: null,
  dishName: '',
  categoryName: '',
  price: 0,
  description: '',
  imageUrl: '',
  available: true
})

// 表单验证规则
const formRules = {
  dishName: [
    { required: true, message: '请输入菜品名称', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  categoryName: [
    { required: true, message: '请选择菜品类别', trigger: 'change' }
  ],
  price: [
    { required: true, message: '请输入菜品价格', trigger: 'blur' },
    { type: 'number', min: 0, message: '价格不能为负数', trigger: 'blur' }
  ],

}

// 表单引用
const addFormRef = ref(null)
const editFormRef = ref(null)

// 生命周期钩子
onMounted(() => {
  fetchAllDishes()
  fetchCategories()
})
const dishes = computed(() => {
  // 先复制数组避免修改原始数据
  let sortedList = [...filteredDishes.value];

  // 根据当前排序字段和顺序进行排序
  sortedList.sort((a, b) => {
    const aValue = a[sortProp.value];
    const bValue = b[sortProp.value];

    // 数字类型排序
    if (typeof aValue === 'number' && typeof bValue === 'number') {
      return sortOrder.value === 'ascending'
          ? aValue - bValue
          : bValue - aValue;
    }

    // 字符串类型排序
    if (typeof aValue === 'string' && typeof bValue === 'string') {
      return sortOrder.value === 'ascending'
          ? aValue.localeCompare(bValue)
          : bValue.localeCompare(aValue);
    }

    // 布尔值类型排序
    if (typeof aValue === 'boolean' && typeof bValue === 'boolean') {
      const aNum = aValue ? 1 : 0;
      const bNum = bValue ? 1 : 0;
      return sortOrder.value === 'ascending'
          ? aNum - bNum
          : bNum - aNum;
    }

    return 0;
  });
  // 分页处理
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return sortedList.slice(start, end)
})

// 获取所有菜品数据（不分页）
const fetchAllDishes = async () => {
  try {
    loading.value = true
    const response = await axios.get('/api/dishes')
    allDishes.value = response.data
    filteredDishes.value = [...allDishes.value]
    total.value = filteredDishes.value.length
  } catch (error) {
    ElMessage.error('获取菜品数据失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

// 获取所有类别
const fetchCategories = async () => {
  try {
    const response = await axios.get('/api/categories')
    categories.value = response.data
  } catch (error) {
    ElMessage.error('获取类别数据失败: ' + error.message)
  }
}

// 搜索处理
const handleSearch = () => {
  if (searchKeyword.value) {
    // 执行搜索
    loading.value = true
    axios.get(`/api/dishes/search?keyword=${searchKeyword.value}`)
        .then(response => {
          filteredDishes.value = response.data
          total.value = filteredDishes.value.length
          currentPage.value = 1
        })
        .catch(error => {
          ElMessage.error('搜索失败: ' + error.message)
        })
        .finally(() => {
          loading.value = false
        })
  } else {
    // 没有搜索关键词时显示所有数据
    filteredDishes.value = [...allDishes.value]
    total.value = filteredDishes.value.length
    currentPage.value = 1
  }
}
// 处理排序变化
const handleSortChange = ({ prop, order }) => {
  sortProp.value = prop;
  sortOrder.value = order || 'ascending'; // 如果没有order（点击后取消排序），则默认为ascending
}
// 每页条数改变
const handleSizeChange = (newSize) => {
  pageSize.value = newSize
  currentPage.value = 1
}

// 当前页改变
const handleCurrentChange = (newPage) => {
  currentPage.value = newPage
}

// 打开添加对话框
const openAddDialog = () => {
  addDialogVisible.value = true
}

// 打开编辑对话框
const openEditDialog = (dish) => {
  editForm.value = {
    ...dish,
    // 确保类别名称正确显示
    categoryName: dish.categoryName || ''
  }
  editDialogVisible.value = true
}

// 提交添加表单
const submitAddForm = async () => {
  try {
    await addFormRef.value.validate()

    const payload = { ...addForm.value }

    // 发送添加请求
    const response = await axios.post('/api/dishes', payload)

    if (response.status === 201) {
      ElMessage.success('菜品添加成功')
      addDialogVisible.value = false
      await fetchAllDishes()
    }
  } catch (error) {
    if (error.response && error.response.status === 400) {
      ElMessage.error('菜品名称已存在')
    } else {
      ElMessage.error('添加菜品失败: ' + error.message)
    }
  }
}

// 提交编辑表单
const submitEditForm = async () => {
  try {
    await editFormRef.value.validate()

    const payload = { ...editForm.value }

    // 发送更新请求
    const response = await axios.put(`/api/dishes/${editForm.value.dishId}`, payload)

    if (response.status === 200) {
      ElMessage.success('菜品信息更新成功')
      editDialogVisible.value = false
      await fetchAllDishes()
    }
  } catch (error) {
    ElMessage.error('更新菜品失败: ' + error.message)
  }
}

// 确认删除
const confirmDelete = (id) => {
  ElMessageBox.confirm('确定要删除这个菜品吗？此操作不可撤销。', '警告', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning',
    center: true
  }).then(async () => {
    await deleteDish(id)
  }).catch(() => {})
}

// 删除菜品
const deleteDish = async (id) => {
  try {
    const response = await axios.delete(`/api/dishes/${id}`)

    if (response.status === 204) {
      ElMessage.success('菜品删除成功')
      await fetchAllDishes()
    }
  } catch (error) {
    ElMessage.error('删除菜品失败: ' + error.message)
  }
}

// 重置添加表单
const resetAddForm = () => {
  addForm.value = {
    dishName: '',
    categoryName: '',
    price: 0,
    description: '',
    imageUrl: '',
    available: true
  }
}

// 重置编辑表单
const resetEditForm = () => {
  editForm.value = {
    dishId: null,
    dishName: '',
    categoryName: '',
    price: 0,
    description: '',
    imageUrl: '',
    available: true
  }
}

// 格式化价格显示
const formatPrice = (price) => {
  return `¥${parseFloat(price).toFixed(2)}`
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
.image-preview {
  margin-top: 10px;
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

.el-table .description-cell {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 300px;
}
</style>