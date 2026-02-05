<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getNewsByCategory, type NewsItem } from '@/api/news';
import { Picture, Calendar, User } from '@element-plus/icons-vue';

const route = useRoute();
const router = useRouter();
const newsList = ref<NewsItem[]>([]);
const loading = ref(false);
const categoryId = computed(() => Number(route.params.id));

const categoryNames: Record<number, string> = {
  1: '时事',
  2: '国际',
  3: '科技',
  4: '体育',
  5: '财经',
  6: '文化'
};

const categoryName = computed(() => categoryNames[categoryId.value] || '新闻列表');

const fetchNews = async () => {
  loading.value = true;
  try {
    newsList.value = await getNewsByCategory(categoryId.value, 1, 50);
  } catch (error) {
    console.error(error);
  } finally {
    loading.value = false;
  }
};

watch(() => route.params.id, () => {
  fetchNews();
});

onMounted(() => {
  fetchNews();
});

const navigateToArticle = (id: number) => {
  router.push(`/article/${id}`);
};
</script>

<template>
  <div class="container mx-auto px-4 py-8">
    <div class="mb-8 border-b border-gray-200 pb-4">
      <h1 class="text-3xl font-bold text-gray-900">{{ categoryName }}</h1>
    </div>

    <div v-if="loading" class="flex justify-center py-20">
      <el-skeleton animated :rows="5" />
    </div>

    <div v-else-if="newsList.length === 0" class="text-center py-20 text-gray-500">
      暂无相关新闻
    </div>

    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <article 
        v-for="news in newsList" 
        :key="news.id" 
        class="bg-white rounded-xl shadow-sm hover:shadow-md transition-shadow duration-300 overflow-hidden border border-gray-100 cursor-pointer flex flex-col h-full group"
        @click="navigateToArticle(news.id)"
      >
        <div class="relative h-48 overflow-hidden bg-gray-100">
          <img 
            v-if="news.image" 
            :src="news.image" 
            :alt="news.title"
            class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-110"
          />
          <div v-else class="w-full h-full flex items-center justify-center text-gray-400">
            <el-icon :size="40"><Picture /></el-icon>
          </div>
          <div class="absolute top-0 right-0 bg-primary text-white text-xs px-3 py-1 m-2 rounded-full">
            {{ news.category === 'Category' ? categoryName : news.category }}
          </div>
        </div>
        
        <div class="p-5 flex flex-col flex-grow">
          <h2 class="text-lg font-bold text-gray-900 mb-3 line-clamp-2 group-hover:text-primary transition-colors">
            {{ news.title }}
          </h2>
          
          <p class="text-gray-500 text-sm line-clamp-3 mb-4 flex-grow">
            {{ news.excerpt }}
          </p>
          
          <div class="flex items-center justify-between text-xs text-gray-400 mt-auto pt-4 border-t border-gray-100">
            <span class="flex items-center gap-1">
              <el-icon><User /></el-icon> {{ news.author }}
            </span>
            <span class="flex items-center gap-1">
              <el-icon><Calendar /></el-icon> {{ news.date }}
            </span>
          </div>
        </div>
      </article>
    </div>
  </div>
</template>
