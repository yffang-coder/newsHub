<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { User, Lock, Message, Key } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import { register, sendVerifyCode } from '@/api/auth';

const router = useRouter();
const loading = ref(false);
const codeLoading = ref(false);
const countdown = ref(0);

const form = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  code: '',
  agree: false
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

const handleRegister = async () => {
  if (!form.agree) {
    ElMessage.warning('请先同意服务条款');
    return;
  }
  if (!validateEmail(form.email)) {
    ElMessage.error('请输入有效的邮箱地址');
    return;
  }
  if (!validatePassword(form.password)) {
    ElMessage.error('密码必须包含数字和字母，且长度至少6位');
    return;
  }
  if (form.password !== form.confirmPassword) {
    ElMessage.error('两次输入的密码不一致');
    return;
  }
  if (!form.code) {
    ElMessage.error('请输入验证码');
    return;
  }
  
  loading.value = true;
  try {
    // Username is same as email, user cannot set it
    await register({
      username: form.email,
      email: form.email,
      password: form.password,
      role: 'USER', // Default role
      code: form.code
    });
    ElMessage.success('注册成功，请登录');
    router.push('/login');
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '注册失败');
  } finally {
    loading.value = false;
  }
};
</script>

<template>
  <div class="min-h-[calc(100vh-64px-300px)] flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8 bg-muted-background">
    <div class="max-w-md w-full space-y-8 bg-background p-8 rounded-xl border border-border shadow-sm">
      <div class="text-center">
        <h2 class="text-3xl font-extrabold text-primary">创建账号</h2>
        <p class="mt-2 text-sm text-muted-foreground">
          加入 NewsHub，开启您的阅读之旅
        </p>
      </div>
      
      <el-form :model="form" class="mt-8 space-y-6" @submit.prevent="handleRegister">
        <div class="space-y-4">
          <el-form-item>
            <el-input 
              v-model="form.email" 
              placeholder="邮箱地址 (将作为用户名)" 
              :prefix-icon="Message"
              size="large"
            />
          </el-form-item>
          <el-form-item>
            <div class="flex gap-2 w-full">
              <el-input 
                v-model="form.code" 
                placeholder="邮箱验证码" 
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
          <el-form-item>
            <el-input 
              v-model="form.confirmPassword" 
              type="password" 
              placeholder="确认密码" 
              :prefix-icon="Lock"
              size="large"
              show-password
            />
          </el-form-item>
        </div>

        <div class="flex items-center">
          <el-checkbox v-model="form.agree">
            我已阅读并同意 
            <a href="#" class="text-accent hover:underline">服务条款</a> 和 
            <a href="#" class="text-accent hover:underline">隐私政策</a>
          </el-checkbox>
        </div>

        <div>
          <el-button 
            type="primary" 
            class="w-full !h-11 !text-lg" 
            :loading="loading"
            @click="handleRegister"
          >
            注 册
          </el-button>
        </div>
        
        <div class="text-center text-sm text-muted-foreground">
          已有账号? 
          <RouterLink to="/login" class="font-medium text-accent hover:text-accent/80">
            立即登录
          </RouterLink>
        </div>
      </el-form>
    </div>
  </div>
</template>
