import axiosInstance from './axiosInstance';

const warehouseService = {
  getAll: async () => {
    const response = await axiosInstance.get('/warehouses');
    return response.data;
  },

  getById: async (id) => {
    const response = await axiosInstance.get(`/warehouses/${id}`);
    return response.data;
  },

  create: async (data) => {
    const response = await axiosInstance.post('/warehouses', data);
    return response.data;
  },

  update: async (id, data) => {
    const response = await axiosInstance.put(`/warehouses/${id}`, data);
    return response.data;
  },

  delete: async (id) => {
    const response = await axiosInstance.delete(`/warehouses/${id}`);
    return response.data;
  },

  assignManager: async (id, managerId) => {
    const response = await axiosInstance.patch(`/warehouses/${id}/manager`, { managerId });
    return response.data;
  },

  getManager: async (id) => {
    const response = await axiosInstance.get(`/warehouses/${id}/manager`);
    return response.data;
  },
};

export default warehouseService;
