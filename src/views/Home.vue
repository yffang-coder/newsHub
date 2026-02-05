<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { Clock, ArrowRight } from '@element-plus/icons-vue';
import { getFeaturedNews, getTrendingNews, getLatestNews, searchNews, type NewsItem } from '@/api/news';
import WeatherWidget from '@/components/WeatherWidget.vue';
import HoroscopeWidget from '@/components/HoroscopeWidget.vue';

const router = useRouter();
const route = useRoute();
const isSearching = ref(false);

const handleSearch = async (query: string) => {
  if (!query || !query.trim()) {
    // If empty, reset to latest news
    fetchNews();
    return;
  }
  
  isSearching.value = true;
  try {
    const results = await searchNews(query);
    latestNews.value = results;
  } catch (e) {
    console.error(e);
  } finally {
    isSearching.value = false;
  }
};

watch(() => route.query.q, (newQuery) => {
  if (typeof newQuery === 'string') {
    handleSearch(newQuery);
  } else {
    fetchNews();
  }
});

const fetchNews = async () => {
  try {
    const [featured, trending, latest] = await Promise.all([
      getFeaturedNews(),
      getTrendingNews(),
      getLatestNews()
    ]);
    featuredNews.value = featured;
    trendingNews.value = trending;
    latestNews.value = latest;
  } catch (error) {
    console.error('Failed to fetch news:', error);
  }
};

const featuredNews = ref<NewsItem | null>(null);
const trendingNews = ref<NewsItem[]>([]);
const latestNews = ref<NewsItem[]>([]);

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
</script>

<template>
  <main class="container mx-auto px-4 py-6">


    <!-- Hero Section -->
    <section class="grid grid-cols-1 lg:grid-cols-12 gap-6 mb-4">
      
      <!-- Main Featured Article -->
      <div class="lg:col-span-8">

        <div 
          v-if="featuredNews" 
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
              <span>{{ featuredNews.author }}</span>
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
      <div class="lg:col-span-4 space-y-8">
        <!-- Weather Widget -->
        <WeatherWidget />

        <!-- Horoscope Widget -->
        <HoroscopeWidget />

        <!-- Trending News -->
        <div class="bg-card rounded-xl border border-border p-6 shadow-sm">
          <div class="flex items-center justify-between border-b border-border pb-4 mb-4">
            <h2 class="text-xl font-bold text-primary">热门新闻</h2>
            <RouterLink to="/trending" class="text-sm text-accent hover:underline no-underline">查看更多</RouterLink>
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
                  <span class="text-xs text-muted-foreground flex items-center gap-1 mt-1">
                    <el-icon><Clock /></el-icon> {{ news.date }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- Ad Placeholder -->
        <div class="bg-secondary/50 rounded-lg p-6 text-center space-y-2">
          <p class="text-xs text-muted-foreground uppercase tracking-widest">Advertisement</p>
          <p class="font-semibold text-primary">订阅 NewsHub Pro</p>
          <p class="text-sm text-muted-foreground">获取无广告阅读体验和深度独家报道</p>
          <el-button link type="primary" class="!text-accent hover:underline">立即订阅 &rarr;</el-button>
        </div>
      </div>
    </section>


    <!-- Latest News Grid -->
    <section>
      <div class="flex items-center justify-between border-b border-border pb-2 mb-4">
        <h2 class="text-2xl font-bold text-primary">最新发布</h2>
        <RouterLink to="/latest" class="group flex items-center gap-1 text-sm font-medium text-muted-foreground hover:text-primary transition-colors no-underline">
          全部新闻 <el-icon class="transition-transform group-hover:translate-x-1"><ArrowRight /></el-icon>
        </RouterLink>
      </div>
      
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
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
    </section>

    <!-- About Module (Footer) -->
    <section class="mt-16 pt-8 border-t border-border">
      <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
        <!-- About Us -->
        <div class="space-y-4">
          <h3 class="text-lg font-bold text-primary">关于 NewsHub</h3>
          <p class="text-muted-foreground text-sm leading-relaxed">
            NewsHub 是一个致力于为您提供最新、最快、最深度新闻报道的聚合平台。我们要做的不仅是传递信息，更是连接世界，启发思考。
          </p>
        </div>

        <!-- Contact -->
        <div class="space-y-4">
          <h3 class="text-lg font-bold text-primary">联系我们</h3>
          <ul class="space-y-2 text-sm text-muted-foreground">
            <li>Email: contact@newshub.com</li>
            <li>Phone: +86 123 4567 8900</li>
            <li>Address: 北京市朝阳区科技园区 NewsHub 大厦</li>
          </ul>
        </div>

        <!-- Links -->
        <div class="space-y-4">
          <h3 class="text-lg font-bold text-primary">快速链接</h3>
          <div class="flex flex-col space-y-2 text-sm">
            <RouterLink to="/" class="text-muted-foreground hover:text-accent">首页</RouterLink>
            <RouterLink to="/latest" class="text-muted-foreground hover:text-accent">最新新闻</RouterLink>
            <RouterLink to="/trending" class="text-muted-foreground hover:text-accent">热门排行</RouterLink>
            <a href="#" class="text-muted-foreground hover:text-accent">隐私政策</a>
            <a href="#" class="text-muted-foreground hover:text-accent">服务条款</a>
          </div>
        </div>
      </div>
      <div class="mt-8 pt-8 border-t border-border text-center text-sm text-muted-foreground">
        &copy; {{ new Date().getFullYear() }} NewsHub. All rights reserved.
      </div>
    </section>
  </main>
</template>
