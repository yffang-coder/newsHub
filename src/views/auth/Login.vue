<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';
import { User, Lock, Message, Key } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import { login, sendVerifyCode, loginByCode, completeSignup, resetPassword } from '@/api/auth';

const router = useRouter();
const userStore = useUserStore();
const loading = ref(false);
const codeLoading = ref(false);
const countdown = ref(0);
const loginType = ref<'password' | 'code'>('password');
const showCompleteProfile = ref(false);

// Reset Password Dialog
const showResetPassword = ref(false);
const resetLoading = ref(false);
const resetForm = reactive({
  username: '',
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
});

const validateEmail = (email: string) => {
  const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return re.test(email);
};

const validatePassword = (password: string) => {
  // At least 6 chars, contains both letters and numbers
  const re = /^(?=.*[0-9])(?=.*[a-zA-Z]).{6,}$/;
  return re.test(password);
};

const handleResetPassword = async () => {
  if (!resetForm.username || !resetForm.oldPassword || !resetForm.newPassword) {
    ElMessage.warning('请填写所有必填项');
    return;
  }
  if (!validatePassword(resetForm.newPassword)) {
    ElMessage.error('密码必须包含数字和字母，且长度至少6位');
    return;
  }
  if (resetForm.newPassword !== resetForm.confirmPassword) {
    ElMessage.error('两次输入的密码不一致');
    return;
  }
  
  resetLoading.value = true;
  try {
    await resetPassword({
      username: resetForm.username,
      oldPassword: resetForm.oldPassword,
      newPassword: resetForm.newPassword
    });
    ElMessage.success('密码重置成功，请使用新密码登录');
    showResetPassword.value = false;
    resetForm.username = '';
    resetForm.oldPassword = '';
    resetForm.newPassword = '';
    resetForm.confirmPassword = '';
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '重置密码失败');
  } finally {
    resetLoading.value = false;
  }
};

const form = reactive({
  username: '',
  password: '',
  email: '',
  code: '',
  remember: false
});

const profileForm = reactive({
  username: '',
  password: '',
  confirmPassword: ''
});

const handleSendCode = async () => {
  if (!form.email) {
    ElMessage.warning('请先输入邮箱地址');
    return;
  }
  if (!validateEmail(form.email)) {
    ElMessage.error('请输入有效的邮箱地址');
    return;
  }
  
  codeLoading.value = true;
  try {
    await sendVerifyCode(form.email);
    ElMessage.success('验证码发送成功');
    countdown.value = 60;
    const timer = setInterval(() => {
      countdown.value--;
      if (countdown.value <= 0) {
        clearInterval(timer);
      }
    }, 1000);
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '验证码发送失败');
  } finally {
    codeLoading.value = false;
  }
};

const handleLogin = async () => {
  if (loginType.value === 'password') {
    if (!form.username || !form.password) {
      ElMessage.error('请输入账号和密码');
      return;
    }
  } else {
    if (!form.email || !form.code) {
      ElMessage.error('请输入邮箱和验证码');
      return;
    }
    if (!validateEmail(form.email)) {
      ElMessage.error('请输入有效的邮箱地址');
      return;
    }
  }

  loading.value = true;
  try {
    let res: any;
    if (loginType.value === 'password') {
      res = await login({
        username: form.username,
        password: form.password
      });
    } else {
      res = await loginByCode({
        email: form.email,
        code: form.code
      });
      
      // Check if we need to complete registration
      if (res.status === 'REGISTER_REQUIRED') {
        showCompleteProfile.value = true;
        profileForm.username = res.email; // Default username to email
        ElMessage.info('验证通过，请设置密码完成注册');
        loading.value = false;
        return;
      }
    }
    
    localStorage.setItem('token', res.token);
    localStorage.setItem('user', JSON.stringify({
      id: res.id,
      name: res.username,
      email: res.email,
      role: res.roles.includes('ROLE_ADMIN') ? 'admin' : 'user'
    }));
    
    ElMessage.success('登录成功');
    router.push('/');
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '登录失败');
  } finally {
    if (!showCompleteProfile.value) {
      loading.value = false;
    }
  }
};

const handleCompleteProfile = async () => {
  if (!profileForm.password || !profileForm.confirmPassword) {
    ElMessage.error('请设置密码');
    return;
  }
  if (!validatePassword(profileForm.password)) {
    ElMessage.error('密码必须包含数字和字母，且长度至少6位');
    return;
  }
  if (profileForm.password !== profileForm.confirmPassword) {
    ElMessage.error('两次输入的密码不一致');
    return;
  }
  if (!profileForm.username) {
    ElMessage.error('请设置用户名');
    return;
  }

  loading.value = true;
  try {
    const res: any = await completeSignup({
      email: form.email,
      username: profileForm.username,
      password: profileForm.password
    });

    localStorage.setItem('token', res.token);
    localStorage.setItem('user', JSON.stringify({
      id: res.id,
      name: res.username,
      email: res.email,
      role: res.roles.includes('ROLE_ADMIN') ? 'admin' : 'user'
    }));
    
    ElMessage.success('注册成功，已自动登录');
    router.push('/');
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '注册失败');
    loading.value = false;
  }
};
</script>

<template>
  <div class="min-h-[calc(100vh-64px-300px)] flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8 bg-muted-background">
    <div class="max-w-md w-full space-y-8 bg-background p-8 rounded-xl border border-border shadow-sm">
      <div class="text-center">
        <h2 class="text-3xl font-extrabold text-primary">欢迎回来</h2>
        <p class="mt-2 text-sm text-muted-foreground">
          请登录您的 NewsHub 账号
        </p>
      </div>
      
      <!-- Login Type Tabs -->
      <div v-if="!showCompleteProfile" class="flex border-b border-border mb-6">
        <button 
          class="flex-1 pb-2 text-center font-medium transition-colors"
          :class="loginType === 'password' ? 'text-primary border-b-2 border-primary' : 'text-muted-foreground hover:text-primary'"
          @click="loginType = 'password'"
        >
          密码登录
        </button>
        <button 
          class="flex-1 pb-2 text-center font-medium transition-colors"
          :class="loginType === 'code' ? 'text-primary border-b-2 border-primary' : 'text-muted-foreground hover:text-primary'"
          @click="loginType = 'code'"
        >
          验证码登录
        </button>
      </div>
      
      <div v-if="showCompleteProfile" class="text-center mb-6">
        <h3 class="text-xl font-bold text-primary">完善账号信息</h3>
        <p class="text-sm text-muted-foreground mt-2">请设置用户名和密码以完成注册</p>
      </div>

      <el-form v-if="!showCompleteProfile" :model="form" class="space-y-6" @submit.prevent="handleLogin">
        <div v-if="loginType === 'password'" class="space-y-4">
          <el-form-item>
            <el-input 
              v-model="form.username" 
              placeholder="用户名/邮箱" 
              :prefix-icon="User"
              size="large"
            />
          </el-form-item>
          <el-form-item>
            <el-input 
              v-model="form.password" 
              type="password" 
              placeholder="密码" 
              :prefix-icon="Lock"
              size="large"
              show-password
            />
          </el-form-item>
        </div>

        <div v-else class="space-y-4">
          <el-form-item>
            <el-input 
              v-model="form.email" 
              placeholder="邮箱地址" 
              :prefix-icon="Message"
              size="large"
            />
          </el-form-item>
          <el-form-item>
            <div class="flex gap-2 w-full">
              <el-input 
                v-model="form.code" 
                placeholder="验证码" 
                :prefix-icon="Key"
                size="large"
                class="flex-1"
              />
              <el-button 
                type="primary" 
                size="large" 
                :disabled="countdown > 0 || codeLoading"
                @click="handleSendCode"
                class="w-32"
              >
                {{ countdown > 0 ? `${countdown}s` : '发送验证码' }}
              </el-button>
            </div>
          </el-form-item>
        </div>

        <div class="flex items-center justify-between">
          <el-checkbox v-model="form.remember">记住我</el-checkbox>
          <div class="text-sm">
            <a href="#" @click.prevent="showResetPassword = true" class="font-medium text-accent hover:text-accent/80">忘记密码?</a>
          </div>
        </div>

        <div>
          <el-button 
            type="primary" 
            class="w-full !h-11 !text-lg" 
            :loading="loading"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </div>
        
        <div class="text-center text-sm text-muted-foreground">
          还没有账号? 
          <RouterLink to="/register" class="font-medium text-accent hover:text-accent/80">
            立即注册
          </RouterLink>
        </div>
      </el-form>
      
      <!-- Complete Profile Form -->
      <el-form v-else :model="profileForm" class="space-y-6" @submit.prevent="handleCompleteProfile">
        <div class="space-y-4">
          <el-form-item>
            <el-input 
              v-model="profileForm.username" 
              placeholder="用户名" 
              :prefix-icon="User"
              size="large"
            />
          </el-form-item>
          <el-form-item>
            <el-input 
              v-model="profileForm.password" 
              type="password" 
              placeholder="设置密码" 
              :prefix-icon="Lock"
              size="large"
              show-password
            />
          </el-form-item>
          <el-form-item>
            <el-input 
              v-model="profileForm.confirmPassword" 
              type="password" 
              placeholder="确认密码" 
              :prefix-icon="Lock"
              size="large"
              show-password
            />
          </el-form-item>
        </div>

        <div>
          <el-button 
            type="primary" 
            class="w-full !h-11 !text-lg" 
            :loading="loading"
            @click="handleCompleteProfile"
          >
            确认并注册
          </el-button>
        </div>
        
        <div class="text-center text-sm text-muted-foreground">
          <a href="#" @click.prevent="showCompleteProfile = false" class="font-medium text-accent hover:text-accent/80">
            返回登录
          </a>
        </div>
      </el-form>
    </div>

    <!-- Reset Password Dialog -->
    <el-dialog
      v-model="showResetPassword"
      title="重置密码"
      width="400px"
      align-center
      append-to-body
    >
      <el-form :model="resetForm" label-position="top" @submit.prevent="handleResetPassword">
        <el-form-item label="用户名">
          <el-input v-model="resetForm.username" placeholder="请输入您的用户名" :prefix-icon="User" />
        </el-form-item>
        <el-form-item label="旧密码">
          <el-input 
            v-model="resetForm.oldPassword" 
            type="password" 
            placeholder="请输入旧密码" 
            :prefix-icon="Lock"
            show-password 
          />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input 
            v-model="resetForm.newPassword" 
            type="password" 
            placeholder="请输入新密码" 
            :prefix-icon="Lock"
            show-password 
          />
        </el-form-item>
        <el-form-item label="确认新密码">
          <el-input 
            v-model="resetForm.confirmPassword" 
            type="password" 
            placeholder="请再次输入新密码" 
            :prefix-icon="Lock"
            show-password 
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showResetPassword = false">取消</el-button>
          <el-button type="primary" :loading="resetLoading" @click="handleResetPassword">
            确认重置
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>
