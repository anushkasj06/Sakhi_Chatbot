import axiosInstance from './axiosInstance';

const authService = {
  login: async (credentials) => {
    const response = await axiosInstance.post('/auth/login', credentials);
    return response.data;
  },

  customerSignup: async (data) => {
    const response = await axiosInstance.post('/auth/customer/signup', data);
    return response.data;
  },

  logout: async (refreshToken) => {
    const response = await axiosInstance.post('/auth/logout', { refreshToken });
    return response.data;
  },

  verifyEmail: async (token) => {
    const response = await axiosInstance.post('/auth/verify-email', { token });
    return response.data;
  },

  forgotPassword: async (email) => {
    const response = await axiosInstance.post('/auth/forgot-password', { email });
    return response.data;
  },

  resetPassword: async (data) => {
    const response = await axiosInstance.post('/auth/reset-password', data);
    return response.data;
  },
};

export default authService;
