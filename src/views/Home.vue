<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { Clock, ArrowRight, User, Close } from '@element-plus/icons-vue';
import { getFeaturedNews, getTrendingNews, getLatestNews, getLatestNewsCount, searchNews, type NewsItem } from '@/api/news';
import WeatherWidget from '@/components/WeatherWidget.vue';
import HoroscopeWidget from '@/components/HoroscopeWidget.vue';

const router = useRouter();
const route = useRoute();
const isSearching = ref(false);
const loading = ref(false);

const searchQuery = computed(() => route.query.q as string || '');

const handleSearch = async (query: string) => {
  if (!query || !query.trim()) {
    fetchNews();
    return;
  }
  
  loading.value = true;
  try {
    const results = await searchNews(query);
    latestNews.value = results;
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
};

const clearSearch = () => {
  router.push({ name: 'home' });
};

watch(() => route.query.q, (newQuery) => {
  if (typeof newQuery === 'string') {
    handleSearch(newQuery);
  } else {
    fetchNews();
  }
});

const fetchNews = async () => {
  loading.value = true;
  try {
    const [featured, trending, latest, count] = await Promise.all([
      getFeaturedNews(),
      getTrendingNews(5),
      getLatestNews(currentPage.value, pageSize.value),
      getLatestNewsCount()
    ]);
    featuredNews.value = featured;
    trendingNews.value = trending;
    latestNews.value = latest;
    totalArticles.value = count;
  } catch (error) {
    console.error('Failed to fetch news:', error);
  } finally {
    loading.value = false;
  }
};

const featuredNews = ref<NewsItem | null>(null);
const trendingNews = ref<NewsItem[]>([]);
const latestNews = ref<NewsItem[]>([]);
const currentPage = ref(1);
const pageSize = ref(9); // Display 9 articles per page
const totalArticles = ref(0);

onMounted(() => {
  if (route.query.q && typeof route.query.q === 'string') {
    handleSearch(route.query.q);
  } else {
    fetchNews();
  }
});

const navigateToArticle = (id: number) => {
  router.push(`/article/${id}`);
};

const handlePageChange = (page: number) => {
  currentPage.value = page;
  fetchNews();
};
</script>

<template>
  <main class="container mx-auto px-4 py-6">


    <!-- Hero Section (Hidden during search) -->
    <section v-if="!searchQuery" class="grid grid-cols-1 lg:grid-cols-12 gap-6 mb-4">
      
      <!-- Main Featured Article -->
      <div v-if="featuredNews" class="lg:col-span-8 self-start">

        <div 
          class="group cursor-pointer"
          @click="navigateToArticle(featuredNews.id)"
        >
          <div v-if="featuredNews.image" class="relative aspect-video overflow-hidden rounded-xl mb-4 bg-muted">
            <img 
              :src="featuredNews.image" 
              :alt="featuredNews.title"
              class="object-cover w-full h-full transition-transform duration-500 group-hover:scale-105"
            />
            <span class="absolute top-4 left-4 bg-accent text-accent-foreground px-3 py-1 text-xs font-bold uppercase tracking-wider rounded-sm">
              头条
            </span>
          </div>
          <div class="space-y-3">
            <div class="flex items-center gap-3 text-sm text-muted-foreground">
              <span class="text-primary font-medium">{{ featuredNews.category }}</span>
              <span>•</span>
              <span class="flex items-center gap-1">
                <el-icon><Clock /></el-icon> {{ featuredNews.date }}
              </span>
              <span>•</span>
              <span class="flex items-center gap-1">
                <el-icon><User /></el-icon> {{ featuredNews.author }}
              </span>
            </div>
            <h1 class="text-3xl md:text-4xl font-bold leading-tight group-hover:text-primary/80 transition-colors">
              {{ featuredNews.title }}
            </h1>
            <p class="text-muted-foreground text-lg leading-relaxed" :class="featuredNews.image ? 'line-clamp-2' : 'line-clamp-6'">
              {{ featuredNews.excerpt }}
            </p>
          </div>
        </div>
      </div>

      <!-- Sidebar -->
      <div :class="{'lg:col-span-12': !featuredNews, 'lg:col-span-4': featuredNews}" class="space-y-8">
        <!-- Horoscope Widget -->
        <HoroscopeWidget />

        <!-- Trending News -->
        <div class="bg-card rounded-xl border border-border p-6 shadow-sm">
          <div class="flex items-center justify-between border-b border-border pb-4 mb-4">
            <h2 class="text-xl font-bold text-primary">热门新闻</h2>
            <RouterLink :to="{ name: 'latest-news' }" class="text-sm text-accent hover:underline no-underline ">查看更多</RouterLink>
          </div>
          <div class="space-y-6">
            <div 
              v-for="(news, index) in trendingNews" 
              :key="news.id" 
              class="group cursor-pointer"
              @click="navigateToArticle(news.id)"
            >
              <div class="flex gap-4 items-start">
                <span class="text-2xl font-bold text-muted/30 font-mono">0{{ index + 1 }}</span>
                <div class="space-y-1">
                  <span class="text-xs font-medium text-accent">{{ news.category }}</span>
                  <h3 class="font-semibold leading-snug group-hover:text-primary/80 transition-colors">
                    {{ news.title }}
                  </h3>
                  <div class="text-xs text-muted-foreground flex items-center gap-3 mt-1">
                    <span class="flex items-center gap-1">
                      <el-icon><Clock /></el-icon> {{ news.date }}
                    </span>
                    <span class="flex items-center gap-1">
                      <el-icon><User /></el-icon> {{ news.author }}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Search Status (Shown only during search) -->
    <section v-if="searchQuery" class="mb-8">
      <div class="flex items-center justify-between bg-secondary/30 p-4 rounded-xl">
        <div class="flex items-center gap-2">
          <span class="text-muted-foreground">搜索结果:</span>
          <span class="font-bold text-primary">"{{ searchQuery }}"</span>
          <span v-if="latestNews.length > 0" class="text-xs text-muted-foreground ml-2">共找到 {{ latestNews.length }} 条相关文章</span>
          <span v-else class="text-xs text-red-500 ml-2">未找到相关文章</span>
        </div>
        <el-button :icon="Close" circle size="small" @click="clearSearch" title="清除搜索" />
      </div>
    </section>

    <!-- Content Grid -->
    <section>
      <div v-if="!searchQuery" class="flex items-center justify-between border-b border-border pb-2 mb-4">
        <h2 class="text-2xl font-bold text-primary">最新发布</h2>
        <RouterLink :to="{ name: 'latest-news' }" class="group flex items-center gap-1 text-sm font-medium text-muted-foreground hover:text-primary transition-colors no-underline">
          全部新闻 <el-icon class="transition-transform group-hover:translate-x-1"><ArrowRight /></el-icon>
        </RouterLink>
      </div>
      
      <div v-if="latestNews.length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
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

      <!-- Pagination -->
      <div v-if="!searchQuery && totalArticles > pageSize" class="flex justify-center mt-8">
        <el-pagination
          background
          layout="prev, pager, next"
          :total="totalArticles"
          :page-size="pageSize"
          :current-page="currentPage"
          @current-change="handlePageChange"
        />
      </div>

      <!-- No Results State for Latest News (Non-search) -->
      <div v-else-if="!loading && !searchQuery && latestNews.length === 0" class="py-20 text-center">
        <el-empty description="暂无新闻" />
        <el-button type="primary" link @click="router.push({ name: 'latest-news' })">查看全部新闻</el-button>
      </div>
    </section>
  </main>
</template>
