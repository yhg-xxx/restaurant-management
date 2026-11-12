<template>
  <div class="profile-container">
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <h2>个人信息</h2>
        </div>
      </template>

      <div class="info-display">
        <div class="info-item">
          <span class="info-label">用户ID：</span>
          <span class="info-value">{{ userForm.userId }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">用户名：</span>
          <span class="info-value">{{ userForm.username }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">姓名：</span>
          <span class="info-value">{{ userForm.fullName }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">角色：</span>
          <span class="info-value">{{ userForm.roleDisplay }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">电话：</span>
          <span class="info-value">{{ userForm.phone || '未设置' }}</span>
        </div>

        <div class="action-buttons">
          <el-button type="primary" @click="openEditDialog">编辑信息</el-button>
          <el-button type="danger" @click="openPasswordDialog">修改密码</el-button>
        </div>
      </div>
    </el-card>

    <!-- 编辑信息对话框 -->
    <el-dialog
        v-model="editDialogVisible"
        title="编辑个人信息"
        width="500px"
        @closed="resetEditForm"
    >
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="用户ID">
          <el-input v-model="editForm.userId" disabled />
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="editForm.username" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="editForm.fullName" />
        </el-form-item>
        <el-form-item label="角色">
          <el-input v-model="editForm.roleDisplay" disabled />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="editForm.phone" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="updateUserInfo">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 修改密码对话框 -->
    <el-dialog
        v-model="passwordDialogVisible"
        title="修改密码"
        width="500px"
        @closed="resetPasswordForm"
    >
      <el-form
          :model="passwordForm"
          :rules="passwordRules"
          ref="passwordFormRef"
          label-width="100px"
      >
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input
              v-model="passwordForm.oldPassword"
              type="password"
              show-password
              placeholder="请输入当前密码"
          />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
              v-model="passwordForm.newPassword"
              type="password"
              show-password
              placeholder="请输入新密码"
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
              v-model="passwordForm.confirmPassword"
              type="password"
              show-password
              placeholder="请再次输入新密码"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="passwordDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="updatePassword">确认修改</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { ElMessage } from 'element-plus';

// 用户信息展示数据
const userForm = ref({
  userId: '',
  username: '',
  fullName: '',
  role: '',
  roleDisplay: '',
  phone: ''
});

// 编辑表单数据
const editForm = ref({
  userId: '',
  username: '',
  fullName: '',
  roleDisplay: '',
  phone: ''
});

// 密码表单
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
});

// 对话框控制
const editDialogVisible = ref(false);
const passwordDialogVisible = ref(false);

// 密码验证规则
const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入旧密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少为6位', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少为6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.value.newPassword) {
          callback(new Error('两次输入的密码不一致'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ]
};

// 表单引用
const passwordFormRef = ref(null);

// 获取用户信息
const fetchUserInfo = async () => {
  try {
    const savedUser = JSON.parse(sessionStorage.getItem('user'));
    const userId = savedUser.userId;

    const response = await axios.get(`/api/users/${userId}`);
    const userData = response.data;

    userForm.value = {
      userId: userData.userId,
      username: userData.username,
      fullName: userData.fullName,
      role: userData.role,
      roleDisplay: userData.role === 'FRONT_DESK' ? '前台' : '后厨',
      phone: userData.phone || ''
    };
  } catch (error) {
    ElMessage.error('获取用户信息失败: ' + error.message);
  }
};

// 打开编辑对话框
const openEditDialog = () => {
  editForm.value = { ...userForm.value };
  editDialogVisible.value = true;
};

// 重置编辑表单
const resetEditForm = () => {
  passwordFormRef.value?.resetFields();
};

// 更新用户信息
const updateUserInfo = async () => {
  try {
    const userId = editForm.value.userId;
    const updateData = {
      username: editForm.value.username,
      fullName: editForm.value.fullName,
      phone: editForm.value.phone
    };

    await axios.put(`/api/users/${userId}`, updateData);

    // 更新展示数据和sessionStorage
    userForm.value = { ...editForm.value };

    const savedUser = JSON.parse(sessionStorage.getItem('user'));
    savedUser.username = editForm.value.username;
    savedUser.fullName = editForm.value.fullName;
    sessionStorage.setItem('user', JSON.stringify(savedUser));

    ElMessage.success('用户信息更新成功');
    editDialogVisible.value = false;
  } catch (error) {
    if (error.response && error.response.status === 400) {
      ElMessage.error('用户名已被使用');
    } else {
      ElMessage.error('更新用户信息失败: ' + error.message);
    }
  }
};

// 打开密码修改对话框
const openPasswordDialog = () => {
  passwordDialogVisible.value = true;
};

// 重置密码表单
const resetPasswordForm = () => {
  passwordForm.value = {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  };
  passwordFormRef.value?.resetFields();
};

// 更新密码
const updatePassword = async () => {
  try {
    // 验证表单
    await passwordFormRef.value.validate();

    const userId = userForm.value.userId;
    const passwordData = {
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword
    };

    await axios.put(`/api/users/${userId}/password`, passwordData);

    // 重置密码表单并关闭对话框
    resetPasswordForm();
    passwordDialogVisible.value = false;

    ElMessage.success('密码修改成功');
  } catch (error) {
    if (error.response) {
      if (error.response.status === 400) {
        ElMessage.error('旧密码不正确');
      } else if (error.response.status === 401) {
        ElMessage.error('身份验证失败');
      } else {
        ElMessage.error('修改密码失败: ' + error.response.data.message);
      }
    } else if (error.name !== 'Error') { // 排除验证错误
      ElMessage.error('修改密码失败: ' + error.message);
    }
  }
};

// 初始化
onMounted(() => {
  fetchUserInfo();
});
</script>

<style scoped>
.profile-container {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.profile-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info-display {
  padding: 10px 20px;
}

.info-item {
  display: flex;
  margin-bottom: 15px;
  font-size: 15px;
  line-height: 1.6;
}

.info-label {
  width: 100px;
  font-weight: bold;
  color: #606266;
}

.info-value {
  flex: 1;
  color: #303133;
}

.action-buttons {
  margin-top: 30px;
  display: flex;
  justify-content: center;
  gap: 20px;
}
</style>