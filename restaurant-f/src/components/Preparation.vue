<template>
  <div>
    <!-- 分类方式选择 -->
    <div class="view-mode">
      <el-radio-group v-model="viewMode" @change="resetPagination">
        <el-radio-button label="order">按订单分类</el-radio-button>
        <el-radio-button label="dish">按菜品分类</el-radio-button>
        <el-radio-button label="category">按种类分类</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 按订单分类视图 -->
    <div v-if="viewMode === 'order'">
      <div v-for="order in paginatedOrders" :key="order.orderId" class="order-card compact-card">
        <h3 class="order-title">订单 #{{ order.orderId }} - 桌号: {{ order.tableNumber }}</h3>
        <p class="status-row">
          状态:
          <el-tag :type="getStatusTagType(order.status)" size="small">
            {{ formatStatus(order.status) }}
          </el-tag>
        </p>

        <div v-if="order.items.length" class="dish-grid">
          <div v-for="item in order.items" :key="item.dishId" class="dish-item">
            <img :src="item.imageUrl" alt="菜品图片" class="dish-image" />
            <div class="dish-info">
              <strong>{{ item.dishName }}</strong>
              <div>数量: {{ item.quantity }}</div>
              <div class="category-name">种类: {{ item.categoryName }}</div>
            </div>
          </div>
        </div>
        <div v-else>
          <p>无菜品信息</p>
        </div>
      </div>
    </div>

    <!-- 按菜品分类视图 - 修改此处 -->
    <div v-if="viewMode === 'dish'">
      <div v-for="dish in paginatedDishes" :key="dish.dishId" class="dish-card compact-card">
        <div class="dish-header">
          <img :src="dish.imageUrl" alt="菜品图片" class="dish-image" />
          <div class="dish-title-container">
            <div class="dish-header-row">
              <h4 class="dish-title">{{ dish.dishName }}</h4>
              <div class="dish-total-inline">总数量: {{ dish.totalQuantity }}</div>
            </div>
            <p class="category-name">种类: {{ dish.categoryName }}</p>
          </div>
        </div>

        <div class="dish-orders">
          <div v-for="order in dish.orders" :key="order.orderId" class="order-info">
            <span>订单 #{{ order.orderId }}</span>
            <span>桌号: {{ order.tableNumber }}</span>
            <span>数量: {{ order.quantity }}</span>
            <el-tag :type="getStatusTagType(order.status)" size="small">
              {{ formatStatus(order.status) }}
            </el-tag>
          </div>
        </div>
      </div>
    </div>

    <!-- 按种类分类视图 -->
    <div v-if="viewMode === 'category'">
      <div v-for="category in paginatedCategories" :key="category.name" class="category-card compact-card">
        <h4 class="category-title">{{ category.name }}</h4>

        <div class="category-dishes">
          <div v-for="dish in category.dishes" :key="dish.dishId" class="category-dish">
            <img :src="dish.imageUrl" alt="菜品图片" class="dish-image" />
            <div class="dish-title">
              <strong>{{ dish.dishName }}</strong>
              <div>总数量: {{ dish.totalQuantity }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 统一的分页控件 -->
    <div class="pagination">
      <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="totalItems"
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[5, 10, 20]"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup>
import {ref, computed, onMounted} from 'vue'
import axios from 'axios'

// 分类模式
const viewMode = ref('order') // order/dish/category

// 订单数据
const orders = ref([])

// 分页相关状态
const currentPage = ref(1)
const pageSize = ref(5)
const pageSizeOptions = [5, 10, 20]

// 计算总项目数
const totalItems = computed(() => {
  if (viewMode.value === 'order') return orders.value.length
  if (viewMode.value === 'dish') return dishesGrouped.value.length
  if (viewMode.value === 'category') return categoriesGrouped.value.length
  return 0
})

// 计算属性：当前页显示的数据
const paginatedOrders = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return orders.value.slice(start, end)
})

// 按菜品分组的数据
const dishesGrouped = computed(() => {
  const dishMap = new Map()

  orders.value.forEach(order => {
    order.items.forEach(item => {
      if (!dishMap.has(item.dishId)) {
        dishMap.set(item.dishId, {
          dishId: item.dishId,
          dishName: item.dishName,
          imageUrl: item.imageUrl,
          categoryName: item.categoryName,
          totalQuantity: 0,
          orders: []
        })
      }

      const dish = dishMap.get(item.dishId)
      dish.totalQuantity += item.quantity
      dish.orders.push({
        orderId: order.orderId,
        tableNumber: order.tableNumber,
        status: order.status,
        quantity: item.quantity
      })
    })
  })

  return Array.from(dishMap.values())
})

// 按种类分组的数据
const categoriesGrouped = computed(() => {
  const categoryMap = new Map()

  orders.value.forEach(order => {
    order.items.forEach(item => {
      const categoryName = item.categoryName

      if (!categoryMap.has(categoryName)) {
        categoryMap.set(categoryName, {
          name: categoryName,
          dishes: []
        })
      }

      const category = categoryMap.get(categoryName)
      const existingDish = category.dishes.find(d => d.dishId === item.dishId)

      if (existingDish) {
        existingDish.totalQuantity += item.quantity
      } else {
        category.dishes.push({
          dishId: item.dishId,
          dishName: item.dishName,
          imageUrl: item.imageUrl,
          totalQuantity: item.quantity
        })
      }
    })
  })

  return Array.from(categoryMap.values())
})

// 菜品分类的分页数据
const paginatedDishes = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return dishesGrouped.value.slice(start, end)
})

// 种类分类的分页数据
const paginatedCategories = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return categoriesGrouped.value.slice(start, end)
})

// 重置分页
const resetPagination = () => {
  currentPage.value = 1
}

// 每页条数改变
const handleSizeChange = (newSize) => {
  pageSize.value = newSize
  resetPagination()
}

// 当前页改变
const handleCurrentChange = (newPage) => {
  currentPage.value = newPage
}

// 状态格式化
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

// 状态标签类型
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

// 获取厨房订单数据
const fetchKitchenOrders = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/orders/kitchen/orders')
    orders.value = response.data

    // 处理可能的空项目列表
    orders.value.forEach(order => {
      if (!order.items) {
        order.items = []
      }
    })

    console.log('成功获取订单数据:', orders.value)
  } catch (error) {
    console.error('获取订单数据时出错:', error)
  }
}

// 组件挂载时获取数据
onMounted(() => {
  fetchKitchenOrders()
})
</script>

<style scoped>
.view-mode {
  margin-bottom: 15px;
  display: flex;
  justify-content: center;
}

.compact-card {
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  padding: 12px;
  margin-bottom: 12px;
  background-color: #fff;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}

.order-title {
  margin: 0 0 8px 0;
  font-size: 16px;
}

.status-row {
  margin: 0 0 10px 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.dish-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
  margin-top: 10px;
}

.dish-item {
  display: flex;
  align-items: center;
  padding: 8px;
  border: 1px solid #f0f0f0;
  border-radius: 4px;
  background-color: #fafafa;
}

.dish-image {
  width: 50px;
  height: 50px;
  object-fit: cover;
  border-radius: 4px;
  margin-right: 10px;
}

.dish-info {
  font-size: 13px;
  line-height: 1.4;
}

.dish-info strong {
  font-size: 13px;
}

.category-name {
  font-size: 12px;
  color: #666;
}

/* 按菜品分类样式 - 修改此处 */
.dish-header {
  display: flex;
  align-items: flex-start; /* 顶部对齐 */
  margin-bottom: 8px;
}

.dish-title-container {
  flex: 1;
  margin-left: 10px;
}

.dish-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 3px;
}

.dish-title {
  margin: 0;
  font-size: 14px;
  font-weight: bold;
}

.dish-total-inline {
  font-size: 14px;
  font-weight: bold;
  color: #409EFF;
}

.dish-orders {
  margin-top: 8px;
  border-top: 1px dashed #eee;
  padding-top: 8px;
}

.order-info {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  padding: 4px 0;
  border-bottom: 1px solid #f5f5f5;
}

.order-info:last-child {
  border-bottom: none;
}

/* 按种类分类样式 */
.category-title {
  margin: 0 0 8px 0;
  font-size: 15px;
}

.category-dishes {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 10px;
}

.category-dish {
  display: flex;
  align-items: center;
  padding: 8px;
  border: 1px solid #f0f0f0;
  border-radius: 4px;
  background-color: #fafafa;
}

.category-dish img {
  margin-right: 8px;
}

.pagination {
  margin-top: 20px;
  padding: 10px;
  background-color: #f5f5f5;
  border-radius: 6px;
}

/* 紧凑的字体大小 */
h3, h4 {
  font-weight: 600;
}
</style>