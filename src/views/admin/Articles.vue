<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import { Plus, Edit, Delete, Search } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getArticles, deleteArticle } from '@/api/admin';

const searchQuery = ref('');
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);
const loading = ref(false);
const articles = ref<any[]>([]);

const fetchArticles = async () => {
  loading.value = true;
  try {
    const data = await getArticles(currentPage.value, pageSize.value, searchQuery.value.trim() || undefined);
    articles.value = data.items || [];
    total.value = data.total || 0;
  } catch (e) {
    ElMessage.error('获取文章列表失败');
  } finally {
    loading.value = false;
  }
};

const handleDelete = (row: any) => {
  ElMessageBox.confirm(
    '确定要删除这篇文章吗？此操作无法撤销。',
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    try {
      await deleteArticle(row.id);
      ElMessage.success('删除成功');
      fetchArticles();
    } catch (error) {
      ElMessage.error('删除失败');
    }
  }).catch(() => {});
};

onMounted(fetchArticles);
watch([currentPage, pageSize], fetchArticles);
</script>

<template>
  <el-card shadow="never" class="!border-none">
    <!-- Toolbar -->
    <div class="flex flex-col sm:flex-row justify-between gap-4 mb-6">
      <div class="flex gap-2">
        <el-input
          v-model="searchQuery"
          placeholder="搜索文章标题..."
          class="w-64"
          :prefix-icon="Search"
        />
        <el-select placeholder="分类" class="w-32">
          <el-option label="全部" value="" />
          <el-option label="科技" value="tech" />
          <el-option label="环境" value="env" />
        </el-select>
      </div>
      <el-button type="primary" :icon="Plus">写文章</el-button>
    </div>

    <!-- Table -->
    <el-table :data="articles" style="width: 100%" v-loading="loading">
      <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
      <el-table-column prop="categoryId" label="分类" width="120">
        <template #default="{ row }">
          <el-tag size="small" effect="plain">类别ID {{ row.categoryId }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'PUBLISHED' ? 'success' : 'info'" size="small">
            {{ row.status === 'PUBLISHED' ? '已发布' : (row.status === 'DRAFT' ? '草稿' : row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="publishTime" label="发布日期" width="180">
        <template #default="{ row }">
          {{ new Date(row.publishTime).toLocaleString() }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" :icon="Edit" size="small">编辑</el-button>
          <el-button link type="danger" :icon="Delete" size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- Pagination -->
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
</template>
