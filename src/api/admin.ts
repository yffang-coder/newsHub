import request from './request';

const ADMIN_URL = '/admin';

export const getDashboardStats = () => {
  return request.get(`${ADMIN_URL}/stats`);
};

export const getAllUsers = () => {
  return request.get(`${ADMIN_URL}/users`);
};

export const deleteUser = (id: number) => {
  return request.delete(`${ADMIN_URL}/users/${id}`);
};

export const updateUserRole = (id: number, role: string) => {
  return request.put(`${ADMIN_URL}/users/${id}/role`, { role });
};

export const getSettings = () => {
  return request.get(`${ADMIN_URL}/settings`);
};

export const updateSettings = (settings: any) => {
  return request.post(`${ADMIN_URL}/settings`, settings);
};

export const getComments = (page = 1, size = 10) => {
  return request.get(`${ADMIN_URL}/comments`, { params: { page, size } });
};

export const deleteComment = (id: number) => {
  return request.delete(`${ADMIN_URL}/comments/${id}`);
};

export const getArticles = (page = 1, size = 10, keyword?: string, startDate?: string, endDate?: string, sortField?: string, sortOrder?: string) => {
  return request.get(`${ADMIN_URL}/articles`, { params: { page, size, keyword, startDate, endDate, sortField, sortOrder } });
};

export const createArticle = (data: any) => {
  return request.post(`${ADMIN_URL}/articles`, data);
};

export const updateArticle = (id: number, data: any) => {
  return request.put(`${ADMIN_URL}/articles/${id}`, data);
};

export const getCategories = () => {
  return request.get(`${ADMIN_URL}/categories`);
};

export const deleteArticle = (id: number) => {
  return request.delete(`${ADMIN_URL}/articles/${id}`);
};

export const sendNotification = (data: { userId?: number, userIds?: number[], title: string, content: string, type: string, isGlobal?: boolean }) => {
  return request.post(`${ADMIN_URL}/notifications/send`, data);
};

export const getNotificationHistory = () => {
  return request.get(`${ADMIN_URL}/notifications`);
};

