<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { User, ChatDotRound, ChatLineSquare } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import { getComments, addComment, type Comment } from '@/api/comment';

const props = defineProps<{
  articleId: number | string;
}>();

const currentUser = ref(localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')!) : null);
const commentContent = ref('');
const comments = ref<Comment[]>([]);
const loading = ref(false);
const replyTo = ref<Comment | null>(null);
const replyContent = ref('');

const fetchComments = async () => {
    loading.value = true;
    try {
        const res = await getComments(props.articleId);
        // Transform to display format
        const formatted = res.map((c: any) => ({
            ...c,
            user: c.username || 'User ' + c.userId,
            date: new Date(c.createdAt).toLocaleString(),
            likes: 0,
            replies: []
        }));
        
        comments.value = buildTree(formatted);
    } catch (e) {
        console.error(e);
    } finally {
        loading.value = false;
    }
};

const buildTree = (items: Comment[]) => {
    const map = new Map<number, Comment>();
    const roots: Comment[] = [];
    
    // Sort by ID/Date first ensures order
    items.sort((a, b) => a.id - b.id);

    items.forEach(item => {
        map.set(item.id, item);
        item.replies = [];
    });

    items.forEach(item => {
        if (item.parentId && map.has(item.parentId)) {
            map.get(item.parentId)!.replies!.push(item);
        } else {
            roots.push(item);
        }
    });
    
    return roots.reverse(); // Newest first
};

onMounted(() => {
    fetchComments();
});

const handleSubmit = async () => {
  if (!currentUser.value) {
    ElMessage.warning('请先登录后再评论');
    return;
  }
  
  if (!commentContent.value.trim()) {
    ElMessage.warning('请输入评论内容');
    return;
  }

  try {
      await addComment({
          articleId: props.articleId,
          content: commentContent.value,
          userId: currentUser.value.id
      });
      ElMessage.success('评论发布成功');
      commentContent.value = '';
      fetchComments();
  } catch (e) {
      ElMessage.error('评论发布失败');
  }
};

const handleReplySubmit = async (parentId: number) => {
    if (!currentUser.value) {
        ElMessage.warning('请先登录');
        return;
    }
    if (!replyContent.value.trim()) {
        ElMessage.warning('请输入回复内容');
        return;
    }
    
    try {
        await addComment({
            articleId: props.articleId,
            content: replyContent.value,
            userId: currentUser.value.id,
            parentId
        });
        ElMessage.success('回复成功');
        replyContent.value = '';
        replyTo.value = null;
        fetchComments();
    } catch (e) {
        ElMessage.error('回复失败');
    }
};
</script>

<template>
  <div class="mt-12 pt-8 border-t border-border">
    <h3 class="text-xl font-bold text-primary mb-6 flex items-center gap-2">
      <el-icon><ChatDotRound /></el-icon> 评论 ({{ comments.reduce((acc, c) => acc + 1 + (c.replies?.length || 0), 0) }})
    </h3>

    <!-- Main Comment Form -->
    <div class="mb-10 flex gap-4">
      <div class="w-10 h-10 rounded-full bg-secondary flex-shrink-0 flex items-center justify-center overflow-hidden">
        <el-icon v-if="!currentUser" :size="20" class="text-muted-foreground"><User /></el-icon>
        <span v-else class="font-bold text-primary">{{ currentUser.username?.[0] || 'U' }}</span>
      </div>
      <div class="flex-1">
        <el-input
          v-model="commentContent"
          type="textarea"
          :rows="3"
          :placeholder="currentUser ? '写下您的想法...' : '请先登录后发表评论'"
          resize="none"
          class="mb-3"
          :disabled="!currentUser"
        />
        <div class="flex justify-end">
          <el-button type="primary" :disabled="!currentUser" @click="handleSubmit">发布评论</el-button>
        </div>
      </div>
    </div>

    <!-- Comments List -->
    <div class="space-y-8">
      <div v-if="loading" class="text-center py-8 text-muted-foreground">加载中...</div>
      <div v-else-if="comments.length === 0" class="text-center py-8 text-muted-foreground">暂无评论，快来抢沙发吧！</div>
      
      <div v-for="comment in comments" :key="comment.id" class="flex gap-4">
        <div class="w-10 h-10 rounded-full bg-muted flex-shrink-0 flex items-center justify-center text-accent font-bold">
          {{ comment.user?.[0]?.toUpperCase() }}
        </div>
        <div class="flex-1">
          <div class="bg-secondary/30 rounded-lg p-4">
            <div class="flex justify-between items-start mb-2">
              <span class="font-bold text-primary text-sm">{{ comment.user }}</span>
              <span class="text-xs text-muted-foreground">{{ comment.date }}</span>
            </div>
            <p class="text-foreground/80 text-sm leading-relaxed">{{ comment.content }}</p>
            
            <div class="mt-3 flex gap-4">
              <button 
                class="text-xs font-medium text-muted-foreground hover:text-primary transition-colors flex items-center gap-1"
                @click="replyTo = replyTo?.id === comment.id ? null : comment"
              >
                <el-icon><ChatLineSquare /></el-icon> 回复
              </button>
            </div>

            <!-- Reply Form -->
            <div v-if="replyTo?.id === comment.id" class="mt-4 pl-4 border-l-2 border-primary/20">
               <el-input
                  v-model="replyContent"
                  type="textarea"
                  :rows="2"
                  placeholder="回复..."
                  class="mb-2"
                />
                <div class="flex justify-end gap-2">
                    <el-button size="small" @click="replyTo = null">取消</el-button>
                    <el-button type="primary" size="small" @click="handleReplySubmit(comment.id)">发表回复</el-button>
                </div>
            </div>
          </div>

          <!-- Replies -->
          <div v-if="comment.replies && comment.replies.length > 0" class="mt-4 space-y-4 pl-4 border-l-2 border-border ml-2">
             <div v-for="reply in comment.replies" :key="reply.id" class="flex gap-3">
                <div class="w-8 h-8 rounded-full bg-muted flex-shrink-0 flex items-center justify-center text-xs font-bold text-accent">
                    {{ reply.user?.[0]?.toUpperCase() }}
                </div>
                <div class="flex-1">
                    <div class="bg-secondary/20 rounded p-3">
                         <div class="flex justify-between items-start mb-1">
                            <span class="font-bold text-primary text-xs">{{ reply.user }}</span>
                            <span class="text-[10px] text-muted-foreground">{{ reply.date }}</span>
                        </div>
                        <p class="text-foreground/80 text-xs">{{ reply.content }}</p>
                    </div>
                </div>
             </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>