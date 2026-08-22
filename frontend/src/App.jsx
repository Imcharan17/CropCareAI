import { Navigate, Route, Routes } from "react-router-dom";
import { useSelector } from "react-redux";

import AppShell from "./layout/AppShell.jsx";

import Login from "./pages/auth/Login.jsx";
import Register from "./pages/auth/Register.jsx";
import ForgotPassword from "./pages/auth/ForgotPassword.jsx";

import FarmerDashboard from "./pages/farmer/FarmerDashboard.jsx";
import DiseaseDetection from "./pages/farmer/DiseaseDetection.jsx";
import TicketTracking from "./pages/farmer/TicketTracking.jsx";
import DiseaseHistory from "./pages/farmer/DiseaseHistory.jsx";
import Profile from "./pages/farmer/Profile.jsx";

import DoctorDashboard from "./pages/doctor/DoctorDashboard.jsx";
import AssignedTickets from "./pages/doctor/AssignedTickets.jsx";
import Chat from "./pages/doctor/Chat.jsx";
import Analytics from "./pages/doctor/Analytics.jsx";
import Recommendations from "./pages/doctor/Recommendations.jsx";

import AdminDashboard from "./pages/admin/AdminDashboard.jsx";
import UserManagement from "./pages/admin/UserManagement.jsx";
import TicketManagement from "./pages/admin/TicketManagement.jsx";
import Reports from "./pages/admin/Reports.jsx";

function Protected({ children, role }) {
  const user = useSelector((state) => state.auth.user);

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  if (role && !user.roles?.includes(role)) {
    return <Navigate to="/" replace />;
  }

  return children;
}

export default function App() {
  return (
    <Routes>
      {/* Public Routes */}
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route path="/forgot-password" element={<ForgotPassword />} />

      {/* Protected Routes */}
      <Route
        path="/"
        element={
          <Protected>
            <AppShell />
          </Protected>
        }
      >
        <Route index element={<RoleHome />} />

        {/* Farmer */}
        <Route path="farmer" element={<FarmerDashboard />} />
        <Route path="farmer/detect" element={<DiseaseDetection />} />
        <Route path="farmer/tickets" element={<TicketTracking />} />
        <Route path="farmer/history" element={<DiseaseHistory />} />
        <Route path="farmer/chat" element={<Chat />} />
        <Route path="profile" element={<Profile />} />

        {/* Doctor */}
        <Route path="doctor" element={<DoctorDashboard />} />
        <Route path="doctor/tickets" element={<AssignedTickets />} />
        <Route path="doctor/chat" element={<Chat />} />
        <Route path="doctor/analytics" element={<Analytics />} />
        <Route
          path="doctor/recommendations"
          element={<Recommendations />}
        />

        {/* Admin */}
        <Route path="admin" element={<AdminDashboard />} />
        <Route path="admin/users" element={<UserManagement />} />
        <Route path="admin/tickets" element={<TicketManagement />} />
        <Route path="admin/reports" element={<Reports />} />
        <Route path="admin/analytics" element={<Analytics />} />
      </Route>
    </Routes>
  );
}

function RoleHome() {
  const user = useSelector((state) => state.auth.user);

  if (user?.roles?.includes("ROLE_ADMIN")) {
    return <Navigate to="/admin" replace />;
  }

  if (user?.roles?.includes("ROLE_DOCTOR")) {
    return <Navigate to="/doctor" replace />;
  }

  return <Navigate to="/farmer" replace />;
}