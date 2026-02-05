<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Menu as MenuIcon, Monitor } from '@element-plus/icons-vue'

const router = useRouter();
const searchQuery = ref('');
const currentUser = ref<any>(null);

const handleSearch = () => {
  if (searchQuery.value.trim()) {
    router.push({ name: 'home', query: { q: searchQuery.value } });
  } else {
    router.push({ name: 'home' });
  }
};

const checkUser = () => {
  const userStr = localStorage.getItem('user');
  if (userStr) {
    currentUser.value = JSON.parse(userStr);
  } else {
    currentUser.value = null;
  }
};

const handleLogout = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('user');
  currentUser.value = null;
  router.push('/login');
};

let intervalId: any;

onMounted(() => {
  checkUser();
  // Simple polling for auth state changes in this simplified implementation
  // In a real app, use Pinia or an Event Bus
  intervalId = setInterval(checkUser, 1000);
});

onUnmounted(() => {
  if (intervalId) clearInterval(intervalId);
});
</script>

<template>
  <header class="border-b border-border bg-background/80 backdrop-blur-md sticky top-0 z-50">
    <div class="container-width h-16 flex items-center justify-between">
      <!-- Logo -->
      <div class="flex items-center gap-2">
        <RouterLink to="/" class="text-2xl font-bold tracking-tight text-primary no-underline">
          News<span class="text-accent">Hub</span>
        </RouterLink>
      </div>

      <!-- Desktop Navigation -->
      <nav class="hidden md:flex items-center gap-8 text-sm font-medium text-muted-foreground">
        <RouterLink to="/" class="hover:text-primary transition-colors no-underline">
          首页
        </RouterLink>
        <RouterLink to="/category/2" class="hover:text-primary transition-colors no-underline">
          国际
        </RouterLink>
        <RouterLink to="/category/4" class="hover:text-primary transition-colors no-underline">
          体育
        </RouterLink>
        <RouterLink to="/category/3" class="hover:text-primary transition-colors no-underline">
          科技
        </RouterLink>
        <RouterLink to="/category/5" class="hover:text-primary transition-colors no-underline">
          财经
        </RouterLink>
        <RouterLink to="/category/6" class="hover:text-primary transition-colors no-underline">
          文化
        </RouterLink>
      </nav>

      <!-- Actions -->
      <div class="flex items-center gap-4">
        <!-- Search Bar -->
        <div class="hidden md:flex items-center mr-2">
           <el-input
             v-model="searchQuery"
             placeholder="搜索..."
             class="w-48 transition-all focus:w-64"
             @keyup.enter="handleSearch"
           >
             <template #suffix>
               <el-icon class="cursor-pointer" @click="handleSearch"><Search /></el-icon>
             </template>
           </el-input>
        </div>

        <template v-if="currentUser">
          <el-dropdown>
            <span class="flex items-center gap-2 cursor-pointer hover:opacity-80">
              <el-avatar :size="32" class="bg-secondary text-primary">{{ currentUser.name[0] }}</el-avatar>
              <span class="hidden md:inline text-sm font-medium text-primary">{{ currentUser.name }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item v-if="currentUser.role === 'admin'" @click="router.push('/admin')">
                  <el-icon><Monitor /></el-icon>后台管理
                </el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        
        <template v-else>
          <div class="hidden md:flex items-center gap-2">
            <RouterLink to="/login">
              <el-button link>登录</el-button>
            </RouterLink>
            <RouterLink to="/register">
              <el-button type="primary" round size="small">注册</el-button>
            </RouterLink>
          </div>
        </template>

        <el-button circle text class="md:hidden !text-muted-foreground hover:!text-primary">
          <el-icon :size="20"><MenuIcon /></el-icon>
        </el-button>
      </div>
    </div>
  </header>
</template>

<style scoped>
/* Scoped styles if needed, but Tailwind handles most */
</style>
