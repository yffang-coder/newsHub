<script setup lang="ts">
import { ref } from 'vue';
import { useRoute, useRouter, RouterView } from 'vue-router';
import { 
  Menu, 
  Document, 
  User, 
  Setting, 
  Fold, 
  Expand,
  Monitor,
  ChatDotRound,
  Bell
} from '@element-plus/icons-vue';

const route = useRoute();
const router = useRouter();
const isCollapse = ref(false);

const handleCommand = (command: string) => {
  if (command === 'logout') {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    router.push('/login');
  } else if (command === 'home') {
    router.push('/');
  }
};
</script>

<template>
  <el-container class="h-screen bg-muted-background">
    <!-- Sidebar -->
    <el-aside :width="isCollapse ? '64px' : '240px'" class="transition-all duration-300 bg-background border-r border-border">
      <div class="h-16 flex items-center justify-center border-b border-border">
        <span v-if="!isCollapse" class="text-xl font-bold text-primary">NewsHub Admin</span>
        <span v-else class="text-xl font-bold text-primary">N</span>
      </div>
      
      <el-menu
        :default-active="route.path"
        class="border-none bg-transparent"
        :collapse="isCollapse"
        router
      >
        <el-menu-item index="/admin/dashboard">
          <el-icon><Menu /></el-icon>
          <template #title>仪表盘</template>
        </el-menu-item>
        <el-menu-item index="/admin/articles">
          <el-icon><Document /></el-icon>
          <template #title>文章管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/comments">
          <el-icon><ChatDotRound /></el-icon>
          <template #title>评论管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/users">
          <el-icon><User /></el-icon>
          <template #title>用户管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/notifications">
          <el-icon><Bell /></el-icon>
          <template #title>通知管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/settings">
          <el-icon><Setting /></el-icon>
          <template #title>系统设置</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <!-- Header -->
      <el-header class="bg-background border-b border-border flex items-center justify-between px-6">
        <div class="flex items-center gap-4">
          <el-button link @click="isCollapse = !isCollapse">
            <el-icon :size="20">
              <Expand v-if="isCollapse" />
              <Fold v-else />
            </el-icon>
          </el-button>
        </div>

        <div class="flex items-center gap-4">
          <el-tooltip content="返回前台">
            <el-button circle @click="handleCommand('home')">
              <el-icon><Monitor /></el-icon>
            </el-button>
          </el-tooltip>
          
          <el-dropdown @command="handleCommand">
            <span class="flex items-center gap-2 cursor-pointer hover:opacity-80">
              <el-avatar :size="32" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
              <span class="text-sm font-medium text-primary">Admin User</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>个人资料</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- Main Content -->
      <el-main class="bg-muted-background p-6">
        <RouterView v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </RouterView>
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
