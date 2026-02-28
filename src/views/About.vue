<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getPublicSetting } from '@/api/public';
import { marked } from 'marked';

const route = useRoute();
const router = useRouter();
const activeTab = ref('about');
const content = ref('');
const loading = ref(false);

const loadContent = async () => {
  loading.value = true;
  try {
    const key = `${activeTab.value}_content`;
    const res = await getPublicSetting(key) as string;
    content.value = await marked.parse(res || '');
  } catch (e) {
    content.value = '<p>暂无内容</p>';
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  if (route.hash) {
    const hash = route.hash.replace('#', '');
    if (['about', 'contact', 'careers', 'privacy'].includes(hash)) {
      activeTab.value = hash;
    }
  }
  loadContent();
});

watch(() => activeTab.value, (newTab) => {
  router.replace({ hash: `#${newTab}` });
  loadContent();
});

watch(() => route.hash, (newHash) => {
    if (newHash) {
        const hash = newHash.replace('#', '');
        if (['about', 'contact', 'careers', 'privacy'].includes(hash)) {
            activeTab.value = hash;
        }
    }
});
</script>

<template>
  <div class="container-width py-12">
    <div class="grid grid-cols-1 md:grid-cols-4 gap-8">
      <!-- Sidebar Navigation -->
      <aside class="md:col-span-1">
        <nav class="space-y-1 sticky top-24">
          <a 
            href="#about" 
            class="block px-4 py-2 rounded-lg transition-colors"
            :class="activeTab === 'about' ? 'bg-secondary text-primary font-medium' : 'text-muted-foreground hover:bg-secondary/50'"
            @click.prevent="activeTab = 'about'"
          >
            关于我们
          </a>
          <a 
            href="#contact" 
            class="block px-4 py-2 rounded-lg transition-colors"
            :class="activeTab === 'contact' ? 'bg-secondary text-primary font-medium' : 'text-muted-foreground hover:bg-secondary/50'"
            @click.prevent="activeTab = 'contact'"
          >
            联系方式
          </a>
          <a 
            href="#careers" 
            class="block px-4 py-2 rounded-lg transition-colors"
            :class="activeTab === 'careers' ? 'bg-secondary text-primary font-medium' : 'text-muted-foreground hover:bg-secondary/50'"
            @click.prevent="activeTab = 'careers'"
          >
            加入我们
          </a>
          <a 
            href="#privacy" 
            class="block px-4 py-2 rounded-lg transition-colors"
            :class="activeTab === 'privacy' ? 'bg-secondary text-primary font-medium' : 'text-muted-foreground hover:bg-secondary/50'"
            @click.prevent="activeTab = 'privacy'"
          >
            隐私政策
          </a>
        </nav>
      </aside>

      <!-- Content Area -->
      <main class="md:col-span-3 min-h-[500px]">
        <div v-if="loading" class="space-y-4">
          <el-skeleton :rows="5" animated />
        </div>
        <div v-else class="prose max-w-none prose-headings:text-primary prose-p:text-muted-foreground" v-html="content">
        </div>
      </main>
    </div>
  </div>
</template>