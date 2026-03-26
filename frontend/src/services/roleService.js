import axiosInstance from './axiosInstance';

const roleService = {
  getAll: async () => {
    const response = await axiosInstance.get('/roles');
    return response.data;
  },

  create: async (data) => {
    const response = await axiosInstance.post('/roles', data);
    return response.data;
  },
};

export default roleService;
