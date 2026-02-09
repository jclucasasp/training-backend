# AR Backend Admin Dashboard

A modern admin dashboard for managing AR Backend services, built with React, TypeScript, and React Three Fiber.

## Features

- **Dashboard**: Overview with statistics and quick actions
- **Staff Management**: Create, update, and manage staff members
- **Course Management**: Full CRUD operations for courses with nested modules and sections
- **Student Management**: Enroll and manage students
- **Settings**: Manage organization profile and API keys
- **3D Background**: Interactive particle system using React Three Fiber
- **Modern UI**: Built with shadcn/ui components and Tailwind CSS
- **Type Safety**: Fully typed with TypeScript

## Tech Stack

- **React 18.2.0** - UI library
- **TypeScript 5.3.3** - Type safety
- **React Three Fiber 8.15.11** - 3D rendering
- **@react-three/drei 9.88.13** - 3D helpers
- **Tailwind CSS 3.3.6** - Styling
- **shadcn/ui** - UI components
- **Axios 1.6.2** - HTTP client
- **React Router DOM 6.20.1** - Routing
- **Lucide React** - Icons

## Prerequisites

- Node.js 18+ 
- npm or yarn
- Access to the AR Backend API

## Installation

1. Install dependencies:
```bash
npm install
```

2. Create a `.env` file in the root directory:
```env
REACT_APP_API_URL=http://localhost:8080
```

3. Start the development server:
```bash
npm start
```

The application will open at [http://localhost:3000](http://localhost:3000)

## Project Structure

```
frontend/
├── public/              # Static files
├── src/
│   ├── components/      # Reusable components
│   │   ├── ui/        # shadcn/ui components
│   │   └── ThreeBackground.tsx
│   ├── pages/         # Page components
│   │   ├── Dashboard.tsx
│   │   ├── StaffManagement.tsx
│   │   ├── CourseManagement.tsx
│   │   ├── StudentManagement.tsx
│   │   └── Settings.tsx
│   ├── services/      # API services
│   │   └── api.ts
│   ├── types/         # TypeScript type definitions
│   │   └── api.ts
│   ├── lib/           # Utility functions
│   │   └── utils.ts
│   ├── App.tsx        # Main app component
│   └── index.tsx      # Entry point
├── package.json
├── tsconfig.json
├── tailwind.config.js
└── postcss.config.js
```

## API Integration

The dashboard integrates with the AR Backend API using the following endpoints:

### Staff Management
- `GET /api/v1/admin/staff/all` - List all staff
- `POST /api/v1/admin/staff/create` - Create staff member
- `PUT /api/v1/admin/staff/update` - Update staff member

### Course Management
- `GET /api/v1/courses` - List all courses
- `POST /api/v1/admin/course/create` - Create course
- `PUT /api/v1/admin/course/update/{id}` - Update course

### Student Management
- `GET /api/v1/students/org/{orgId}` - List students
- `POST /api/v1/students/org/{orgId}/enroll` - Enroll student

### Organization
- `GET /api/v1/organisations/details` - Get organization details
- `PUT /api/v1/organisations/profile` - Update profile
- `POST /api/v1/organisations/api-keys` - Generate API key

## Authentication

The dashboard uses API key authentication. The API key should be stored in localStorage with the key `apiKey`. You can generate a new API key in the Settings page.

## Building for Production

```bash
npm run build
```

The optimized build will be in the `build` directory.

## License

Proprietary
