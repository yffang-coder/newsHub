import request from './request';

export interface Comment {
  id: number;
  content: string;
  articleId: number;
  userId: number;
  parentId?: number;
  createdAt: string;
  // Transient fields from backend or enriched in frontend
  user?: string; // Username from backend
  avatar?: string;
  date?: string; // Formatted date
  likes?: number;
  replies?: Comment[];
}

export const getComments = async (articleId: number | string) => {
  try {
    const res = await request.get(`/comments/article/${articleId}`) as any;
    if (Array.isArray(res)) {
        // Backend returns flat list, frontend can organize into tree if needed
        // But for simplicity, we might want backend to return tree or sort it here
        return res;
    }
    return [];
  } catch (error) {
    console.error(`Error fetching comments for article ${articleId}:`, error);
    return [];
  }
};

export const addComment = async (data: { articleId: number | string, content: string, userId: number, parentId?: number }) => {
    try {
        const res = await request.post('/comments', data);
        return res;
    } catch (error) {
        console.error('Error adding comment:', error);
        throw error;
    }
};
