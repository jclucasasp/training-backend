import React from 'react';
import { BrowserRouter as Router, Routes, Route, Link, useLocation } from 'react-router-dom';
import ThreeBackground from './components/ThreeBackground';
import Dashboard from './pages/Dashboard';
import StaffManagement from './pages/StaffManagement';
import CourseManagement from './pages/CourseManagement';
import StudentManagement from './pages/StudentManagement';
import Settings from './pages/Settings';
import { Toaster } from './components/ui/toaster';
import { 
  LayoutDashboard, 
  Users, 
  BookOpen, 
  GraduationCap, 
  Settings as SettingsIcon 
} from 'lucide-react';
import './App.css';

function Sidebar() {
  const location = useLocation();

  const navItems = [
    { path: '/', label: 'Dashboard', icon: LayoutDashboard },
    { path: '/staff', label: 'Staff Management', icon: Users },
    { path: '/courses', label: 'Course Management', icon: BookOpen },
    { path: '/students', label: 'Student Management', icon: GraduationCap },
    { path: '/settings', label: 'Settings', icon: SettingsIcon },
  ];

  return (
    <nav className="sidebar">
      <div className="sidebar-header">
        <h1 className="text-2xl font-bold">AR Admin</h1>
        <p className="text-sm text-muted-foreground">Backend Management</p>
      </div>
      <ul className="nav-links">
        {navItems.map((item) => {
          const Icon = item.icon;
          const isActive = location.pathname === item.path;
          return (
            <li key={item.path}>
              <Link 
                to={item.path} 
                className={`nav-link ${isActive ? 'active' : ''}`}
              >
                <Icon className="h-4 w-4" />
                <span>{item.label}</span>
              </Link>
            </li>
          );
        })}
      </ul>
    </nav>
  );
}

function App() {
  return (
    <Router>
      <ThreeBackground />
      <div className="app-container">
        <Sidebar />
        <main className="main-content">
          <Routes>
            <Route path="/" element={<Dashboard />} />
            <Route path="/staff" element={<StaffManagement />} />
            <Route path="/courses" element={<CourseManagement />} />
            <Route path="/students" element={<StudentManagement />} />
            <Route path="/settings" element={<Settings />} />
          </Routes>
        </main>
      </div>
      <Toaster />
    </Router>
  );
}

export default App;
