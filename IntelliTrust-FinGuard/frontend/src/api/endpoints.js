import client from './client';

export const authApi = {
  register: (payload) => client.post('/api/auth/register', payload),
  login: (payload) => client.post('/api/auth/login', payload),
  me: () => client.get('/api/auth/me')
};

export const dashboardApi = {
  get: () => client.get('/api/dashboard')
};

export const transactionsApi = {
  list: (params) => client.get('/api/transactions', { params }),
  create: (payload) => client.post('/api/transactions', payload),
  remove: (id) => client.delete(`/api/transactions/${id}`),
  importCsv: (file) => {
    const formData = new FormData();
    formData.append('file', file);
    return client.post('/api/transactions/import', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
  },
  simulate: (payload) => client.post('/api/transactions/simulate', payload)
};

export const fraudApi = {
  explain: (transactionId) => client.get(`/api/fraud/explain/${transactionId}`),
  summary: () => client.get('/api/fraud/summary')
};

export const forecastApi = {
  get: () => client.get('/api/forecast')
};

export const behaviourApi = {
  submitEvent: (payload) => client.post('/api/behaviour/event', payload),
  status: () => client.get('/api/behaviour/status')
};

export const locationApi = {
  submit: (payload) => client.post('/api/location', payload),
  latest: () => client.get('/api/location/latest')
};

export const trustScoreApi = {
  get: () => client.get('/api/trust-score')
};

export const alertsApi = {
  list: () => client.get('/api/alerts'),
  resolve: (id) => client.put(`/api/alerts/${id}/resolve`)
};

export const demoApi = {
  reset: () => client.post('/api/demo/reset')
};
