import request from './request';

export const login = (data: any) => {
  return request({
    url: '/auth/signin',
    method: 'post',
    data
  });
};

export const loginByCode = (data: any) => {
  return request({
    url: '/auth/signin-by-code',
    method: 'post',
    data
  });
};

export const sendVerifyCode = (email: string) => {
  return request({
    url: '/auth/send-code',
    method: 'post',
    params: { email }
  });
};

export const register = (data: any) => {
  return request({
    url: '/auth/signup',
    method: 'post',
    data
  });
};

export const completeSignup = (data: any) => {
  return request({
    url: '/auth/complete-signup',
    method: 'post',
    data
  });
};

export const resetPassword = (data: any) => {
  return request({
    url: '/auth/reset-password',
    method: 'post',
    data
  });
};
