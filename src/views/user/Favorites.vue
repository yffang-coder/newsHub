<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { getFavorites, type NewsItem } from '@/api/news';
import { Clock, ArrowRight } from '@element-plus/icons-vue';

const router = useRouter();
const favorites = ref<NewsItem[]>([]);
const loading = ref(true);

onMounted(async () => {
  try {
    favorites.value = await getFavorites();
  } catch (error) {
    console.error('Failed to fetch favorites:', error);
  } finally {
    loading.value = false;
  }
});

const navigateToArticle = (id: number) => {
  router.push(`/article/${id}`);
};
</script>

<template>
  <div class="container mx-auto px-4 py-8">
    <h1 class="text-3xl font-bold mb-8 text-primary">我的收藏</h1>
    
    <div v-if="loading" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
       <el-skeleton v-for="i in 6" :key="i" :rows="5" animated />
    </div>

    <div v-else-if="favorites.length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
        <article 
          v-for="news in favorites" 
          :key="news.id" 
          class="group flex flex-col h-full cursor-pointer bg-card rounded-lg overflow-hidden border border-border hover:shadow-lg transition-shadow"
          @click="navigateToArticle(news.id)"
        >
          <div v-if="news.image" class="aspect-[3/2] overflow-hidden bg-muted">
            <img 
              :src="news.image" 
              :alt="news.title"
              class="object-cover w-full h-full transition-transform duration-500 group-hover:scale-105"
            />
          </div>
          <div class="flex-1 flex flex-col p-4">
            <div class="flex items-center gap-2 text-xs font-medium mb-2">
              <span class="text-accent">{{ news.category }}</span>
              <span class="text-muted-foreground">•</span>
              <span class="text-muted-foreground">{{ news.date }}</span>
            </div>
            <h3 class="text-xl font-bold mb-2 leading-snug group-hover:text-primary/80 transition-colors">
              {{ news.title }}
            </h3>
            <p class="text-muted-foreground text-sm leading-relaxed mb-4 flex-1 line-clamp-3">
              {{ news.excerpt }}
            </p>
            <div class="inline-flex items-center text-sm font-medium text-primary hover:text-accent transition-colors mt-auto">
              阅读全文 <el-icon class="ml-1"><ArrowRight /></el-icon>
            </div>
          </div>
        </article>
    </div>

    <div v-else class="text-center py-12 bg-secondary/20 rounded-xl">
      <el-empty description="暂无收藏文章" />
      <el-button type="primary" @click="router.push('/')">去浏览</el-button>
    </div>
  </div>
</template>
