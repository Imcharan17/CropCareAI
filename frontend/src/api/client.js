import axios from 'axios';

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '/api',
});

api.interceptors.request.use((config) => {
  const auth = JSON.parse(localStorage.getItem('crop-auth') || 'null');
  if (auth?.token) config.headers.Authorization = `Bearer ${auth.token}`;
  return config;
});
