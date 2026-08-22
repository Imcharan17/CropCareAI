import { Outlet, NavLink } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { motion } from 'framer-motion';
import {
  Activity,
  BarChart3,
  FileText,
  Leaf,
  LogOut,
  Shield,
  Ticket,
  Upload,
  Users,
} from 'lucide-react';
import { logout } from '../features/auth/authSlice.js';

const links = {
  ROLE_FARMER: [
    ['/farmer', Activity, 'Dashboard'],
    ['/farmer/detect', Upload, 'Detect'],
    ['/farmer/tickets', Ticket, 'Tickets'],
    ['/farmer/history', FileText, 'History'],
    ['/profile', Users, 'Profile'],
  ],

  ROLE_DOCTOR: [
    ['/doctor', Activity, 'Dashboard'],
    ['/doctor/tickets', Ticket, 'Tickets'],
    ['/doctor/chat', FileText, 'Chat'],
    ['/doctor/analytics', BarChart3, 'Analytics'],
    ['/doctor/recommendations', FileText, 'Recommendations'],
  ],

  ROLE_ADMIN: [
    ['/admin', Shield, 'Dashboard'],
    ['/admin/users', Users, 'Users'],
    ['/admin/tickets', Ticket, 'Tickets'],
    ['/admin/reports', FileText, 'Reports'],
    ['/admin/analytics', BarChart3, 'Analytics'],
  ],
};

export default function AppShell() {
  const user = useSelector((state) => state.auth.user);
  const dispatch = useDispatch();
  const role = user?.roles?.[0] || 'ROLE_FARMER';

  return (
    <div className="min-h-screen p-3 md:p-5">
      <div className="mx-auto grid max-w-7xl gap-4 lg:grid-cols-[260px_1fr]">
        <aside className="glass rounded-lg p-4 lg:sticky lg:top-5 lg:h-[calc(100vh-40px)]">
          <div className="flex items-center gap-3 border-b border-white/10 pb-4">
            <div className="grid h-11 w-11 place-items-center rounded-md bg-field text-ink">
              <Leaf />
            </div>

            <div>
              <p className="text-sm text-white/60">AI Powered</p>
              <h1 className="text-lg font-bold">CropCare Portal</h1>
            </div>
          </div>

          <nav className="mt-5 grid gap-2">
            {links[role].map(([to, Icon, label]) => (
              <NavLink
                key={to}
                to={to}
                className={({ isActive }) =>
                  `flex items-center gap-3 rounded-md px-3 py-2 text-sm ${
                    isActive
                      ? 'bg-field text-ink'
                      : 'text-white/72 hover:bg-white/10'
                  }`
                }
              >
                <Icon size={18} />
                {label}
              </NavLink>
            ))}
          </nav>

          <button
            className="btn-ghost mt-5 w-full"
            onClick={() => dispatch(logout())}
          >
            <LogOut size={17} />
            Logout
          </button>
        </aside>

        <motion.main
          initial={{ opacity: 0, y: 12 }}
          animate={{ opacity: 1, y: 0 }}
          className="min-w-0"
        >
          <header className="mb-4 flex flex-col justify-between gap-3 rounded-lg border border-white/10 bg-white/5 p-4 md:flex-row md:items-center">
            <div>
              <p className="text-sm text-emerald-200">
                Secure agriculture intelligence workspace
              </p>
              <h2 className="text-2xl font-semibold">
                {user?.fullName}
              </h2>
            </div>

            <span className="w-fit rounded-md border border-field/30 px-3 py-1 text-sm text-field">
              {role.replace('ROLE_', '')}
            </span>
          </header>

          <Outlet />
        </motion.main>
      </div>
    </div>
  );
}