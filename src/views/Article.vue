<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { User, Star, StarFilled } from '@element-plus/icons-vue';
import { getArticleDetail, getRelatedNews, checkFavorite, toggleFavorite, type ArticleDetail, type NewsItem } from '@/api/news';
import CommentSection from '@/components/CommentSection.vue';
import { useUserStore } from '@/stores/user';
import { ElMessage } from 'element-plus';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const article = ref<ArticleDetail | null>(null);
const relatedNews = ref<NewsItem[]>([]);
const loading = ref(true);
const isFavorite = ref(false);

onMounted(async () => {
  const id = route.params.id as string;
  if (id) {
    try {
      article.value = await getArticleDetail(id);
      relatedNews.value = await getRelatedNews(id);
      
      if (userStore.token) {
          const res = await checkFavorite(Number(id)) as any;
          isFavorite.value = res.favorited;
      }
    } catch (error) {
      console.error('Failed to fetch article:', error);
    } finally {
      loading.value = false;
    }
  }
});

const navigateToArticle = (id: number) => {
  router.push(`/article/${id}`).then(() => {
    // Force reload or update when navigating to same component
    window.location.reload(); 
    // Ideally, we should watch route param changes, but reload is safer for now to ensure all states reset
  });
};

const navigateToCategory = (id?: number) => {
  if (id) {
    router.push(`/category/${id}`);
  }
};

const handleFavorite = async () => {
    if (!userStore.token) {
        ElMessage.warning('请先登录');
        router.push('/login');
        return;
    }
    if (!article.value) return;
    
    try {
        const res = await toggleFavorite(article.value.id) as any;
        isFavorite.value = res.favorited;
        ElMessage.success(isFavorite.value ? '收藏成功' : '已取消收藏');
    } catch (error) {
        ElMessage.error('操作失败');
    }
}
</script>

<template>
  <div v-if="loading" class="container-width py-12 text-center">
    <el-skeleton :rows="10" animated />
  </div>
  
  <article v-else-if="article" class="container-width py-12">
    <!-- Article Header -->
    <header class="max-w-4xl mx-auto mb-12 text-center">
      <div class="flex items-center justify-center gap-2 text-sm font-medium mb-6">
        <span 
          class="text-accent uppercase tracking-wider cursor-pointer hover:underline"
          @click="navigateToCategory(article.categoryId)"
        >
          {{ article.category }}
        </span>
        <span class="text-muted-foreground">•</span>
        <span class="text-muted-foreground">{{ article.date }} {{ article.time }}</span>
      </div>
      <h1 class="text-4xl md:text-5xl font-bold leading-tight text-primary mb-8">
        {{ article.title }}
      </h1>
      <p class="text-xl text-muted-foreground leading-relaxed max-w-2xl mx-auto">
        {{ article.excerpt }}
      </p>
    </header>

    <!-- Featured Image -->
    <div v-if="article.image" class="aspect-video w-full overflow-hidden rounded-xl mb-12 bg-muted">
      <img 
        :src="article.image" 
        :alt="article.title" 
        class="w-full h-full object-cover"
      />
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-12 gap-12">
      <!-- Sidebar Left (Author & Share) -->
      <aside class="hidden lg:block lg:col-span-2 space-y-8 sticky top-24 h-fit">
        <div class="text-center">
          <div class="w-16 h-16 bg-secondary rounded-full mx-auto mb-3 flex items-center justify-center text-muted-foreground">
            <el-icon :size="32"><User /></el-icon>
          </div>
          <div class="font-bold text-primary">{{ article.author }}</div>
          <div class="text-xs text-muted-foreground">{{ article.authorRole }}</div>
        </div>
        <div class="flex flex-col gap-3">
          <el-button 
            round 
            class="w-full !ml-0" 
            :type="isFavorite ? 'warning' : 'default'"
            @click="handleFavorite"
          >
            <el-icon class="mr-2">
                <StarFilled v-if="isFavorite" />
                <Star v-else />
            </el-icon> 
            {{ isFavorite ? '已收藏' : '收藏' }}
          </el-button>
        </div>
      </aside>

      <!-- Article Body -->
      <div class="lg:col-span-8">
        <!-- Mobile Author Info -->
        <div class="lg:hidden flex items-center justify-between mb-8 border-b border-border pb-6">
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 bg-secondary rounded-full flex items-center justify-center text-muted-foreground">
              <el-icon :size="20"><User /></el-icon>
            </div>
            <div>
              <div class="font-bold text-primary text-sm">{{ article.author }}</div>
              <div class="text-xs text-muted-foreground">{{ article.authorRole }}</div>
            </div>
          </div>
          <div class="flex gap-2">
            <el-button 
                circle 
                :type="isFavorite ? 'warning' : 'default'"
                @click="handleFavorite"
            >
              <el-icon>
                  <StarFilled v-if="isFavorite" />
                  <Star v-else />
              </el-icon>
            </el-button>
          </div>
        </div>

        <div 
          class="prose prose-lg max-w-none 
          prose-headings:font-bold prose-headings:tracking-tight prose-headings:text-primary 
          prose-p:text-muted-foreground prose-p:leading-relaxed 
          prose-a:text-accent prose-a:no-underline hover:prose-a:underline
          prose-blockquote:border-l-4 prose-blockquote:border-accent prose-blockquote:bg-secondary/30 prose-blockquote:py-2 prose-blockquote:px-4 prose-blockquote:rounded-r-lg prose-blockquote:not-italic
          prose-img:rounded-xl"
          v-html="article.content"
        ></div>

        <!-- Tags -->
        <div class="mt-12 pt-8 border-t border-border">
          <div class="flex flex-wrap gap-2">
            <el-tag 
              effect="dark"
              type="info"
              class="!bg-secondary !text-secondary-foreground !border-none cursor-pointer hover:opacity-80"
              @click="navigateToCategory(article.categoryId)"
            >
              #{{ article.category }}
            </el-tag>
             <el-tag 
              effect="dark"
              type="info"
              class="!bg-secondary !text-secondary-foreground !border-none cursor-pointer hover:opacity-80"
              @click="router.push('/latest')"
            >
              #最新新闻
            </el-tag>
             <el-tag 
              effect="dark"
              type="info"
              class="!bg-secondary !text-secondary-foreground !border-none cursor-pointer hover:opacity-80"
              @click="router.push('/')"
            >
              #NewsHub
            </el-tag>
          </div>
        </div>

        <!-- Comments -->
        <CommentSection :article-id="article?.id || 0" />
      </div>

      <!-- Sidebar Right (Related) -->
      <aside class="lg:col-span-2 space-y-6">
        <h3 class="font-bold text-primary border-b border-border pb-2">相关阅读</h3>
        <div class="space-y-4">
          <div v-if="relatedNews.length === 0" class="text-sm text-muted-foreground">
            暂无相关推荐
          </div>
          <div 
            v-for="news in relatedNews" 
            :key="news.id" 
            class="block group cursor-pointer"
            @click="navigateToArticle(news.id)"
          >
            <span class="text-xs text-accent block mb-1">{{ news.category }}</span>
            <h4 class="text-sm font-medium leading-snug group-hover:text-primary/70 transition-colors">
              {{ news.title }}
            </h4>
          </div>
        </div>
      </aside>
    </div>
  </article>
  
  <div v-else class="container-width py-12 text-center">
    <el-empty description="文章未找到" />
  </div>
</template>

<style>
/* Custom prose styles if needed beyond Tailwind Typography */
.prose p {
  text-indent: 2em;
  margin-bottom: 1.5em;
  line-height: 1.8;
  text-align: justify;
}
</style>
