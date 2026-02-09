import axios, { AxiosInstance, AxiosError } from 'axios';
import type {
  StaffResponse,
  CreateStaffRequest,
  CourseResponse,
  CourseCreateRequest,
  CourseUpdateRequest,
  OrganisationResponse,
  ProfileRequest,
  ApiKeyResponse,
  StudentResponse,
  StudentEnrollRequest,
  EnrollmentResponse,
  PaginatedResponse,
  ApiError
} from '@/types/api';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

const api: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add API key to requests
api.interceptors.request.use((config) => {
  const apiKey = localStorage.getItem('apiKey');
  if (apiKey) {
    config.headers['X-API-KEY'] = apiKey;
  }
  return config;
});

// Error handler
api.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ApiError>) => {
    if (error.response) {
      // Server responded with error status
      console.error('API Error:', error.response.data);
    } else if (error.request) {
      // Request made but no response received
      console.error('Network Error:', error.message);
    } else {
      // Error in request configuration
      console.error('Request Error:', error.message);
    }
    return Promise.reject(error);
  }
);

// Staff Management
export const staffApi = {
  getAll: async (page = 0, size = 10): Promise<PaginatedResponse<StaffResponse>> => {
    const response = await api.get(`/api/v1/admin/staff/all?page=${page}&size=${size}`);
    return response.data;
  },

  create: async (data: CreateStaffRequest): Promise<StaffResponse> => {
    const response = await api.post('/api/v1/admin/staff/create', data);
    return response.data;
  },

  update: async (staffId: number, data: CreateStaffRequest): Promise<StaffResponse> => {
    const response = await api.put(`/api/v1/admin/staff/update?staffId=${staffId}`, data);
    return response.data;
  },
};

// Course Management
export const courseApi = {
  getAll: async (page = 0, size = 10, sort = 'id'): Promise<PaginatedResponse<CourseResponse>> => {
    const response = await api.get(`/api/v1/courses?page=${page}&size=${size}&sort=${sort}`);
    return response.data;
  },

  create: async (data: CourseCreateRequest): Promise<CourseResponse> => {
    const response = await api.post('/api/v1/admin/course/create', data);
    return response.data;
  },

  update: async (courseId: number, data: CourseUpdateRequest): Promise<CourseResponse> => {
    const response = await api.put(`/api/v1/admin/course/update/${courseId}`, data);
    return response.data;
  },
};

// Organisation Management
export const orgApi = {
  getDetails: async (): Promise<OrganisationResponse> => {
    const response = await api.get('/api/v1/organisations/details');
    return response.data;
  },

  updateProfile: async (data: ProfileRequest): Promise<void> => {
    await api.put('/api/v1/organisations/profile', data);
  },

  generateApiKey: async (): Promise<ApiKeyResponse> => {
    const response = await api.post('/api/v1/organisations/api-keys');
    return response.data;
  },
};

// Student Management
export const studentApi = {
  getAll: async (orgId: number, page = 0, size = 10): Promise<PaginatedResponse<StudentResponse>> => {
    const response = await api.get(`/api/v1/students/org/${orgId}?page=${page}&size=${size}`);
    return response.data;
  },

  enroll: async (orgId: number, data: StudentEnrollRequest): Promise<EnrollmentResponse> => {
    const response = await api.post(`/api/v1/students/org/${orgId}/enroll`, data);
    return response.data;
  },
};

export default api;
