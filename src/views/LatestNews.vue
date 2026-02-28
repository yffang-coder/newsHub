<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { Clock, User, ArrowRight } from '@element-plus/icons-vue';
import { getLatestNews, getLatestNewsCount, type NewsItem } from '@/api/news';

const router = useRouter();
const route = useRoute();

const latestNews = ref<NewsItem[]>([]);
const loading = ref(false);
const currentPage = ref(parseInt(route.query.page as string) || 1);
const pageSize = ref(12); // Display 12 articles per page for the dedicated page
const totalArticles = ref(0);

const fetchNews = async () => {
  loading.value = true;
  try {
    const [news, count] = await Promise.all([
      getLatestNews(currentPage.value, pageSize.value),
      getLatestNewsCount()
    ]);
    latestNews.value = news;
    totalArticles.value = count;
  } catch (error) {
    console.error('Failed to fetch latest news:', error);
  } finally {
    loading.value = false;
  }
};

const navigateToArticle = (id: number) => {
  router.push(`/article/${id}`);
};

const handlePageChange = (page: number) => {
  currentPage.value = page;
  router.push({ query: { page: page.toString() } }); // Update URL query parameter
  fetchNews();
};

onMounted(() => {
  fetchNews();
});

watch(
  () => route.query.page,
  (newPage) => {
    const pageNum = parseInt(newPage as string);
    if (!isNaN(pageNum) && pageNum > 0 && pageNum !== currentPage.value) {
      currentPage.value = pageNum;
      fetchNews();
    } else if (!newPage && currentPage.value !== 1) {
      // If page query is removed, reset to page 1
      currentPage.value = 1;
      fetchNews();
    }
  }
);
</script>

<template>
  <main class="container mx-auto px-4 py-6">
    <h1 class="text-3xl font-bold mb-8 text-primary">全部新闻</h1>

    <div v-if="loading" class="py-20 text-center">
      <el-icon class="is-loading" size="40px"><Loading /></el-icon>
      <p class="text-muted-foreground mt-4">加载中...</p>
    </div>

    <div v-else-if="latestNews.length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
      <article 
        v-for="news in latestNews" 
        :key="news.id" 
        class="group flex flex-col h-full cursor-pointer"
        @click="navigateToArticle(news.id)"
      >
        <div v-if="news.image" class="aspect-[3/2] overflow-hidden rounded-lg mb-4 bg-muted">
          <img 
            :src="news.image" 
            :alt="news.title"
            class="object-cover w-full h-full transition-transform duration-500 group-hover:scale-105"
          />
        </div>
        <div class="flex-1 flex flex-col">
          <div class="flex items-center gap-2 text-xs font-medium mb-2">
            <span class="text-accent">{{ news.category }}</span>
            <span class="text-muted-foreground">•</span>
            <span class="text-muted-foreground">{{ news.date }}</span>
            <span class="text-muted-foreground">•</span>
            <span class="flex items-center gap-1 text-muted-foreground">
              <el-icon><User /></el-icon> {{ news.author }}
            </span>
          </div>
          <h3 class="text-xl font-bold mb-2 leading-snug group-hover:text-primary/80 transition-colors">
            {{ news.title }}
          </h3>
          <p class="text-muted-foreground text-sm leading-relaxed mb-4 flex-1" :class="news.image ? 'line-clamp-3' : 'line-clamp-6'">
            {{ news.excerpt }}
          </p>
          <div class="inline-flex items-center text-sm font-medium text-primary hover:text-accent transition-colors mt-auto">
            阅读全文 <el-icon class="ml-1"><ArrowRight /></el-icon>
          </div>
        </div>
      </article>
    </div>

    <div v-else class="py-20 text-center">
      <el-empty description="暂无新闻" />
    </div>

    <div v-if="totalArticles > pageSize" class="flex justify-center mt-8">
      <el-pagination
        background
        layout="prev, pager, next"
        :total="totalArticles"
        :page-size="pageSize"
        :current-page="currentPage"
        @current-change="handlePageChange"
      />
    </div>
  </main>
</template>
