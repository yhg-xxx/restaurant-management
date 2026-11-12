<template>
  <!-- 加载状态 -->
  <template v-if="loading" class="loading-mask">
    <el-icon class="is-loading"><Loading /></el-icon>
  </template>
  <template v-else>
    <div class="app-container">
      <!-- 侧边栏 -->
      <div class="sidebar">
        <div class="sidebar-header">
          <h2 class="welcome-message">
            欢迎 {{ user.fullName }}
            <div class="user-role">{{ roleMap[user.role] }}</div>
          </h2>
        </div>

        <!-- 前台人员菜单 -->
        <div v-if="user.role === 'FRONT_DESK'" class="menu-container">


          <router-link to="/view/tables" class="menu-item">
            <el-icon><Menu /></el-icon>
            <span class="menu-text">桌台管理</span>
          </router-link>

          <router-link to="/view/orders" class="menu-item">
            <el-icon><Document /></el-icon>
            <span class="menu-text">订单管理</span>
          </router-link>

          <router-link to="/view/dishes" class="menu-item">
            <el-icon><Food /></el-icon>
            <span class="menu-text">菜品管理</span>
          </router-link>

          <router-link to="/view/reports" class="menu-item">
            <el-icon><DataAnalysis /></el-icon>
            <span class="menu-text">销售报表</span>
          </router-link>

          <!-- 在前台人员菜单和后厨人员菜单中都添加以下菜单项 -->
          <router-link to="/view/profile" class="menu-item">
            <el-icon><User /></el-icon>
            <span class="menu-text">个人信息</span>
          </router-link>

          <!-- 退出登录 -->
          <div class="logout-item" @click="handleLogout">
            <el-icon><SwitchButton /></el-icon>
            <span class="menu-text">退出登录</span>
          </div>
        </div>

        <!-- 后厨人员菜单 -->
        <div v-else-if="user.role === 'KITCHEN'" class="menu-container">


          <router-link to="/view/orders" class="menu-item">
            <el-icon><List /></el-icon>
            <span class="menu-text">待处理订单</span>
          </router-link>

          <router-link to="/view/preparation" class="menu-item">
            <el-icon><KnifeFork /></el-icon>
            <span class="menu-text">菜品制作</span>
          </router-link>

          <router-link to="/view/dishes" class="menu-item">
            <el-icon><Food /></el-icon>
            <span class="menu-text">菜品管理</span>
          </router-link>
          <!-- 在前台人员菜单和后厨人员菜单中都添加以下菜单项 -->
          <router-link to="/view/profile" class="menu-item">
            <el-icon><User /></el-icon>
            <span class="menu-text">个人信息</span>
          </router-link>

          <!-- 退出登录 -->
          <div class="logout-item" @click="handleLogout">
            <el-icon><SwitchButton /></el-icon>
            <span class="menu-text">退出登录</span>
          </div>
        </div>
      </div>

      <div class="main-content">
        <!-- 标签页导航栏 -->
        <div class="tab-bar">
          <div class="tabs-container">
            <div
                v-for="(tab, index) in tabs"
                :key="index"
                :class="['tab-item', { 'active': tab.active }]"
                @click="switchTab(tab)"
            >
              <span class="tab-title">{{ tab.title }}</span>
              <el-icon class="close-icon" @click.stop="closeTab(tab, index)">
                <Close />
              </el-icon>
            </div>
          </div>
          <div class="tab-actions">
            <el-button size="small" @click="closeAllTabs">关闭所有</el-button>
          </div>
        </div>

        <!-- 路由视图 -->
        <div class="router-view-container">
          <router-view></router-view>
        </div>
      </div>
    </div>
  </template>
</template>

<script setup>
import {
  Loading,
  SwitchButton,
  Menu,
  Document,
  Food,
  DataAnalysis,
  List,
  KnifeFork,
  Close
} from "@element-plus/icons-vue";
import {ref, onMounted, computed, watch} from 'vue';

import { ElMessage } from "element-plus";
import { useRouter, useRoute } from 'vue-router';
const router = useRouter();
const route = useRoute();
// 角色映射
const roleMap = {
  'FRONT_DESK': '前台',
  'KITCHEN': '后厨'
};
// 标签页状态
const tabs = ref([]);
// 用户信息
const loading = ref(true);
const userData = ref(null);

// 退出登录处理
const handleLogout = () => {
  sessionStorage.removeItem('user');
  router.push('/').then(() => {
    window.location.reload(true);
  });
};
// 处理标签页打开事件
const handleTabOpened = (tabInfo) => {
  addTab(tabInfo);
};

// 添加标签页
const addTab = (tabInfo) => {
  const existingTabIndex = tabs.value.findIndex(tab => tab.path === tabInfo.path);

  if (existingTabIndex === -1) {
    tabs.value.push({
      title: tabInfo.title,
      path: tabInfo.path,
      active: true,
      meta: tabInfo.meta || {}
    });
  } else {
    switchTab(tabs.value[existingTabIndex]);
  }

  // 更新激活状态
  tabs.value.forEach(t => {
    t.active = t.path === tabInfo.path;
  });
};

// 切换标签页
const switchTab = (tab) => {
  if (route.path !== tab.path) {
    router.push(tab.path);
  }

  // 更新标签激活状态
  tabs.value.forEach(t => {
    t.active = t.path === tab.path;
  });
};

// 关闭标签页
const closeTab = (tab, index) => {
  // 如果关闭的是当前激活的标签
  if (tab.active) {
    // 尝试激活前一个标签
    if (index > 0) {
      switchTab(tabs.value[index - 1]);
    }
    // 如果关闭的是第一个标签，尝试激活后一个标签
    else if (tabs.value.length > 1) {
      switchTab(tabs.value[1]);
    }
    // 如果没有其他标签了，回到首页
    else {
      router.push('/view/orders');
    }
  }

  // 移除标签
  tabs.value.splice(index, 1);
};

const closeAllTabs = () => {
  tabs.value = []; // 直接清空所有标签页
  // 跳转到默认页面（如订单管理）
  router.push('/view/orders');
};

// 监听路由变化
watch(() => route.path, (newPath) => {
  // 当路由变化时，更新标签页状态
  tabs.value.forEach(tab => {
    tab.active = tab.path === newPath;
  });

  // 如果当前路由没有对应的标签页，自动添加
  const existingTab = tabs.value.find(tab => tab.path === newPath);
  if (!existingTab) {
    const title = route.meta?.title || newPath.split('/').pop();
    addTab({
      title: title,
      path: newPath,
      meta: route.meta || {}
    });
  }
});
// 用户信息初始化
onMounted(async () => {
  try {
    // 模拟API请求延迟
    await new Promise(resolve => setTimeout(resolve, 50));

    // 从sessionStorage获取用户数据
    const savedUser = sessionStorage.getItem('user');

    if (savedUser) {
      userData.value = JSON.parse(savedUser);
    } else {
      // 如果sessionStorage中没有用户数据，重定向到登录页
      await router.push('/login');
      return;
    }

    // 检查库存（示例）
    if (userData.value.role === 'KITCHEN') {
    }
  } catch (e) {
    console.error('加载用户信息失败:', e);
    ElMessage.error('加载用户信息失败');
    await router.push('/login');
  } finally {
    loading.value = false;
  }
});

// 计算属性包装
const user = computed(() => userData.value || {});
</script>

<style scoped>
/* 样式保持不变（使用您提供的样式） */
.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid #eee;
}

.welcome-message {
  font-size: 18px;
  margin: 0;
  line-height: 1.5;
}

.user-role {
  font-size: 14px;
  color: #909399;
  font-weight: normal;
}

.app-container {
  display: flex;
  min-height: 100vh;
  position: relative;
}

.sidebar {
  width: 240px;
  background: #fff;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.05);
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  z-index: 1000;
  overflow-y: auto;
}

.menu-item,
.logout-item {
  display: flex;
  align-items: center;
  padding: 12px 24px;
  margin: 4px 12px;
  border-radius: 6px;
  color: #606266;
  transition: all 0.2s ease;
  text-decoration: none !important;
  cursor: pointer;
}

.menu-item:hover,
.logout-item:hover {
  background: #f5f7fa !important;
  color: #409eff !important;
  transform: translateX(4px);
}

.menu-item.router-link-exact-active {
  background: #ecf5ff !important;
  color: #409eff !important;
}

.logout-item:hover {
  background: #fef0f0 !important;
  color: #f56c6c !important;
}

.menu-container {
  flex: 1;
  overflow-y: auto;
  padding-bottom: 60px;
}

.main-content {
  flex: 1;
  margin-left: 240px;
  min-height: 100vh;
  background: #f8f9fc;
  padding: 24px;
  transition: margin 0.3s;
}

@media (max-width: 768px) {
  .menu-item,
  .logout-item {
    padding: 10px 16px;
    margin: 4px 8px;
  }
}
/* 新增标签页样式 */
.tab-bar {
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  justify-content: space-between;
  padding: 0 12px;
  height: 40px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  z-index: 10;
}

.tabs-container {
  display: flex;
  overflow-x: auto;
  flex: 1;
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE 10+ */
}

.tabs-container::-webkit-scrollbar {
  display: none; /* Chrome, Safari, Opera */
}

.tab-item {
  display: flex;
  align-items: center;
  padding: 0 16px;
  height: 100%;
  font-size: 13px;
  border: 1px solid #e4e7ed;
  border-bottom: none;
  background: #f5f7fa;
  cursor: pointer;
  position: relative;
  min-width: 120px;
  max-width: 200px;
  margin-right: 4px;
  border-radius: 4px 4px 0 0;
  transition: all 0.2s;
}

.tab-item.active {
  background: #fff;
  border-color: #409eff;
  color: #409eff;
  font-weight: 500;
  border-bottom: 1px solid #fff;
  margin-bottom: -1px;
}

.tab-title {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
}

.close-icon {
  font-size: 12px;
  margin-left: 8px;
  color: #909399;
  padding: 2px;
  border-radius: 50%;
  transition: all 0.2s;
}

.close-icon:hover {
  background: #f56c6c;
  color: #fff;
}

.tab-actions {
  display: flex;
  align-items: center;
  padding: 0 8px;
}

.router-view-container {
  height: calc(100vh - 40px - 24px); /* 减去标签栏高度和内边距 */
  overflow: auto;
  padding: 12px;
  background: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}
</style>