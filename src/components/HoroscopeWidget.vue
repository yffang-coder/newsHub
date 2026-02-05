<script setup lang="ts">
import { ref, computed } from 'vue';
import { Star } from '@element-plus/icons-vue';

const signs = [
  { name: '白羊座', date: '3.21-4.19', icon: '♈' },
  { name: '金牛座', date: '4.20-5.20', icon: '♉' },
  { name: '双子座', date: '5.21-6.21', icon: '♊' },
  { name: '巨蟹座', date: '6.22-7.22', icon: '♋' },
  { name: '狮子座', date: '7.23-8.22', icon: '♌' },
  { name: '处女座', date: '8.23-9.22', icon: '♍' },
  { name: '天秤座', date: '9.23-10.23', icon: '♎' },
  { name: '天蝎座', date: '10.24-11.22', icon: '♏' },
  { name: '射手座', date: '11.23-12.21', icon: '♐' },
  { name: '摩羯座', date: '12.22-1.19', icon: '♑' },
  { name: '水瓶座', date: '1.20-2.18', icon: '♒' },
  { name: '双鱼座', date: '2.19-3.20', icon: '♓' },
];

const selectedSign = ref(signs[0]);

const fortunes = [
  "今天运势极佳，适合开展新计划，财运亨通。",
  "注意人际关系，保持平和心态，工作上会有新机遇。",
  "桃花运旺盛，单身者有望脱单，有伴者感情升温。",
  "宜静不宜动，适合阅读和学习，避免大额投资。",
  "身体健康需关注，保持规律作息，避免过度劳累。",
  "贵人运强，遇到困难会有朋友相助，事业顺利。",
  "财运平稳，适合理财规划，避免冲动消费。",
  "心情愉悦，适合外出郊游，放松身心。",
];

const currentFortune = computed(() => {
  // Simple deterministic hash based on sign and date to keep it consistent for the day
  const dateStr = new Date().toDateString();
  const hash = (selectedSign.value.name + dateStr).split('').reduce((a, b) => a + b.charCodeAt(0), 0);
  return fortunes[hash % fortunes.length];
});

const stars = computed(() => {
   const dateStr = new Date().toDateString();
   const hash = (selectedSign.value.name + dateStr).split('').reduce((a, b) => a + b.charCodeAt(0), 0);
   return (hash % 2) + 4; // 4 or 5 stars
});

</script>

<template>
  <div class="bg-card rounded-xl border border-border p-6 shadow-sm mt-6">
    <div class="flex items-center justify-between mb-6">
      <h3 class="font-bold text-lg flex items-center gap-2">
        <span class="w-1 h-6 bg-purple-500 rounded-full"></span>
        星座运势
      </h3>
    </div>

    <div class="grid grid-cols-4 sm:grid-cols-6 gap-2 mb-6">
      <button 
        v-for="sign in signs" 
        :key="sign.name"
        @click="selectedSign = sign"
        class="flex flex-col items-center justify-center p-2 rounded-lg transition-all hover:bg-secondary"
        :class="selectedSign.name === sign.name ? 'bg-secondary ring-2 ring-purple-500/20 text-purple-600' : 'text-muted-foreground'"
      >
        <span class="text-xl mb-1">{{ sign.icon }}</span>
        <span class="text-[10px] scale-90">{{ sign.name }}</span>
      </button>
    </div>

    <div class="bg-secondary/30 rounded-lg p-4 relative overflow-hidden">
      <div class="absolute top-0 right-0 p-4 opacity-10 text-6xl pointer-events-none">
        {{ selectedSign.icon }}
      </div>
      
      <div class="relative z-10">
        <div class="flex items-center justify-between mb-2">
          <span class="font-bold text-lg">{{ selectedSign.name }}</span>
          <div class="flex text-yellow-400">
            <el-icon v-for="i in stars" :key="i"><Star /></el-icon>
          </div>
        </div>
        <p class="text-sm text-muted-foreground leading-relaxed">
          {{ currentFortune }}
        </p>
      </div>
    </div>
  </div>
</template>
