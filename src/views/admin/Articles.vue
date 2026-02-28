<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import { Plus, Edit, Delete, Search, Refresh, User } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getArticles, deleteArticle, createArticle, updateArticle, getCategories } from '@/api/admin';

const searchQuery = ref('');
const dateRange = ref<[string, string] | null>(null);
const sortField = ref<string | undefined>(undefined);
const sortOrder = ref<string | undefined>(undefined);
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);
const loading = ref(false);
const articles = ref<any[]>([]);
const categories = ref<any[]>([]);

const dialogVisible = ref(false);
const isEdit = ref(false);
const articleForm = ref({
  id: undefined as number | undefined,
  title: '',
  summary: '',
  content: '',
  coverImage: '',
  categoryId: undefined as number | undefined,
  status: 'PUBLISHED',
  sourceName: '',
  sourceUrl: '',
  publishTime: ''
});

const fetchCategories = async () => {
  try {
    const { data } = await getCategories();
    categories.value = data || [];
  } catch (e) {
    console.error('Failed to fetch categories', e);
  }
};

const fetchArticles = async () => {
  loading.value = true;
  try {
    const [startDate, endDate] = dateRange.value || [];
    const { data } = await getArticles(
      currentPage.value, 
      pageSize.value, 
      searchQuery.value.trim() || undefined,
      startDate,
      endDate,
      sortField.value,
      sortOrder.value
    );
    articles.value = data.items || [];
    total.value = data.total || 0;
  } catch (e) {
    ElMessage.error('获取文章列表失败');
  } finally {
    loading.value = false;
  }
};

const handleSortChange = ({ prop, order }: { prop: string, order: string }) => {
  sortField.value = prop;
  sortOrder.value = order;
  fetchArticles();
};

const handleCreate = () => {
  isEdit.value = false;
  articleForm.value = {
    id: undefined,
    title: '',
    summary: '',
    content: '',
    coverImage: '',
    categoryId: undefined,
    status: 'PUBLISHED',
    sourceName: '',
    sourceUrl: '',
    publishTime: ''
  };
  dialogVisible.value = true;
};

const handleEdit = (row: any) => {
  isEdit.value = true;
  articleForm.value = { ...row };
  dialogVisible.value = true;
};

const handleSave = async () => {
  if (!articleForm.value.title) {
    ElMessage.warning('请输入标题');
    return;
  }
  try {
    if (isEdit.value) {
      await updateArticle(articleForm.value.id!, articleForm.value);
      ElMessage.success('更新成功');
    } else {
      await createArticle(articleForm.value);
      ElMessage.success('创建成功');
    }
    dialogVisible.value = false;
    fetchArticles();
  } catch (e) {
    ElMessage.error(isEdit.value ? '更新失败' : '创建失败');
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

onMounted(() => {
  fetchCategories();
  fetchArticles();
});
watch([currentPage, pageSize], fetchArticles);
</script>

<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <h2 class="text-xl font-bold">文章管理</h2>
      <el-button :icon="Refresh" circle @click="fetchArticles" :loading="loading" />
    </div>

    <el-card shadow="never" class="!border-none">
      <!-- Toolbar -->
      <div class="flex flex-col sm:flex-row justify-between gap-4 mb-6">
      <div class="flex gap-2 flex-wrap">
        <el-input
          v-model="searchQuery"
          placeholder="搜索文章标题..."
          class="w-64"
          :prefix-icon="Search"
          @keyup.enter="fetchArticles"
        />
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD HH:mm:ss"
          @change="fetchArticles"
        />
        <el-button @click="fetchArticles">搜索</el-button>
      </div>
      <el-button type="primary" :icon="Plus" @click="handleCreate">写文章</el-button>
    </div>

    <!-- Table -->
    <el-table :data="articles" style="width: 100%" v-loading="loading" @sort-change="handleSortChange">
      <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
      <el-table-column prop="sourceName" label="来源" width="150" show-overflow-tooltip>
        <template #default="{ row }">
          <div class="flex items-center gap-1">
            <el-icon><User /></el-icon>
            <span>{{ row.sourceName || 'NewsHub' }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="categoryName" label="分类" width="120">
        <template #default="{ row }">
          <el-tag size="small" effect="plain">{{ row.categoryName || row.categoryId }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'PUBLISHED' ? 'success' : 'info'" size="small">
            {{ row.status === 'PUBLISHED' ? '已发布' : (row.status === 'DRAFT' ? '草稿' : row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="views" label="浏览量" width="100" align="right">
        <template #default="{ row }">
          {{ row.views ?? 0 }}
        </template>
      </el-table-column>
      <el-table-column prop="publishTime" label="发布日期" width="180" sortable="custom">
        <template #default="{ row }">
          {{ row.publishTime ? new Date(row.publishTime).toLocaleString() : '-' }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" :icon="Edit" size="small" @click="handleEdit(row)">编辑</el-button>
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

    <!-- Edit/Create Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑文章' : '写文章'"
      width="60%"
    >
      <el-form :model="articleForm" label-width="80px">
        <el-form-item label="标题" required>
          <el-input v-model="articleForm.title" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="articleForm.categoryId" placeholder="选择分类" class="w-full">
            <el-option
              v-for="cat in categories"
              :key="cat.id"
              :label="cat.name"
              :value="cat.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="作者/来源">
          <el-input v-model="articleForm.sourceName" placeholder="例如: 人民网" />
        </el-form-item>
        <el-form-item label="摘要">
          <el-input v-model="articleForm.summary" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="articleForm.content" type="textarea" :rows="10" />
        </el-form-item>
        <el-form-item label="封面图">
          <el-input v-model="articleForm.coverImage" placeholder="图片URL" />
        </el-form-item>
        <el-form-item label="发布时间">
          <el-date-picker
            v-model="articleForm.publishTime"
            type="datetime"
            placeholder="选择发布时间"
            value-format="YYYY-MM-DDTHH:mm:ss"
            class="w-full"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="articleForm.status">
            <el-radio label="PUBLISHED">发布</el-radio>
            <el-radio label="DRAFT">草稿</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSave">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </el-card>
  </div>
</template>
