<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { Sunny, Cloudy, Pouring, Lightning, MostlyCloudy, PartlyCloudy, Loading } from '@element-plus/icons-vue';
import axios from 'axios';

interface WeatherData {
  name: string;
  temp: string;
  range: string;
  weather: string;
  icon: any;
  color: string;
}

const cities = ref<WeatherData[]>([]);
const loading = ref(true);
const currentDate = new Date().toLocaleDateString('zh-CN', { month: 'long', day: 'numeric', weekday: 'long' });

// List of Jiangsu cities to fetch
// const targetCities = ['南京', '苏州', '无锡', '常州', '徐州', '南通', '连云港', '淮安'];

const getWeatherIcon = (type: string) => {
  if (type.includes('晴')) return { icon: Sunny, color: 'text-orange-500' };
  if (type.includes('多云')) return { icon: MostlyCloudy, color: 'text-blue-400' };
  if (type.includes('阴')) return { icon: Cloudy, color: 'text-gray-500' };
  if (type.includes('雨')) return { icon: Pouring, color: 'text-blue-500' };
  if (type.includes('雷')) return { icon: Lightning, color: 'text-yellow-600' };
  return { icon: PartlyCloudy, color: 'text-gray-400' };
};

const fetchWeather = async () => {
  loading.value = true;
  try {
    const response = await axios.get('/api/public/weather');
    const data = response.data;
    
    if (Array.isArray(data) && data.length > 0) {
      cities.value = data.map((item: any) => {
        const { icon, color } = getWeatherIcon(item.type);
        // Clean up temp string if needed
        const low = item.low ? item.low.replace('°C', '') : 'N/A';
        const high = item.high ? item.high.replace('°C', '') : 'N/A';
        const currentTemp = item.temp || 'N/A';
        
        return {
          name: item.name,
          temp: currentTemp,
          range: `${low}°~${high}°`,
          weather: item.type,
          icon,
          color
        };
      });
    } else {
        // Fallback if empty
        cities.value = [];
    }
  } catch (error) {
    console.error('Failed to fetch weather', error);
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchWeather();
});
</script>

<template>
  <div class="bg-card rounded-xl border border-border p-6 shadow-sm">
    <div class="flex items-center justify-between mb-6">
      <h3 class="font-bold text-lg flex items-center gap-2">
        <span class="w-1 h-6 bg-primary rounded-full"></span>
        全国天气
      </h3>
      <span class="text-xs text-muted-foreground">{{ currentDate }}</span>
    </div>
    
    <div v-if="loading" class="flex justify-center py-4 text-primary">
      <el-icon class="is-loading text-2xl"><Loading /></el-icon>
      <span class="ml-2">加载中...</span>
    </div>

    <div v-else class="grid grid-cols-2 gap-4 max-h-[500px] overflow-y-auto custom-scrollbar">
      <div 
        v-for="city in cities" 
        :key="city.name"
        class="flex items-center justify-between p-3 rounded-lg bg-secondary/30 hover:bg-secondary/50 transition-colors"
      >
        <div class="flex items-center gap-3">
          <component :is="city.icon" class="w-6 h-6" :class="city.color" />
          <span class="font-medium text-sm">{{ city.name }}</span>
        </div>
        <div class="flex flex-col items-end">
          <span class="font-bold text-sm">{{ city.temp }}</span>
          <span class="text-xs text-muted-foreground">{{ city.weather }} <span class="opacity-70">({{ city.range }})</span></span>
        </div>
      </div>
    </div>
  </div>
</template>
