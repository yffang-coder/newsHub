<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { ElMessage } from 'element-plus';
import { sendNotification, getAllUsers, getNotificationHistory } from '@/api/admin';
import { Bell, User, Message, Document, Search, Refresh } from '@element-plus/icons-vue';

const form = ref({
  title: '',
  content: '',
  type: 'SYSTEM',
  isGlobal: false,
  userIds: [] as number[]
});

const users = ref<any[]>([]);
const history = ref<any[]>([]);
const loading = ref(false);
const refreshLoading = ref(false);

// Filter states
const searchKeyword = ref('');
const filterType = ref('');

const notificationTypes = [
  { label: '系统通知', value: 'SYSTEM', type: 'primary' },
  { label: '活动通知', value: 'ACTIVITY', type: 'success' },
  { label: '提醒', value: 'ALERT', type: 'danger' }
];

// Computed filtered history
const filteredHistory = computed(() => {
  return history.value.filter(item => {
    const matchesSearch = !searchKeyword.value || 
      item.title.toLowerCase().includes(searchKeyword.value.toLowerCase()) || 
      item.content.toLowerCase().includes(searchKeyword.value.toLowerCase());
    const matchesType = !filterType.value || item.type === filterType.value;
    return matchesSearch && matchesType;
  });
});

const getTagType = (type: string) => {
  const found = notificationTypes.find(t => t.value === type);
  return found ? found.type : 'info';
};

const getTypeName = (type: string) => {
  const found = notificationTypes.find(t => t.value === type);
  return found ? found.label : type;
};

const fetchData = async () => {
  refreshLoading.value = true;
  try {
    const [usersRes, historyRes] = await Promise.all([
      getAllUsers(),
      getNotificationHistory()
    ]);
    // The interceptor returns response.data directly
    users.value = (usersRes as any) || [];
    history.value = (historyRes as any) || [];
  } catch (error) {
    console.error('Failed to fetch data:', error);
    ElMessage.error('数据加载失败');
  } finally {
    refreshLoading.value = false;
  }
};

onMounted(() => {
  fetchData();
});

const formatDate = (date: any) => {
  if (!date) return '';
  if (Array.isArray(date)) {
    return new Date(date[0], date[1] - 1, date[2], date[3], date[4], date[5]).toLocaleString();
  }
  return new Date(date).toLocaleString();
};

const handleSubmit = async () => {
  if (!form.value.title || !form.value.content) {
    ElMessage.warning('请填写标题和内容');
    return;
  }
  
  if (!form.value.isGlobal && form.value.userIds.length === 0) {
    ElMessage.warning('请选择用户或全员发送');
    return;
  }

  loading.value = true;
  try {
    await sendNotification(form.value);
    ElMessage.success('通知发送成功');
    // Reset form
    form.value = {
      title: '',
      content: '',
      type: 'SYSTEM',
      isGlobal: false,
      userIds: []
    };
    // Refresh history
    const historyRes = await getNotificationHistory();
    history.value = (historyRes as any) || [];
  } catch (error) {
    console.error('Failed to send notification:', error);
    ElMessage.error('发送失败，请重试');
  } finally {
    loading.value = false;
  }
};
</script>

<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <h2 class="text-2xl font-bold tracking-tight">通知管理</h2>
      <el-button :icon="Refresh" circle @click="fetchData" :loading="refreshLoading" />
    </div>

    <div class="grid gap-6 lg:grid-cols-12">
      <!-- Send Notification Form (Left/Top) -->
      <div class="lg:col-span-4">
        <el-card shadow="hover">
          <template #header>
            <div class="flex items-center gap-2">
              <el-icon class="text-primary"><Bell /></el-icon>
              <span class="font-medium">发送新通知</span>
            </div>
          </template>
          
          <el-form :model="form" label-position="top">
            <el-form-item label="发送对象">
              <el-radio-group v-model="form.isGlobal" class="!w-full">
                <el-radio-button :value="false" class="w-1/2">指定用户</el-radio-button>
                <el-radio-button :value="true" class="w-1/2">全员广播</el-radio-button>
              </el-radio-group>
            </el-form-item>
            
            <el-form-item label="选择用户" v-if="!form.isGlobal">
              <el-select
                v-model="form.userIds"
                multiple
                filterable
                placeholder="搜索并选择用户"
                class="w-full"
                collapse-tags
                collapse-tags-tooltip
                :max-collapse-tags="3"
              >
                <el-option
                  v-for="user in users"
                  :key="user.id"
                  :label="`${user.username} (ID: ${user.id})`"
                  :value="user.id"
                />
              </el-select>
            </el-form-item>
            
            <el-form-item label="通知类型">
              <el-select v-model="form.type" placeholder="选择类型" class="w-full">
                <el-option
                  v-for="item in notificationTypes"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                >
                  <span class="flex items-center gap-2">
                    <el-tag :type="item.type as any" size="small" effect="dark" class="w-2 h-2 rounded-full p-0 border-none" />
                    {{ item.label }}
                  </span>
                </el-option>
              </el-select>
            </el-form-item>
            
            <el-form-item label="标题">
              <el-input v-model="form.title" placeholder="请输入通知标题" :prefix-icon="Document" />
            </el-form-item>
            
            <el-form-item label="内容">
              <el-input
                v-model="form.content"
                type="textarea"
                :rows="6"
                placeholder="请输入通知详细内容..."
                resize="none"
              />
            </el-form-item>
            
            <el-button type="primary" @click="handleSubmit" :loading="loading" class="w-full mt-4" size="large">
              <el-icon class="mr-2"><Message /></el-icon>
              立即发送
            </el-button>
          </el-form>
        </el-card>
      </div>

      <!-- Notification History (Right/Bottom) -->
      <div class="lg:col-span-8">
        <el-card shadow="hover" class="h-full flex flex-col">
          <template #header>
            <div class="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
              <div class="flex items-center gap-2">
                <el-icon class="text-primary"><Document /></el-icon>
                <span class="font-medium">历史记录</span>
                <el-tag type="info" round size="small">{{ filteredHistory.length }} 条</el-tag>
              </div>
              
              <div class="flex items-center gap-2 w-full sm:w-auto">
                <el-select v-model="filterType" placeholder="全部类型" clearable class="w-32" size="small">
                  <el-option
                    v-for="item in notificationTypes"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  />
                </el-select>
                <el-input
                  v-model="searchKeyword"
                  placeholder="搜索标题或内容"
                  :prefix-icon="Search"
                  size="small"
                  class="w-48"
                  clearable
                />
              </div>
            </div>
          </template>
          
          <el-table :data="filteredHistory" style="width: 100%" height="600" stripe>
            <el-table-column prop="title" label="标题" min-width="150" show-overflow-tooltip>
              <template #default="{ row }">
                <span class="font-medium text-gray-700">{{ row.title }}</span>
              </template>
            </el-table-column>
            
            <el-table-column prop="content" label="内容" min-width="200" show-overflow-tooltip />
            
            <el-table-column prop="type" label="类型" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="getTagType(row.type)" size="small" effect="light">
                  {{ getTypeName(row.type) }}
                </el-tag>
              </template>
            </el-table-column>
            
            <el-table-column prop="userId" label="接收对象" width="140">
              <template #default="{ row }">
                <div class="flex items-center gap-1">
                  <el-icon size="12" class="text-gray-400"><User /></el-icon>
                  <span class="truncate text-xs">
                    {{ users.find(u => u.id === row.userId)?.username || `ID: ${row.userId}` }}
                  </span>
                </div>
              </template>
            </el-table-column>
            
            <el-table-column prop="isRead" label="状态" width="80" align="center">
              <template #default="{ row }">
                <el-tag :type="row.isRead ? 'success' : 'info'" size="small" effect="plain" round>
                  {{ row.isRead ? '已读' : '未读' }}
                </el-tag>
              </template>
            </el-table-column>

            <el-table-column prop="createdAt" label="发送时间" width="160" align="right">
              <template #default="{ row }">
                <span class="text-xs text-gray-500">{{ formatDate(row.createdAt) }}</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </div>
    </div>
  </div>
</template>
