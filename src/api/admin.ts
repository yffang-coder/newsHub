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

export const getArticles = (page = 1, size = 10, keyword?: string) => {
  return request.get(`${ADMIN_URL}/articles`, { params: { page, size, keyword } });
};

export const deleteArticle = (id: number) => {
  return request.delete(`${ADMIN_URL}/articles/${id}`);
};
