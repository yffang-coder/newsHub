<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Menu as MenuIcon, Monitor, Star, Moon, Sunny, Bell, Location, Sunny as SunnyIcon } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getUnreadCount, getNotifications, markAsRead, markAllAsRead, type Notification } from '@/api/notification'
import { getDailyHighlights, type NewsItem } from '@/api/news'
import { getWeatherByIp, type WeatherData } from '@/api/weather'
import SockJS from 'sockjs-client/dist/sockjs';
import { Stomp } from '@stomp/stompjs';

const router = useRouter();
const searchQuery = ref('');
const currentUser = ref<any>(null);
const isDark = ref(false);

const unreadCount = ref(0);
const notifications = ref<Notification[]>([]);
const showNotifications = ref(false);
let stompClient: any = null;

const weatherInfo = ref<WeatherData | null>(null);
const dailyHighlights = ref<NewsItem[]>([]);

const connectWebSocket = () => {
  if (!currentUser.value) return;

  const socket = new SockJS('http://localhost:8080/ws');
  stompClient = Stomp.over(socket);
  stompClient.debug = () => {}; // Disable debug logs

  stompClient.connect({}, () => {
    // Subscribe to user-specific notifications
    stompClient.subscribe(`/topic/notifications/${currentUser.value.id}`, (message: any) => {
      const notification = JSON.parse(message.body);
      notifications.value.unshift(notification);
      unreadCount.value++;
    });
  }, (error: any) => {
    console.error('WebSocket connection error:', error);
  });
};

const disconnectWebSocket = () => {
  if (stompClient && stompClient.connected) {
    stompClient.disconnect();
  }
};


const loadNotifications = async () => {
  if (!currentUser.value) return;
  try {
    const res: any = await getNotifications();
    notifications.value = res;
    // Update unread count based on fetched notifications or separate API
    const countRes: any = await getUnreadCount();
    unreadCount.value = countRes.count;
  } catch (error) {
    console.error('Failed to load notifications', error);
  }
};

const handleNotificationClick = async (notification: Notification) => {
  if (!notification.isRead) {
    await markAsRead(notification.id);
    notification.isRead = true;
    unreadCount.value = Math.max(0, unreadCount.value - 1);
  }
  // Navigate if relatedId exists (assuming it's an article for now)
  if (notification.relatedId) {
    router.push(`/article/${notification.relatedId}`);
  }
};

const handleMarkAllRead = async () => {
  await markAllAsRead();
  notifications.value.forEach(n => n.isRead = true);
  unreadCount.value = 0;
};

const handleThemeChange = (val: any) => {
  isDark.value = val;
  if (isDark.value) {
    document.documentElement.classList.add('dark');
    localStorage.setItem('theme', 'dark');
  } else {
    document.documentElement.classList.remove('dark');
    localStorage.setItem('theme', 'light');
  }
};

const handleSearch = () => {
  const query = searchQuery.value.trim();
  if (query) {
    router.push({ name: 'home', query: { q: query } });
  } else {
    ElMessage.warning('请输入搜索内容');
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
  
  if (currentUser.value) {
    loadNotifications();
    connectWebSocket();
  }

  // Fetch weather and daily highlights
  getWeatherByIp().then(data => {
    if (data && data.length > 0) {
      weatherInfo.value = data[0];
      console.log('Weather Info:', weatherInfo.value); // Add this line for debugging
    } else {
      console.log('No weather data received.'); // Add this line for debugging
    }
  }).catch(error => {
    console.error('Error fetching weather:', error); // Add this line for debugging
  });
  getDailyHighlights().then(data => {
    dailyHighlights.value = data;
  });

  // Check theme preference
  const savedTheme = localStorage.getItem('theme');
  const systemDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
  
  if (savedTheme === 'dark' || (!savedTheme && systemDark)) {
    isDark.value = true;
    document.documentElement.classList.add('dark');
  } else {
    isDark.value = false;
    document.documentElement.classList.remove('dark');
  }

  // Simple polling for auth state changes in this simplified implementation
  // In a real app, use Pinia or an Event Bus
  intervalId = setInterval(checkUser, 1000);
});

onUnmounted(() => {
  if (intervalId) clearInterval(intervalId);
  disconnectWebSocket();
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
        <!-- Weather Info -->
        <div v-if="weatherInfo" class="hidden md:flex items-center gap-1 text-sm text-muted-foreground">
          <el-icon><Location /></el-icon>
          <span>{{ weatherInfo.name }}</span>
          <el-icon><SunnyIcon /></el-icon>
          <span>{{ weatherInfo.type }} {{ weatherInfo.temp }}</span>
        </div>

        <!-- Daily Highlights -->
        <el-popover
          v-if="dailyHighlights.length > 0"
          placement="bottom"
          :width="300"
          trigger="hover"
        >
          <template #reference>
            <el-button link class="!text-primary hover:!text-accent transition-colors">
              今日要闻
            </el-button>
          </template>
          <div class="flex flex-col">
            <div class="font-bold mb-2">今日要闻</div>
            <div 
              v-for="item in dailyHighlights" 
              :key="item.id"
              class="p-2 hover:bg-secondary rounded cursor-pointer transition-colors mb-1"
              @click="router.push(`/article/${item.id}`)"
            >
              <div class="text-sm font-medium">{{ item.title }}</div>
              <div class="text-xs text-muted-foreground mt-1">{{ item.date }}</div>
            </div>
          </div>
        </el-popover>

        <!-- Search Bar -->
        <div class="hidden md:flex items-center mr-2">
           <el-input
             v-model="searchQuery"
             placeholder="搜索文章..."
             class="w-48 transition-all focus:w-64"
             @keyup.enter="handleSearch"
           >
             <template #suffix>
               <el-button 
                 link 
                 :icon="Search" 
                 @click="handleSearch"
                 class="!p-0 !h-auto hover:!text-primary"
               />
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
                <el-dropdown-item @click="router.push('/favorites')">
                  <el-icon><Star /></el-icon>我的收藏
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

        <!-- Notification Bell -->
        <template v-if="currentUser">
          <el-popover
            placement="bottom"
            :width="300"
            trigger="click"
            @show="loadNotifications"
          >
            <template #reference>
              <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="mr-2">
                <el-button circle text class="!text-muted-foreground hover:!text-primary transition-colors">
                  <el-icon :size="20"><Bell /></el-icon>
                </el-button>
              </el-badge>
            </template>
            <div class="flex flex-col">
              <div class="flex justify-between items-center mb-2 px-2">
                <span class="font-bold">通知</span>
                <el-button link type="primary" size="small" @click="handleMarkAllRead" :disabled="unreadCount === 0">全部已读</el-button>
              </div>
              <div class="max-h-64 overflow-y-auto">
                <div v-if="notifications.length === 0" class="text-center text-muted-foreground py-4 text-sm">
                  暂无通知
                </div>
                <div 
                  v-else
                  v-for="item in notifications" 
                  :key="item.id"
                  class="p-2 hover:bg-secondary rounded cursor-pointer transition-colors mb-1"
                  :class="{ 'opacity-50': item.isRead }"
                  @click="handleNotificationClick(item)"
                >
                  <div class="text-sm font-medium">{{ item.title }}</div>
                  <div class="text-xs text-muted-foreground mt-1">{{ item.content }}</div>
                </div>
              </div>
            </div>
          </el-popover>
        </template>

        <!-- Theme Toggle -->
        <el-switch
          v-model="isDark"
          inline-prompt
          :active-icon="Moon"
          :inactive-icon="Sunny"
          style="--el-switch-on-color: #333; --el-switch-off-color: #e5e7eb"
          @change="handleThemeChange"
        />

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
