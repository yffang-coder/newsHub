import request from './request';

export interface Notification {
  id: number;
  userId: number;
  title: string;
  content: string;
  type: string;
  isRead: boolean;
  relatedId?: number;
  createdAt: string;
}

export const getNotifications = async () => {
  return request.get('/notifications');
};

export const getUnreadCount = async () => {
  return request.get('/notifications/unread-count');
};

export const markAsRead = async (id: number) => {
  return request.put(`/notifications/${id}/read`);
};

export const markAllAsRead = async () => {
  return request.put('/notifications/read-all');
};
