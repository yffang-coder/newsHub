import request from './request';

export const getPublicSettings = () => {
  return request.get('/public/settings');
};

export const getPublicSetting = (key: string) => {
  return request.get(`/public/settings/${key}`);
};
