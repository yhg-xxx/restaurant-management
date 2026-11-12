<template>
  <div class="login-container">
    <!-- 左侧轮播图区域 -->
    <div class="carousel-container">
      <el-carousel height="100vh" :interval="4000" arrow="always">
        <el-carousel-item v-for="(item, index) in carouselItems" :key="index">
          <div class="carousel-item" :style="{ backgroundImage: `url(${item.image})` }">
            <div class="image-overlay"></div>
            <div class="carousel-content">
              <h2>{{ item.title }}</h2>
              <p>{{ item.description }}</p>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </div>

    <!-- 右侧登录表单区域 -->
    <div class="login-card">
      <div class="logo-area">
        <div class="restaurant-icon">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M3 22H21" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            <path d="M5 9V22" stroke="currentColor" stroke-width="2"/>
            <path d="M19 9V22" stroke="currentColor" stroke-width="2"/>
            <path d="M5 7C5 3.68629 7.68629 1 11 1C14.3137 1 17 3.68629 17 7V9H5V7Z" stroke="currentColor" stroke-width="2"/>
            <path d="M2 9H22" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </div>
        <h1>餐厅管理系统</h1>
      </div>

      <el-form
          ref="formRef"
          :model="loginForm"
          :rules="rules"
          class="login-form"
      >
        <el-form-item prop="username">
          <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              prefix-icon="User"
              size="large"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              show-password
              prefix-icon="Lock"
              size="large"
              @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-button
            type="primary"
            size="large"
            class="login-btn"
            :loading="loading"
            @click="handleLogin"
        >
          登 录
        </el-button>
      </el-form>


    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';
import { ElMessage } from 'element-plus';

const router = useRouter();

// 轮播图数据
const carouselItems = ref([
  {
    image: 'https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=1920&q=80',
    title: '欢迎使用餐厅管理系统',
    description: '高效管理您的餐厅运营'
  },
  {
    image: 'https://images.unsplash.com/photo-1552566626-52f8b828add9?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=1920&q=80',
    title: '智能点餐系统',
    description: '提升顾客用餐体验'
  },
  {
    image: 'https://images.unsplash.com/photo-1555396273-367ea4eb4db5?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=1920&q=80',
    title: '实时数据分析',
    description: '掌握餐厅经营状况'
  }
]);

// 表单数据模型
const loginForm = ref({
  username: '',
  password: ''
});

// 表单引用
const formRef = ref();

// 登录加载状态
const loading = ref(false);

// 表单验证规则
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少为6位', trigger: 'blur' }
  ]
};

// 登录方法
const handleLogin = async () => {
  // 表单验证
  try {
    await formRef.value.validate();
  } catch (e) {
    return;
  }

  loading.value = true;
  try {
    // 发送登录请求
    const response = await axios.post('/api/auth/login', null, {
      params: {
        username: loginForm.value.username,
        password: loginForm.value.password
      }
    });

    // 登录成功处理
    if (response.data && response.data.userId) {
      // 存储用户信息到本地
      sessionStorage.setItem('user', JSON.stringify({
        ...response.data,
      }));
      ElMessage.success('登录成功！');

      await router.push('/view');
    }
  } catch (error) {
    // 错误处理
    let errorMsg = '登录失败，请重试';
    if (error.response) {
      if (error.response.status === 401) {
        errorMsg = '用户名或密码错误';
      } else if (error.response.data && error.response.data.error) {
        errorMsg = error.response.data.error;
      }
    }
    ElMessage.error(errorMsg);
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.login-container {
  display: flex;
  min-height: 100vh;
  background: #f5f7fa;
}

.carousel-container {
  flex: 1;
  position: relative;
  overflow: hidden;
  /* 添加圆角 */
  border-radius: 6px 0 0 6px;
}

.carousel-item {
  height: 100%;
  background-size: cover;
  background-position: center;
  position: relative;
  /* 添加圆角 */
  border-radius: 6px;
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.4);
  /* 添加圆角 */
  border-radius: 6px;
}

.carousel-content {
  position: absolute;
  bottom: 20%;
  left: 10%;
  color: white;
  max-width: 500px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
}

.carousel-content h2 {
  font-size: 2.5rem;
  margin-bottom: 1rem;
}

.carousel-content p {
  font-size: 1.2rem;
  line-height: 1.6;
}

.login-card {
  width: 420px;
  /* 减少上下内边距使卡片向上移动 */
  padding: 30px 35px;
  background: white;
  display: flex;
  flex-direction: column;
  justify-content: center;
  /* 添加圆角匹配左侧 */
  border-radius: 0 6px 6px 0;
}

.logo-area {
  text-align: center;
  margin-bottom: 30px;
}

.logo-area h1 {
  color: #2c3e50;
  font-size: 24px;
  margin-top: 15px;
}

.restaurant-icon {
  display: inline-flex;
  justify-content: center;
  align-items: center;
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #6a11cb 0%, #2575fc 100%);
  border-radius: 50%;
  margin-bottom: 15px;
}

.restaurant-icon svg {
  width: 48px;
  height: 48px;
  color: white;
}

.login-form {
  margin-top: 20px;
}

.login-btn {
  width: 100%;
  margin-top: 10px;
  font-size: 16px;
  letter-spacing: 2px;
  height: 48px;
}

.footer-tip {
  margin-top: 20px;
  text-align: center;
  font-size: 12px;
  color: #909399;
}

/* 响应式调整 */
@media (max-width: 992px) {
  .carousel-container {
    display: none;
  }

  .login-container {
    justify-content: center;
    align-items: center;
    background: linear-gradient(135deg, #6a11cb 0%, #2575fc 100%);
  }

  .login-card {
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.2);
    border-radius: 12px;
    /* 移动端保持原有内边距 */
    padding: 40px 35px;
  }
}
</style>