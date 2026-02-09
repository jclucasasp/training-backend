// Course Types
export interface CourseResponse {
  id: number;
  name: string;
  description: string;
  difficulty: string;
  imageUrl?: string;
  tags: string;
}

export interface SectionRequest {
  title: string;
  content: string;
  duration: number;
  orderIndex: number;
  resourceUrl?: string;
  resourceMediaType?: string;
  tags: string;
}

export interface ModuleRequest {
  name: string;
  description: string;
  sections: SectionRequest[];
}

export interface CourseCreateRequest {
  name: string;
  description: string;
  difficultyTypes: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';
  tags: string;
  imageUrl?: string;
  modules: ModuleRequest[];
}

export interface CourseUpdateRequest {
  name?: string;
  description?: string;
  difficultyTypes?: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';
  tags?: string;
  imageUrl?: string;
  modules?: ModuleRequest[];
}

// Staff Types
export interface StaffResponse {
  id: number;
  email: string;
  role: string;
  isActive: boolean;
}

export interface CreateStaffRequest {
  email: string;
  password: string;
  role: string;
  isActive: boolean;
}

// Organisation Types
export interface OrganisationResponse {
  id: number;
  email: string;
  orgName: string;
  registrationNumber?: string;
  vatNumber?: string;
  apiKey?: string;
  orgSignedUpDate: string;
  orgLastUpdated: string;
  orgDeletedDate?: string;
  subscriptionPlan: string;
  subscriptionStartDate: string;
  subscriptionStatus: boolean;
  subscriptionEndDate: string;
}

export interface ProfileRequest {
  registrationNumber?: string;
  vatNumber?: string;
  displayName?: string;
}

export interface ApiKeyResponse {
  apiKey: string;
}

// Student Types
export interface StudentResponse {
  id: number;
  studentNumber: string;
  firstName: string;
  lastName: string;
}

export interface StudentEnrollRequest {
  studentNumber: string;
  firstName: string;
  lastName: string;
  courseId: number;
}

export interface EnrollmentResponse {
  studentId: number;
  courseId: number;
  enrollmentDate: string;
}

// API Response Types
export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

// Error Types
export interface ApiError {
  message: string;
  status: number;
  timestamp: string;
}
