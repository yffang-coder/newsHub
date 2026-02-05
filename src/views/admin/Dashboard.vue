<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { getDashboardStats } from '@/api/admin';
import { 
  DataLine, 
  Document, 
  User, 
  ChatDotRound 
} from '@element-plus/icons-vue';

const stats = ref([
  { title: '总访问量', value: '0', icon: DataLine, color: '#409EFF', key: 'totalVisits' },
  { title: '文章总数', value: '0', icon: Document, color: '#67C23A', key: 'totalArticles' },
  { title: '注册用户', value: '0', icon: User, color: '#E6A23C', key: 'totalUsers' },
  { title: '今日评论', value: '0', icon: ChatDotRound, color: '#F56C6C', key: 'todayComments' },
]);

const recentArticles = ref<any[]>([]);
const recentComments = ref<any[]>([]);

onMounted(async () => {
  try {
    const data = await getDashboardStats();
    stats.value = stats.value.map(stat => ({
      ...stat,
      value: data[stat.key]?.toString() || '0'
    }));
    
    recentArticles.value = data.recentArticles || [];
    recentComments.value = data.recentComments || [];
  } catch (error) {
    console.error('Failed to fetch stats', error);
  }
});
</script>

<template>
  <div class="space-y-6">
    <!-- Stats Cards -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
      <el-card v-for="stat in stats" :key="stat.title" shadow="hover" class="!border-none">
        <div class="flex items-center justify-between">
          <div>
            <div class="text-sm text-muted-foreground mb-2">{{ stat.title }}</div>
            <div class="text-2xl font-bold text-primary">{{ stat.value }}</div>
          </div>
          <div 
            class="w-12 h-12 rounded-lg flex items-center justify-center bg-opacity-10"
            :style="{ backgroundColor: `${stat.color}1a`, color: stat.color }"
          >
            <el-icon :size="24">
              <component :is="stat.icon" />
            </el-icon>
          </div>
        </div>
      </el-card>
    </div>

    <!-- Recent Activity -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <el-card header="最新文章" shadow="hover" class="!border-none">
        <el-table :data="recentArticles" style="width: 100%">
          <el-table-column prop="title" label="标题" show-overflow-tooltip />
          <el-table-column prop="views" label="浏览" width="80" />
          <el-table-column prop="publishTime" label="日期" width="120">
            <template #default="{ row }">
              {{ new Date(row.publishTime).toLocaleDateString() }}
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <el-card header="最新评论" shadow="hover" class="!border-none">
        <div class="space-y-4">
          <div v-for="comment in recentComments" :key="comment.id" class="flex gap-3 pb-4 border-b border-border last:border-0 last:pb-0">
            <el-avatar :size="32" class="flex-shrink-0">{{ (comment.username || 'U')[0].toUpperCase() }}</el-avatar>
            <div>
              <div class="flex items-center gap-2 mb-1">
                <span class="font-bold text-sm text-primary">{{ comment.username || '未知用户' }}</span>
                <span class="text-xs text-muted-foreground">{{ new Date(comment.createdAt).toLocaleString() }}</span>
              </div>
              <p class="text-sm text-muted-foreground line-clamp-2">
                {{ comment.content }}
              </p>
            </div>
          </div>
          <div v-if="recentComments.length === 0" class="text-center text-muted-foreground py-4">
            暂无评论
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>
