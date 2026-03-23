import axiosInstance from './axiosInstance';

const productService = {
  getAll: async () => {
    const response = await axiosInstance.get('/products');
    return response.data;
  },

  getById: async (id) => {
    const response = await axiosInstance.get(`/products/${id}`);
    return response.data;
  },

  create: async (data) => {
    const response = await axiosInstance.post('/products', data);
    return response.data;
  },

  update: async (id, data) => {
    const response = await axiosInstance.put(`/products/${id}`, data);
    return response.data;
  },

  delete: async (id) => {
    const response = await axiosInstance.delete(`/products/${id}`);
    return response.data;
  },

  search: async (query) => {
    const response = await axiosInstance.get('/products/search', {
      params: { q: query },
    });
    return response.data;
  },

  updatePrice: async (id, price) => {
    const response = await axiosInstance.patch(`/products/${id}/price`, { price });
    return response.data;
  },
};

export default productService;
