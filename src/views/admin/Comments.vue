<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import { Delete } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getComments, deleteComment } from '@/api/admin';

const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);
const loading = ref(false);
const comments = ref<any[]>([]);

const fetchComments = async () => {
  loading.value = true;
  try {
    const data = await getComments(currentPage.value, pageSize.value);
    comments.value = data.items || [];
    total.value = data.total || 0;
  } catch (e) {
    ElMessage.error('获取评论列表失败');
  } finally {
    loading.value = false;
  }
};

const handleDelete = (row: any) => {
  ElMessageBox.confirm(
    '确定要删除这条评论吗？此操作无法撤销。',
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    try {
      await deleteComment(row.id);
      ElMessage.success('评论已删除');
      fetchComments();
    } catch (error) {
      ElMessage.error('删除失败');
    }
  }).catch(() => {});
};

onMounted(fetchComments);
watch([currentPage, pageSize], fetchComments);
</script>

<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <h2 class="text-2xl font-bold text-primary">评论管理</h2>
      <el-button type="primary" @click="fetchComments">刷新列表</el-button>
    </div>

    <el-card shadow="hover" class="!border-none">
      <el-table :data="comments" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="用户" width="160">
          <template #default="{ row }">
            <div class="flex items-center gap-3">
              <el-avatar :size="28">{{ (row.username || 'U')[0].toUpperCase() }}</el-avatar>
              <span class="text-sm">{{ row.username || '未知用户' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="articleId" label="文章ID" width="120" />
        <el-table-column prop="content" label="内容" min-width="300" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="时间" width="180">
          <template #default="{ row }">
            {{ new Date(row.createdAt).toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="danger" :icon="Delete" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="flex justify-end mt-6">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
        />
      </div>
    </el-card>
  </div>
</template>

