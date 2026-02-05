<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { getSettings, updateSettings } from '@/api/admin';

const form = ref({
  site_name: '',
  site_description: '',
  footer_text: ''
});
const loading = ref(false);
const submitting = ref(false);

const fetchSettings = async () => {
  loading.value = true;
  try {
    const data = await getSettings();
    form.value = {
      site_name: data.site_name || '',
      site_description: data.site_description || '',
      footer_text: data.footer_text || ''
    };
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
    </div>

    <el-card shadow="hover" class="!border-none max-w-2xl">
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

        <el-form-item class="pt-4">
          <el-button type="primary" :loading="submitting" @click="handleSubmit">
            保存更改
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>
