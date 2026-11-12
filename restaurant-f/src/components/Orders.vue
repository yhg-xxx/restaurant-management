<template>
  <div class="container">
    <!-- 搜索和工具栏 -->
    <div class="header">
      <h1>订单管理</h1>
      <div class="toolbar">
        <el-input
            v-model="searchKeyword"
            placeholder="输入订单号/桌号/服务员搜索"
            style="width: 250px; margin-right: 10px;"
            clearable
            @clear="handleSearch"
            @keyup.enter="handleSearch"
        />
        <!-- 添加状态筛选下拉框 -->
        <el-select
            v-model="statusFilter"
            placeholder="订单状态"
            clearable
            style="width: 120px; margin-right: 10px;"
            @change="handleSearch"
        >
          <el-option label="待处理" value="pending" />
          <el-option label="准备中" value="preparing" />
          <el-option label="已上菜" value="served" />
          <el-option label="已完成" value="completed" />
        </el-select>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <!-- 添加新建订单按钮 -->
        <el-button
            type="success"
            @click="openCreateDialog"
            v-if="!isKitchen"
        >
          新建订单
        </el-button>
      </div>
    </div>

    <!-- 订单数据表格 -->
    <el-table
        :data="displayOrders"
        stripe
        style="width: 100%"
        v-loading="loading"
        @row-click="openDetailDialog"
    >
      <el-table-column prop="orderId" label="订单号" width="120" sortable />
      <el-table-column prop="tableNumber" label="桌号" width="100" sortable />
      <el-table-column prop="waiterName" label="服务员" width="120" sortable />
      <el-table-column prop="orderTime" label="下单时间" min-width="180" sortable>
        <template #default="{ row }">
          {{ formatDateTime(row.orderTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="getStatusTagType(row.status)" size="default">
            {{ formatStatus(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="totalAmount" label="总金额" width="120" sortable>
        <template #default="{ row }">
          {{ formatPrice(row.totalAmount) }}
        </template>
      </el-table-column>
      <el-table-column prop="paymentMethod" label="支付方式" width="130">
        <template #default="{ row }">
          {{ formatPaymentMethod(row.paymentMethod) }}
        </template>
      </el-table-column>
      <el-table-column fixed="right" label="操作" width="120">
        <template #default="{ row }">
          <el-button
              v-if="!isKitchen"
              type="primary"
              size="small"
              @click.stop="openEditDialog(row)"
          >修改</el-button>
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
    <el-dialog
        v-model="editDialogVisible"
        title="修改订单状态"
        width="500px"
    >
      <el-form :model="editingOrder" label-width="80px">
        <el-form-item label="当前状态" class="current-status">
          <el-tag :type="getStatusTagType(editingOrder.originalStatus)" size="default">
            {{ formatStatus(editingOrder.originalStatus) }}
          </el-tag>
        </el-form-item>

        <el-form-item label="更新为" required>
          <el-select
              v-model="editingOrder.status"
              placeholder="请选择新状态"
              :disabled="isFinalStatus(editingOrder.originalStatus)"
              class="status-select"
          >
            <!-- 状态选项 -->
            <el-option
                v-for="status in allowedStatusOptions"
                :key="status.value"
                :label="status.label"
                :value="status.value"
            />
          </el-select>
          <div class="status-tip" v-if="statusTip">
            <el-icon><InfoFilled /></el-icon>
            <span>{{ statusTip }}</span>
          </div>
        </el-form-item>

        <el-form-item label="支付方式">
          <el-select v-model="editingOrder.paymentMethod" placeholder="选择支付方式">
            <el-option label="现金" value="cash" />
            <el-option label="支付宝" value="alipay" />
            <el-option label="微信支付" value="wechat" />
            <el-option label="银行卡" value="card" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>

        <el-form-item label="备注">
          <el-input
              v-model="editingOrder.notes"
              type="textarea"
              :rows="3"
              placeholder="请输入备注"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="updateOrder">保存</el-button>
      </template>
    </el-dialog>

    <!-- 订单详情对话框 -->
    <el-dialog
        v-model="detailDialogVisible"
        :title="`订单详情 - #${currentOrder.orderId || ''}`"
        width="700px"
    >
      <div class="order-detail">
        <div class="order-info">
          <div><strong>桌号：</strong>{{ currentOrder.tableNumber }}</div>
          <div><strong>服务员：</strong>{{ currentOrder.waiterName }}</div>
          <div><strong>下单时间：</strong>{{ formatDateTime(currentOrder.orderTime) }}</div>
          <div>
            <strong>状态：</strong>
            <el-tag :type="getStatusTagType(currentOrder.status)" size="default">
              {{ formatStatus(currentOrder.status) }}
            </el-tag>
          </div>
          <div><strong>总金额：</strong>{{ formatPrice(currentOrder.totalAmount) }}</div>
          <div><strong>支付方式：</strong>{{ formatPaymentMethod(currentOrder.paymentMethod) }}</div>
          <div><strong>备注：</strong>{{ currentOrder.notes || '无' }}</div>
        </div>

        <el-divider />
        <!-- 在订单菜品表格上方添加按钮 -->
        <div style="margin-bottom: 15px; text-align: right;" v-if="!isKitchen">
        <el-button type="primary" @click="openAddItemDialog">添加菜品</el-button>
      </div>
        <h3>订单菜品</h3>
        <el-table :data="currentOrderItems" style="width: 100%">
          <el-table-column prop="dishName" label="菜品名称" width="150" />
          <el-table-column prop="quantity" label="数量" width="80" />
          <el-table-column prop="price" label="单价" width="100">
            <template #default="{ row }">
              {{ formatPrice(row.price) }}
            </template>
          </el-table-column>
          <el-table-column prop="amount" label="小计" width="100">
            <template #default="{ row }">
              {{ formatPrice(row.price * row.quantity) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180" v-if="!isKitchen">
            <template #default="{ row }">
              <el-button size="small" @click="openEditItemDialog(row.detailId)">修改</el-button>
              <el-button type="danger" size="small" @click="confirmDeleteItem(row.detailId)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <template #footer>
        <el-button type="primary" @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
    <!-- 添加菜品对话框 -->
    <el-dialog v-model="addItemDialogVisible" title="添加菜品" width="500px">
      <el-form :model="newItem" label-width="80px">
        <el-form-item label="菜品" required>
          <el-select v-model="newItem.dishId" placeholder="选择菜品" style="width: 100%">
            <el-option
                v-for="dish in dishes"
                :key="dish.dishId"
                :label="dish.dishName"
                :value="dish.dishId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="数量" required>
          <el-input-number v-model="newItem.quantity" :min="1" />
        </el-form-item>

      </el-form>
      <template #footer>
        <el-button @click="addItemDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="addOrderItem">添加</el-button>
      </template>
    </el-dialog>

    <!-- 修改菜品对话框 -->
    <el-dialog v-model="editItemDialogVisible" title="修改菜品" width="500px">
      <el-form :model="editingItem" label-width="80px">
        <el-form-item label="菜品">
          <el-input :value="editingItem.dishName" disabled />
        </el-form-item>
        <el-form-item label="数量" required>
          <el-input-number v-model="editingItem.quantity" :min="1" />
        </el-form-item>

      </el-form>
      <template #footer>
        <el-button @click="editItemDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="updateOrderItem">保存</el-button>
      </template>
    </el-dialog>
    <!-- 添加创建订单对话框 -->
    <el-dialog
        v-model="createDialogVisible"
        title="创建新订单"
        width="600px"
    >
      <el-form :model="newOrder" label-width="100px" ref="createForm">
        <el-form-item label="桌号" prop="tableId" required>
          <el-select
              v-model="newOrder.tableId"
              placeholder="请选择桌号"
              style="width: 100%"
              @change="handleTableChange"
          >
            <el-option
                v-for="table in tables"
                :key="table.tableId"
                :label="`${table.tableNumber} (${formatTableStatus(table.status)})`"
                :value="table.tableId"
                :disabled="table.status !== 'available'"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="服务员" prop="waiterName">
          <el-input
              v-model="newOrder.waiterName"
              placeholder="服务员姓名"
              disabled
          />
        </el-form-item>

        <el-form-item label="支付方式" prop="paymentMethod">
          <el-select v-model="newOrder.paymentMethod" placeholder="请选择支付方式">
            <el-option label="现金" value="cash" />
            <el-option label="支付宝" value="alipay" />
            <el-option label="微信支付" value="wechat" />
            <el-option label="银行卡" value="card" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>

        <el-form-item label="备注" prop="notes">
          <el-input
              v-model="newOrder.notes"
              type="textarea"
              :rows="3"
              placeholder="请输入订单备注"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="createOrder">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {ref, onMounted, computed, watch, nextTick} from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import {InfoFilled} from "@element-plus/icons-vue";

const statusTip = ref('')
// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)

const searchKeyword = ref('')

// 数据状态
const allOrders = ref([]) // 存储所有订单数据

const loading = ref(false)
const currentOrder = ref({})
const currentOrderItems = ref([])
const tables = ref([])
// 对话框状态
const detailDialogVisible = ref(false)
const createDialogVisible = ref(false)
// 添加新的状态
const addItemDialogVisible = ref(false)
const editItemDialogVisible = ref(false)
const dishes = ref([])
const newItem = ref({
  dishId: null,
  quantity: 1,
  notes: ''
})
const editingItem = ref({
  detailId: null,
  dishName: '',
  quantity: 1,
  notes: ''
})
const editDialogVisible = ref(false)
const editingOrder = ref({
  orderId: null,
  status: '',
  paymentMethod: '',
  notes: '',
  originalStatus: ''  // 添加原始状态字段
})
// 修改状态选项计算属性
const allowedStatusOptions = computed(() => {
  return getNextStatusOptions(editingOrder.value.originalStatus)
})

// 修改状态流转规则
const isFinalStatus = (status) => {
  return status === 'completed' || status === 'cancelled';
}
// 更新分页计算属性
const displayOrders = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  return filteredOrders.value.slice(start, end);
});
// 添加状态提示更新
const updateStatusTip = () => {
  const status = editingOrder.value.status
  const original = editingOrder.value.originalStatus

  if (!status) {
    statusTip.value = `请选择要更新到的状态`
    return
  }

  const actionMap = {
    preparing: '开始准备菜品',
    served: '标记为已上菜',
    completed: '完成订单',
    cancelled: '取消订单'
  }

  statusTip.value = `将订单从 "${formatStatus(original)}" 更新为 "${formatStatus(status)}" ${actionMap[status] ? `(${actionMap[status]})` : ''}`
}

// 监听状态选择变化
watch(() => editingOrder.value.status, () => {
  updateStatusTip()
})

// 确保打开对话框时更新提示
watch(editDialogVisible, (visible) => {
  if (visible) {
    nextTick(updateStatusTip)
  }
})
const total = computed(() => filteredOrders.value.length);
// 添加角色判断
const isKitchen = computed(() => {
  const userData = sessionStorage.getItem('user');
  if (userData) {
    try {
      const user = JSON.parse(userData);
      return user.role === 'KITCHEN';
    } catch (e) {
      return false;
    }
  }
  return false;
});
// 添加角色判断逻辑
const setDefaultFilter = () => {
  if (isKitchen.value) {
    statusFilter.value = 'pending'; // 后厨默认只显示待处理订单
  } else {
    statusFilter.value = ''; // 前台默认显示全部
  }
};

// 监听角色变化
watch(isKitchen, () => {
  setDefaultFilter();
  fetchOrders();
});

// 添加状态筛选变量
const statusFilter = ref('');
// 修改过滤逻辑
const filteredOrders = computed(() => {
  let result = [...allOrders.value];

  if (searchKeyword.value) {
  }

  // 状态筛选 - 在前端执行
  if (statusFilter.value) {
    result = result.filter(order => order.status === statusFilter.value);
  }

  return result;
});

// 修改打开编辑对话框方法
const openEditDialog = (order) => {
  // 检查是否是最终状态
  if (isFinalStatus(order.status)) {
    ElMessage.warning('已完成或已取消的订单不能修改状态')
    return
  }

  // 保存原始状态
  editingOrder.value = {
    orderId: order.orderId,
    status: '', // 初始不选择任何状态
    paymentMethod: order.paymentMethod,
    notes: order.notes || '',
    originalStatus: order.status // 保存原始状态
  }

  editDialogVisible.value = true
  updateStatusTip() // 初始化提示
}
// 状态流转规则
const getNextStatusOptions = (currentStatus) => {
  const transitions = {
    pending: [
      { value: 'preparing', label: '准备中' },
      { value: 'cancelled', label: '取消订单' }
    ],
    preparing: [
      { value: 'served', label: '已上菜' },
      { value: 'cancelled', label: '取消订单' }
    ],
    served: [
      { value: 'completed', label: '已完成' },
      { value: 'cancelled', label: '取消订单' }
    ]
  }

  return transitions[currentStatus] || []
}

// 更新订单
const updateOrder = async () => {
  try {

    await axios.put(`/api/orders/${editingOrder.value.orderId}`, {
      status: editingOrder.value.status, // 使用转换后的英文值
      paymentMethod: editingOrder.value.paymentMethod,
      notes: editingOrder.value.notes
    })

    ElMessage.success('订单更新成功')
    editDialogVisible.value = false
    await fetchOrders() // 刷新订单列表

    // 如果当前正在查看该订单的详情，也刷新详情
    if (detailDialogVisible.value && currentOrder.value.orderId === editingOrder.value.orderId) {
      const orderRes = await axios.get(`/api/orders/${editingOrder.value.orderId}`)
      currentOrder.value = orderRes.data
    }
  } catch (error) {
    ElMessage.error('更新订单失败: ' + error.message)
  }
}
// 格式化桌号状态
const formatTableStatus = (status) => {
  const statusMap = {
    'available': '可用',
    'occupied': '已占用',
    'reserved': '已预订'
  }
  return statusMap[status] || status;
}

// 处理桌号选择变化
const handleTableChange = (tableId) => {
  const selectedTable = tables.value.find(table => table.tableId === tableId);
  if (selectedTable && selectedTable.status !== 'available') {
    ElMessage.warning(`桌号 ${selectedTable.tableNumber} 当前状态为 ${formatTableStatus(selectedTable.status)}，无法选择`);

    // 重置选择并显示提示
    newOrder.value.tableId = null;
  }
}
// 获取所有菜品
const fetchDishes = async () => {
  try {
    const response = await axios.get('/api/dishes')
    dishes.value = response.data
  } catch (error) {
    ElMessage.error('获取菜品列表失败: ' + error.message)
  }
}

// 打开添加菜品对话框
const openAddItemDialog = () => {
  newItem.value = { dishId: null, quantity: 1, notes: '' }
  addItemDialogVisible.value = true
}

// 添加菜品到订单
const addOrderItem = async () => {
  try {
    // 获取菜品价格
    const dish = dishes.value.find(d => d.dishId === newItem.value.dishId)
    if (!dish) throw new Error('菜品不存在')

    const request = {
      dishId: newItem.value.dishId,
      quantity: newItem.value.quantity,
      price: dish.price,

    }

    await axios.post(`/api/orders/${currentOrder.value.orderId}/items`, request)
    ElMessage.success('菜品添加成功')
    addItemDialogVisible.value = false
    await fetchOrders()
    await fetchOrderDetails(currentOrder.value.orderId);
    const orderRes = await axios.get(`/api/orders/${currentOrder.value.orderId}`);
    currentOrder.value = orderRes.data;
  } catch (error) {
    ElMessage.error('添加菜品失败: ' + error.message)
  }
}

const openEditItemDialog = async (detailId) => {
  try {
    // 通过detailId获取要编辑的菜品
    const response = await axios.get(`/api/orders/items/${detailId}`)
    editingItem.value = {
      detailId: detailId,
      dishName: response.data.dishName,
      quantity: response.data.quantity,
      price: response.data.price
    }
    editItemDialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取菜品详情失败: ' + error.message)
  }
}

// 更新菜品
const updateOrderItem = async () => {
  try {
    const request = {
      quantity: editingItem.value.quantity,
      notes: editingItem.value.notes
    }

    await axios.put(`/api/orders/items/${editingItem.value.detailId}`, request)
    ElMessage.success('菜品更新成功')
    editItemDialogVisible.value = false
    await fetchOrders()
    await fetchOrderDetails(currentOrder.value.orderId);
    const orderRes = await axios.get(`/api/orders/${currentOrder.value.orderId}`);
    currentOrder.value = orderRes.data;
  } catch (error) {
    ElMessage.error('更新菜品失败: ' + error.message)
  }
}

// 确认删除菜品
const confirmDeleteItem = (detailId) => {
  ElMessageBox.confirm('确定要删除这个菜品吗？', '警告', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await deleteOrderItem(detailId)
  }).catch(() => {})
}

// 删除菜品
const deleteOrderItem = async (detailId) => {
  try {
    await axios.delete(`/api/orders/items/${detailId}`)
    ElMessage.success('菜品删除成功')
    await fetchOrders()
    await fetchOrderDetails(currentOrder.value.orderId);
    const orderRes = await axios.get(`/api/orders/${currentOrder.value.orderId}`);
    currentOrder.value = orderRes.data;
  } catch (error) {
    ElMessage.error('删除菜品失败: ' + error.message)
  }
}
// 获取所有订单数据
const fetchOrders = async () => {
  try {
    loading.value = true;

    let response;
    if (searchKeyword.value) {
      // 使用搜索端点
      response = await axios.get(`/api/orders/search?keyword=${searchKeyword.value}`);
    } else {
      // 使用普通端点
      response = await axios.get('/api/orders');
    }

    allOrders.value = response.data;
    allOrders.value.sort((a, b) => a.orderId - b.orderId);

    // 更新总记录数
    total.value = allOrders.value.length;

    // 应用分页
    applyPagination();
  } catch (error) {
    ElMessage.error('获取订单数据失败: ' + error.message);
  } finally {
    loading.value = false;
  }
}

// 应用分页逻辑
const applyPagination = () => {
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;

  // 确保不超过数组边界
  const totalItems = filteredOrders.value.length;
  if (start >= totalItems) {
    currentPage.value = Math.max(1, Math.ceil(totalItems / pageSize.value));
    return;
  }

  displayOrders.value = filteredOrders.value.slice(start, end);
}


// 修改获取订单详情的方法
const fetchOrderDetails = async (orderId) => {
  try {
    const response = await axios.get(`/api/orders/${orderId}/items`)
    // 确保返回的数据包含detailId
    currentOrderItems.value = response.data.map(item => ({
      ...item,
      detailId: item.detailId // 确保这个字段存在
    }))
  } catch (error) {
    ElMessage.error('获取订单详情失败: ' + error.message)
  }
}

// 搜索处理
const handleSearch = () => {
  currentPage.value = 1
  fetchOrders()
}

// 每页条数改变
const handleSizeChange = (newSize) => {
  pageSize.value = newSize
  currentPage.value = 1 // 重置到第一页
  fetchOrders();
}
// 当前页改变
const handleCurrentChange = (newPage) => {
  currentPage.value = newPage
  fetchOrders()
}

// 新订单表单
const newOrder = ref({
  tableNumber: '',
  waiterName: '',
  paymentMethod: '',
  notes: ''
})
const fetchTables = async () => {
  try {
    const response = await axios.get('/api/tables')
    tables.value = response.data
  } catch (error) {
    ElMessage.error('获取桌号列表失败: ' + error.message)
  }
}
// 打开创建订单对话框
const openCreateDialog = () => {
  // 从sessionStorage获取用户信息
  const userData = sessionStorage.getItem('user')
  if (userData) {
    try {
      const user = JSON.parse(userData)
      newOrder.value.waiterName = user.fullName || user.username || ''
    } catch (e) {
      console.error('解析用户信息失败', e)
      newOrder.value.waiterName = ''
    }
  }

  // 重置其他表单字段
  newOrder.value.tableId = null
  newOrder.value.paymentMethod = ''
  newOrder.value.notes = ''

  createDialogVisible.value = true
}

// 创建订单
const createOrder = async () => {
  try {
    const response = await axios.post('/api/orders', newOrder.value)
    ElMessage.success('订单创建成功')
    createDialogVisible.value = false
    // 刷新订单列表
    await fetchOrders()
  } catch (error) {
    ElMessage.error('创建订单失败: ' + error.message)
  }
}

// 打开详情对话框方法
const openDetailDialog = async (order) => {
  currentOrder.value = order
  await fetchOrderDetails(order.orderId)
  detailDialogVisible.value = true
}


// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 格式化价格
const formatPrice = (price) => {
  return `¥${parseFloat(price || 0).toFixed(2)}`
}

// 格式化状态
const formatStatus = (status) => {
  const statusMap = {
    'pending': '待处理',
    'preparing': '准备中',
    'served': '已上菜',
    'completed': '已完成',
    'cancelled': '已取消'
  }
  return statusMap[status] || status
}

// 获取状态标签类型
const getStatusTagType = (status) => {
  const typeMap = {
    'pending': 'warning',
    'preparing': 'primary',
    'served': 'success',
    'completed': 'info',
    'cancelled': 'danger'
  }
  return typeMap[status] || 'info'
}

// 格式化支付方式
const formatPaymentMethod = (method) => {
  const methodMap = {
    'cash': '现金',
    'alipay': '支付宝',
    'wechat': '微信支付',
    'card': '银行卡',
    'other': '其他'
  }
  return method ? (methodMap[method] || method) : '未支付'
}

// 生命周期钩子
onMounted(() => {
  setDefaultFilter(); // 设置默认筛选状态
  fetchOrders()
  fetchTables()
  fetchDishes()
})
</script>

<style scoped>
/* 添加样式 */
.current-status {
  margin-bottom: 20px;
}

.status-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #666;
  display: flex;
  align-items: center;

  .el-icon {
    margin-right: 5px;
    color: #409EFF;
  }
}

.status-select {
  width: 100%;
}
/* 精准控制输入框文本的显示 */
.status-select :deep(.el-input__inner) {
  color: transparent !important;
  text-indent: 45px; /* 根据您的实际标签宽度调整 */
}

.status-select :deep(.el-input__prefix) {
  left: 15px;  /* 精确对齐标签位置 */
  pointer-events: none;
  color: #606266;
}

/* 确保文本不换行 */
.status-text {
  display: inline-block;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
/* 添加行点击样式 */
.el-table :deep(.el-table__row) {
  cursor: pointer;
  transition: background-color 0.3s;
}

.el-table :deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}
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
  gap: 10px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.order-detail .order-info {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-bottom: 20px;
}

.order-detail .order-info div {
  line-height: 2;
}

.header h1 {
  font-size: 24px;
  color: #303133;
  margin: 0;
}

/* 表格样式优化 */
.el-table {
  table-layout: fixed;
}

/* 列宽调整 */
:deep(.el-table__header) .el-table__cell {
  padding: 12px 0;
}

/* 表格内容自动换行 */
:deep(.el-table .cell) {
  white-space: normal;
  word-break: break-word;
}

/* 操作列固定宽度 */
:deep(.el-table__fixed-right) {
  right: 0;
  box-shadow: -5px 0 10px -5px rgba(0,0,0,0.1);
}

/* 表格行高度调整 */
:deep(.el-table__row) td {
  padding: 12px 0;
}

/* 表格整体样式优化 */
.el-table {
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  border-radius: 8px;
  overflow: hidden;
}
</style>