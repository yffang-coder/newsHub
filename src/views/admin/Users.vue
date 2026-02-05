<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getAllUsers, deleteUser, updateUserRole } from '@/api/admin';
import { Delete } from '@element-plus/icons-vue';

const users = ref([]);
const loading = ref(false);

const fetchUsers = async () => {
  loading.value = true;
  try {
    const data = await getAllUsers();
    users.value = data;
  } catch (error) {
    ElMessage.error('获取用户列表失败');
  } finally {
    loading.value = false;
  }
};

const handleDelete = (user: any) => {
  ElMessageBox.confirm(
    `确定要删除用户 "${user.username}" 吗? 此操作无法撤销。`,
    '警告',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    try {
      await deleteUser(user.id);
      ElMessage.success('用户已删除');
      fetchUsers();
    } catch (error) {
      ElMessage.error('删除失败');
    }
  }).catch(() => {});
};

const handleRoleChange = async (user: any, newRole: string) => {
  try {
    await updateUserRole(user.id, newRole);
    ElMessage.success('权限已更新');
    user.role = newRole;
  } catch (error) {
    ElMessage.error('更新失败');
    // Revert change locally if needed, but easier to just fetch again
    fetchUsers();
  }
};

onMounted(() => {
  fetchUsers();
});
</script>

<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <h2 class="text-2xl font-bold text-primary">用户管理</h2>
      <el-button type="primary" @click="fetchUsers">刷新列表</el-button>
    </div>

    <el-card shadow="hover" class="!border-none">
      <el-table :data="users" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="用户">
          <template #default="scope">
            <div class="flex items-center gap-3">
              <el-avatar :size="32" class="bg-secondary text-primary">{{ scope.row.username[0].toUpperCase() }}</el-avatar>
              <div class="flex flex-col">
                <span class="font-medium text-primary">{{ scope.row.username }}</span>
                <span class="text-xs text-muted-foreground">{{ scope.row.email }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="role" label="角色" width="150">
          <template #default="scope">
            <el-select 
              v-model="scope.row.role" 
              size="small"
              @change="(val) => handleRoleChange(scope.row, val)"
              :disabled="scope.row.username === 'admin'"
            >
              <el-option label="普通用户" value="USER" />
              <el-option label="管理员" value="ADMIN" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" width="180">
          <template #default="scope">
            {{ new Date(scope.row.createdAt).toLocaleDateString() }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="scope">
            <el-button 
              type="danger" 
              size="small" 
              circle 
              :icon="Delete"
              :disabled="scope.row.username === 'admin'"
              @click="handleDelete(scope.row)"
            />
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>
