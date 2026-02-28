<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { Refresh } from '@element-plus/icons-vue';
import { getSettings, updateSettings } from '@/api/admin';

const form = ref<Record<string, string>>({
  site_name: '',
  site_description: '',
  footer_text: '',
  retention_days: '3',
  about_content: '',
  contact_content: '',
  careers_content: '',
  privacy_content: ''
});

const activeTab = ref('basic');
const loading = ref(false);
const submitting = ref(false);

const pages = [
  { label: '关于我们', key: 'about_content' },
  { label: '联系方式', key: 'contact_content' },
  { label: '加入我们', key: 'careers_content' },
  { label: '隐私政策', key: 'privacy_content' }
];

const selectedPageKey = ref('about_content');

const fetchSettings = async () => {
  loading.value = true;
  try {
    const { data } = await getSettings();
    form.value = { ...form.value, ...data as Record<string, string> };
  } catch (error) {
    ElMessage.error('获取设置失败');
  } finally {
    loading.value = false;
  }
};

const handleSubmit = async () => {
  submitting.value = true;
  try {
    await updateSettings(form.value);
    ElMessage.success('系统设置已更新');
  } catch (error) {
    ElMessage.error('更新失败');
  } finally {
    submitting.value = false;
  }
};

onMounted(() => {
  fetchSettings();
});
</script>

<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <h2 class="text-2xl font-bold text-primary">系统设置</h2>
      <el-button :icon="Refresh" circle @click="fetchSettings" :loading="loading" />
    </div>

    <el-tabs v-model="activeTab" class="max-w-4xl">
      <el-tab-pane label="基本设置" name="basic">
        <el-card shadow="hover" class="!border-none">
          <el-form :model="form" label-position="top" v-loading="loading">
            <el-form-item label="网站名称">
              <el-input v-model="form.site_name" placeholder="请输入网站名称" />
            </el-form-item>
            
            <el-form-item label="网站描述">
              <el-input 
                v-model="form.site_description" 
                type="textarea" 
                rows="3"
                placeholder="请输入网站描述 (SEO Meta Description)" 
              />
            </el-form-item>
            
            <el-form-item label="底部版权文字">
              <el-input v-model="form.footer_text" placeholder="例如: © 2024 NewsHub. All rights reserved." />
            </el-form-item>

            <el-form-item label="新闻保留天数">
              <el-input v-model="form.retention_days" type="number" placeholder="3" />
              <div class="text-xs text-gray-500 mt-1">超过此天数的新闻将被自动清理</div>
            </el-form-item>

            <el-form-item class="pt-4">
              <el-button type="primary" :loading="submitting" @click="handleSubmit">
                保存更改
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>
      
      <el-tab-pane label="页面内容" name="pages">
        <el-card shadow="hover" class="!border-none">
          <div class="mb-4">
             <el-radio-group v-model="selectedPageKey">
                <el-radio-button v-for="page in pages" :key="page.key" :label="page.key">{{ page.label }}</el-radio-button>
             </el-radio-group>
          </div>
          
          <el-form :model="form" label-position="top" v-loading="loading">
             <el-form-item :label="pages.find(p => p.key === selectedPageKey)?.label + '内容 (Markdown)'">
               <el-input 
                 v-model="form[selectedPageKey]" 
                 type="textarea" 
                 :rows="15" 
                 placeholder="请输入页面内容 (支持 Markdown)"
               />
             </el-form-item>
             
             <el-form-item class="pt-4">
               <el-button type="primary" :loading="submitting" @click="handleSubmit">
                 保存更改
               </el-button>
             </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>
