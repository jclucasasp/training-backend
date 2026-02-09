import React from 'react';
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import ThreeBackground from './components/ThreeBackground';
import Dashboard from './pages/Dashboard';
import StaffManagement from './pages/StaffManagement';
import CourseManagement from './pages/CourseManagement';
import StudentManagement from './pages/StudentManagement';
import Settings from './pages/Settings';
import './App.css';

function App() {
  return (
    <Router>
      <ThreeBackground />
      <div className="app-container">
        <nav className="sidebar">
          <div className="sidebar-header">
            <h1>AR Admin</h1>
            <p>Backend Management</p>
          </div>
          <ul className="nav-links">
            <li>
              <Link to="/">Dashboard</Link>
            </li>
            <li>
              <Link to="/staff">Staff Management</Link>
            </li>
            <li>
              <Link to="/courses">Course Management</Link>
            </li>
            <li>
              <Link to="/students">Student Management</Link>
            </li>
            <li>
              <Link to="/settings">Settings</Link>
            </li>
          </ul>
        </nav>
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
    </Router>
  );
}

export default App;
