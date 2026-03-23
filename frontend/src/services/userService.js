import axiosInstance from './axiosInstance';

const userService = {
  getAll: async () => {
    const response = await axiosInstance.get('/users');
    return response.data;
  },

  getById: async (id) => {
    const response = await axiosInstance.get(`/users/${id}`);
    return response.data;
  },

  create: async (data) => {
    const response = await axiosInstance.post('/users', data);
    return response.data;
  },

  update: async (id, data) => {
    const response = await axiosInstance.put(`/users/${id}`, data);
    return response.data;
  },

  delete: async (id) => {
    const response = await axiosInstance.delete(`/users/${id}`);
    return response.data;
  },

  updateStatus: async (id, status) => {
    const response = await axiosInstance.patch(`/users/${id}/status`, { status });
    return response.data;
  },

  assignRole: async (id, roleName) => {
    const response = await axiosInstance.post(`/users/${id}/roles`, { roleName });
    return response.data;
  },

  removeRole: async (userId, roleId) => {
    const response = await axiosInstance.delete(`/users/${userId}/roles/${roleId}`);
    return response.data;
  },
};

export default userService;
