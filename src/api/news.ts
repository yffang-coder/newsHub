import request from './request';

const categoryNames: Record<number, string> = {
  1: '时事',
  2: '国际',
  3: '科技',
  4: '体育',
  5: '财经',
  6: '文化'
};

export const getCategoryName = (id: number) => categoryNames[id] || '新闻';

export interface NewsItem {
  id: number;
  title: string;
  excerpt?: string;
  category: string;
  categoryId?: number;
  date: string;
  author?: string;
  image?: string;
}

export interface ArticleDetail extends NewsItem {
  content: string;
  authorRole?: string;
  time?: string;
}

// Helper to format date
const formatDate = (dateStr: string) => {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    const now = new Date();
    const diff = now.getTime() - date.getTime();
    
    // If less than 24 hours
    if (diff < 24 * 60 * 60 * 1000) {
        const hours = Math.floor(diff / (60 * 60 * 1000));
        if (hours < 1) return '刚刚';
        return `${hours}小时前`;
    }
    return date.toLocaleDateString();
}

export const getFeaturedNews = async () => {
  try {
    const res = await request.get('/news/latest?limit=1') as any;
    if (res && res.length > 0) {
        const item = res[0];
        return {
            id: item.id,
            title: item.title,
            excerpt: item.summary,
            category: getCategoryName(item.categoryId),
            categoryId: item.categoryId,
            date: formatDate(item.publishTime),
            author: item.sourceName || 'NewsHub',
            image: item.coverImage
        } as NewsItem;
    }
    return null;
  } catch (error) {
    console.error('Error fetching featured news:', error);
    return null;
  }
};

export const getTrendingNews = async () => {
  try {
    const res = await request.get('/news/latest?limit=5') as any;
    if (Array.isArray(res)) {
      return res.map((item: any) => ({
          id: item.id,
          title: item.title,
          excerpt: item.summary,
          category: getCategoryName(item.categoryId),
          categoryId: item.categoryId,
          date: formatDate(item.publishTime),
          author: item.sourceName || 'NewsHub',
          image: item.coverImage
      })) as NewsItem[];
    }
    return [];
  } catch (error) {
    console.error('Error fetching trending news:', error);
    return [];
  }
};

export const getLatestNews = async () => {
  try {
    const res = await request.get('/news/latest?limit=10') as any;
    if (Array.isArray(res)) {
      return res.map((item: any) => ({
          id: item.id,
          title: item.title,
          excerpt: item.summary,
          category: getCategoryName(item.categoryId),
          categoryId: item.categoryId,
          date: formatDate(item.publishTime),
          author: item.sourceName || 'NewsHub',
          image: item.coverImage
      })) as NewsItem[];
    }
    return [];
  } catch (error) {
    console.error('Error fetching latest news:', error);
    return [];
  }
};

export const getArticleDetail = async (id: number | string) => {
  try {
    const res = await request.get(`/news/${id}`) as any;
    return {
        id: res.id,
        title: res.title,
        excerpt: res.summary,
        category: getCategoryName(res.categoryId),
        categoryId: res.categoryId,
        date: new Date(res.publishTime).toLocaleDateString(),
        time: new Date(res.publishTime).toLocaleTimeString(),
        author: res.sourceName || 'NewsHub',
        authorRole: '记者',
        image: res.coverImage,
        content: res.content || res.summary // Fallback to summary if content is empty
    } as ArticleDetail;
  } catch (error) {
    console.error(`Error fetching article ${id}:`, error);
    throw error;
  }
};

export const searchNews = async (keyword: string) => {
  try {
    const res = await request.get(`/news/search?q=${encodeURIComponent(keyword)}`) as any;
    if (Array.isArray(res)) {
      return res.map((item: any) => ({
          id: item.id,
          title: item.title,
          excerpt: item.summary,
          category: getCategoryName(item.categoryId),
          categoryId: item.categoryId,
          date: formatDate(item.publishTime),
          author: item.sourceName || 'NewsHub',
          image: item.coverImage
      })) as NewsItem[];
    }
    return [];
  } catch (error) {
    console.error(`Error searching news:`, error);
    return [];
  }
};

export const getNbaNews = async () => {
  try {
    const res = await request.get('/news/category/4?limit=4') as any;
    if (Array.isArray(res)) {
      return res.map((item: any) => ({
          id: item.id,
          title: item.title,
          excerpt: item.summary,
          category: 'NBA',
          categoryId: 4,
          date: formatDate(item.publishTime),
          author: item.sourceName || 'NewsHub',
          image: item.coverImage
      })) as NewsItem[];
    }
    return [];
  } catch (error) {
    console.error('Error fetching NBA news:', error);
    return [];
  }
};

export const getNewsByCategory = async (categoryId: number, _page: number = 1, pageSize: number = 20) => {
  try {
    // Backend likely supports pagination, or at least limit
    // Assuming /news/category/{id}?page={page}&limit={limit}
    // If backend doesn't support pagination params on this endpoint, we might just get a list.
    // Based on getNbaNews, it uses limit.
    const res = await request.get(`/news/category/${categoryId}?limit=${pageSize}`) as any;
    if (Array.isArray(res)) {
      return res.map((item: any) => ({
          id: item.id,
          title: item.title,
          excerpt: item.summary,
          category: getCategoryName(categoryId),
          categoryId: categoryId,
          date: formatDate(item.publishTime),
          author: item.sourceName || 'NewsHub',
          image: item.coverImage
      })) as NewsItem[];
    }
    return [];
  } catch (error) {
    console.error(`Error fetching news for category ${categoryId}:`, error);
    return [];
  }
};

export const getRelatedNews = async (id: number | string) => {
  try {
    const res = await request.get(`/news/${id}/related?limit=5`) as any;
    if (Array.isArray(res)) {
      return res.map((item: any) => ({
          id: item.id,
          title: item.title,
          excerpt: item.summary,
          category: getCategoryName(item.categoryId),
          categoryId: item.categoryId,
          date: formatDate(item.publishTime),
          author: item.sourceName || 'NewsHub',
          image: item.coverImage
      })) as NewsItem[];
    }
    return [];
  } catch (error) {
    console.error(`Error fetching related news for article ${id}:`, error);
    return [];
  }
};
