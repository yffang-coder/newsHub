<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { User, Share, Star } from '@element-plus/icons-vue';
import { getArticleDetail, type ArticleDetail } from '@/api/news';
import CommentSection from '@/components/CommentSection.vue';

const route = useRoute();
const article = ref<ArticleDetail | null>(null);
const loading = ref(true);

onMounted(async () => {
  const id = route.params.id as string;
  if (id) {
    try {
      article.value = await getArticleDetail(id);
    } catch (error) {
      console.error('Failed to fetch article:', error);
    } finally {
      loading.value = false;
    }
  }
});
</script>

<template>
  <div v-if="loading" class="container-width py-12 text-center">
    <el-skeleton :rows="10" animated />
  </div>
  
  <article v-else-if="article" class="container-width py-12">
    <!-- Article Header -->
    <header class="max-w-4xl mx-auto mb-12 text-center">
      <div class="flex items-center justify-center gap-2 text-sm font-medium mb-6">
        <span class="text-accent uppercase tracking-wider">{{ article.category }}</span>
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
          <el-button round class="w-full !ml-0">
            <el-icon class="mr-2"><Share /></el-icon> 分享
          </el-button>
          <el-button round class="w-full !ml-0">
            <el-icon class="mr-2"><Star /></el-icon> 收藏
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
            <el-button circle>
              <el-icon><Share /></el-icon>
            </el-button>
            <el-button circle>
              <el-icon><Star /></el-icon>
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
              v-for="tag in ['气候变化', '巴黎协定', '碳中和', '联合国']" 
              :key="tag" 
              effect="dark"
              type="info"
              class="!bg-secondary !text-secondary-foreground !border-none cursor-pointer hover:opacity-80"
            >
              #{{ tag }}
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
          <div v-for="i in 3" :key="i" class="block group cursor-pointer">
            <span class="text-xs text-accent block mb-1">国际</span>
            <h4 class="text-sm font-medium leading-snug group-hover:text-primary/70 transition-colors">
              欧盟提出新的环保法案，限制一次性塑料制品的使用
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
</style>
