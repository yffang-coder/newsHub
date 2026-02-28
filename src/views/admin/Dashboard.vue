<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue';
import { getDashboardStats } from '@/api/admin';
import * as echarts from 'echarts';
import { 
  DataLine, 
  Document, 
  User, 
  ChatDotRound,
  Refresh
} from '@element-plus/icons-vue';

const stats = ref([
  { title: '总访问量', value: '0', icon: DataLine, color: '#409EFF', key: 'totalVisits' },
  { title: '文章总数', value: '0', icon: Document, color: '#67C23A', key: 'totalArticles' },
  { title: '注册用户', value: '0', icon: User, color: '#E6A23C', key: 'totalUsers' },
  { title: '今日评论', value: '0', icon: ChatDotRound, color: '#F56C6C', key: 'todayComments' },
]);

const recentArticles = ref<any[]>([]);
const recentComments = ref<any[]>([]);
const loading = ref(false);

const articleTrendChartRef = ref<HTMLElement | null>(null);
const commentTrendChartRef = ref<HTMLElement | null>(null);
const categoryChartRef = ref<HTMLElement | null>(null);
const visitsChartRef = ref<HTMLElement | null>(null);
const topViewsChartRef = ref<HTMLElement | null>(null);

let articleTrendChart: echarts.ECharts | null = null;
let commentTrendChart: echarts.ECharts | null = null;
let categoryChart: echarts.ECharts | null = null;
let visitsChart: echarts.ECharts | null = null;
let topViewsChart: echarts.ECharts | null = null;

const chartTextColor = '#FFFFFF';
const chartGridLineColor = 'rgba(255, 255, 255, 0.15)';
const chartTooltipBg = 'rgba(30, 30, 30, 0.95)';

const initCharts = () => {
  if (articleTrendChartRef.value) articleTrendChart = echarts.init(articleTrendChartRef.value);
  if (commentTrendChartRef.value) commentTrendChart = echarts.init(commentTrendChartRef.value);
  if (categoryChartRef.value) categoryChart = echarts.init(categoryChartRef.value);
  if (visitsChartRef.value) visitsChart = echarts.init(visitsChartRef.value);
  if (topViewsChartRef.value) topViewsChart = echarts.init(topViewsChartRef.value);
};

const updateCharts = (data: any) => {
  // 1. Trend Charts (Split into Article and Comment)
  const articleTrend = data.articleTrend || [];
  const commentTrend = data.commentTrend || [];
  
  // Generate last 7 days to ensure continuous x-axis
  const sortedDates: string[] = [];
  for (let i = 6; i >= 0; i--) {
    const d = new Date();
    d.setDate(d.getDate() - i);
    const dateStr = d.toISOString().split('T')[0];
    sortedDates.push(dateStr);
  }

  const articleData = sortedDates.map(date => {
    const found = articleTrend.find((d: any) => d.date === date);
    return found ? found.count : 0;
  });

  const commentData = sortedDates.map(date => {
    const found = commentTrend.find((d: any) => d.date === date);
    return found ? found.count : 0;
  });

  // Article Trend Chart Configuration
  articleTrendChart?.setOption({
    textStyle: { color: chartTextColor },
    title: { 
      text: '文章发布趋势', 
      left: 'left',
      textStyle: { color: chartTextColor, fontWeight: 600, fontSize: 14 }
    },
    tooltip: {
      trigger: 'axis',
      backgroundColor: chartTooltipBg,
      borderColor: 'rgba(255, 255, 255, 0.15)',
      borderWidth: 1,
      textStyle: { color: '#FFFFFF' }
    },
    grid: { left: '3%', right: '4%', top: 54, bottom: 16, containLabel: true },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: sortedDates,
      axisLabel: { color: chartTextColor, fontSize: 12 },
      axisLine: { lineStyle: { color: chartGridLineColor } },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'value',
      axisLabel: { color: chartTextColor, fontSize: 12 },
      axisLine: { show: false },
      splitLine: { lineStyle: { color: chartGridLineColor, type: 'dashed' } }
    },
    series: [
      {
        name: '文章发布',
        type: 'line',
        data: articleData,
        smooth: true,
        showSymbol: false,
        lineStyle: { width: 3, color: '#67C23A' },
        areaStyle: {
          opacity: 0.8,
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(103, 194, 58, 0.5)' },
            { offset: 1, color: 'rgba(103, 194, 58, 0.01)' }
          ])
        },
        itemStyle: { color: '#67C23A' }
      }
    ]
  }, { notMerge: true });

  // Comment Trend Chart Configuration
  commentTrendChart?.setOption({
    textStyle: { color: chartTextColor },
    title: { 
      text: '新增评论趋势', 
      left: 'left',
      textStyle: { color: chartTextColor, fontWeight: 600, fontSize: 14 }
    },
    tooltip: {
      trigger: 'axis',
      backgroundColor: chartTooltipBg,
      borderColor: 'rgba(144, 147, 153, 0.25)',
      borderWidth: 1,
      textStyle: { color: '#303133' }
    },
    grid: { left: '3%', right: '4%', top: 54, bottom: 16, containLabel: true },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: sortedDates,
      axisLabel: { color: chartTextColor, fontSize: 12 },
      axisLine: { lineStyle: { color: chartGridLineColor } },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'value',
      axisLabel: { color: chartTextColor, fontSize: 12 },
      axisLine: { show: false },
      splitLine: { lineStyle: { color: chartGridLineColor, type: 'dashed' } }
    },
    series: [
      {
        name: '新增评论',
        type: 'line',
        data: commentData,
        smooth: true,
        showSymbol: false,
        lineStyle: { width: 3, color: '#F56C6C' },
        areaStyle: {
          opacity: 0.8,
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(245, 108, 108, 0.5)' },
            { offset: 1, color: 'rgba(245, 108, 108, 0.01)' }
          ])
        },
        itemStyle: { color: '#F56C6C' }
      }
    ]
  }, { notMerge: true });

  // 2. Category Distribution (Pie)
  const categoryDist = data.categoryDistribution || [];
  categoryChart?.setOption({
    textStyle: { color: chartTextColor },
    title: { 
      text: '文章分类占比', 
      left: 'center',
      textStyle: { color: chartTextColor, fontWeight: 600, fontSize: 14 }
    },
    tooltip: {
      trigger: 'item',
      backgroundColor: chartTooltipBg,
      borderColor: 'rgba(255, 255, 255, 0.15)',
      borderWidth: 1,
      textStyle: { color: '#FFFFFF' }
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      top: 'middle',
      textStyle: { color: chartTextColor, fontSize: 12 }
    },
    series: [
      {
        name: '文章数量',
        type: 'pie',
        radius: ['35%', '62%'],
        data: categoryDist.map((item: any) => ({ value: item.value, name: item.name })),
        label: { color: chartTextColor, fontSize: 12 },
        labelLine: { lineStyle: { color: chartGridLineColor } },
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }, { notMerge: true });

  // 3. Visits by Category (Bar)
  const visitsDist = data.visitsByCategory || [];
  visitsChart?.setOption({
    textStyle: { color: chartTextColor },
    title: { 
      text: '各分类阅读量',
      left: 'left',
      textStyle: { color: chartTextColor, fontWeight: 600, fontSize: 14 }
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      backgroundColor: chartTooltipBg,
      borderColor: 'rgba(255, 255, 255, 0.15)',
      borderWidth: 1,
      textStyle: { color: '#FFFFFF' }
    },
    grid: { left: '3%', right: '4%', top: 54, bottom: 24, containLabel: true },
    xAxis: {
      type: 'category',
      data: visitsDist.map((d: any) => d.name),
      axisLabel: { interval: 0, rotate: 22, color: chartTextColor, fontSize: 12 },
      axisLine: { lineStyle: { color: chartGridLineColor } },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'value',
      axisLabel: { color: chartTextColor, fontSize: 12 },
      axisLine: { show: false },
      splitLine: { lineStyle: { color: chartGridLineColor, type: 'dashed' } }
    },
    series: [
      {
        name: '阅读量',
        type: 'bar',
        data: visitsDist.map((d: any) => d.value),
        barWidth: 18,
        itemStyle: {
          borderRadius: [6, 6, 0, 0],
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#409EFF' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.25)' }
          ])
        }
      }
    ]
  }, { notMerge: true });

  const topArticlesByViews = data.topArticlesByViews || [];
  const topTitles = topArticlesByViews.map((a: any) => a.title || `#${a.id}`);
  const topViews = topArticlesByViews.map((a: any) => Number(a.views || 0));

  topViewsChart?.setOption({
    textStyle: { color: chartTextColor },
    title: {
      text: '浏览量 Top 5',
      left: 'left',
      textStyle: { color: chartTextColor, fontWeight: 600, fontSize: 14 }
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      backgroundColor: chartTooltipBg,
      borderColor: 'rgba(255, 255, 255, 0.15)',
      borderWidth: 1,
      textStyle: { color: '#FFFFFF' }
    },
    grid: { left: 120, right: 24, top: 54, bottom: 16, containLabel: false },
    xAxis: {
      type: 'value',
      axisLabel: { color: chartTextColor, fontSize: 12 },
      axisLine: { show: false },
      splitLine: { lineStyle: { color: chartGridLineColor, type: 'dashed' } }
    },
    yAxis: {
      type: 'category',
      data: topTitles.slice().reverse(),
      axisTick: { show: false },
      axisLine: { show: false },
      axisLabel: {
        color: chartTextColor,
        fontSize: 12,
        formatter: (value: string) => (value.length > 12 ? `${value.slice(0, 12)}…` : value)
      }
    },
    series: [
      {
        name: '浏览量',
        type: 'bar',
        data: topViews.slice().reverse(),
        barWidth: 14,
        itemStyle: {
          borderRadius: 6,
          color: new echarts.graphic.LinearGradient(1, 0, 0, 0, [
            { offset: 0, color: '#7C3AED' },
            { offset: 1, color: 'rgba(124, 58, 237, 0.25)' }
          ])
        },
        label: { show: true, position: 'right', color: chartTextColor, fontSize: 12 }
      }
    ]
  }, { notMerge: true });
};

const refreshData = async () => {
  loading.value = true;
  try {
    const { data } = await getDashboardStats();
    stats.value = stats.value.map(stat => ({
      ...stat,
      value: data[stat.key]?.toString() || '0'
    }));
    
    recentArticles.value = data.recentArticles || [];
    recentComments.value = data.recentComments || [];
    
    // Update charts
    updateCharts(data);
  } catch (error) {
    console.error('Failed to fetch stats', error);
  } finally {
    loading.value = false;
  }
};

const handleResize = () => {
  articleTrendChart?.resize();
  commentTrendChart?.resize();
  categoryChart?.resize();
  visitsChart?.resize();
  topViewsChart?.resize();
};

onMounted(async () => {
  await nextTick();
  initCharts();
  refreshData();
  window.addEventListener('resize', handleResize);
});

onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
  articleTrendChart?.dispose();
  commentTrendChart?.dispose();
  categoryChart?.dispose();
  visitsChart?.dispose();
  topViewsChart?.dispose();
});
</script>

<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <h2 class="text-2xl font-bold tracking-tight">仪表盘</h2>
      <el-button :icon="Refresh" circle @click="refreshData" :loading="loading" />
    </div>

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

    <!-- Charts -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <el-card shadow="hover" class="!border-none">
        <div ref="articleTrendChartRef" style="height: 300px;"></div>
      </el-card>
      <el-card shadow="hover" class="!border-none">
        <div ref="commentTrendChartRef" style="height: 300px;"></div>
      </el-card>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <el-card shadow="hover" class="!border-none">
        <div ref="categoryChartRef" style="height: 300px;"></div>
      </el-card>
      <el-card shadow="hover" class="!border-none">
        <div ref="visitsChartRef" style="height: 300px;"></div>
      </el-card>
    </div>

    <div class="grid grid-cols-1 gap-6">
      <el-card shadow="hover" class="!border-none">
        <div ref="topViewsChartRef" style="height: 320px;"></div>
      </el-card>
    </div>
  </div>
</template>
