import axiosInstance from './axiosInstance';

const systemService = {
  ping: async () => {
    const response = await axiosInstance.get('/ping');
    return response.data;
  },
};

export default systemService;
