import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useDispatch } from 'react-redux';
import { motion } from 'framer-motion';
import { Leaf, LogIn } from 'lucide-react';
import { api } from '../../api/client.js';
import { setCredentials } from '../../features/auth/authSlice.js';

export default function Login() {
  const [form, setForm] = useState({ email: 'admin@crop.ai', password: 'Password@123' });
  const [error, setError] = useState('');
  const dispatch = useDispatch();
  const navigate = useNavigate();
  async function submit(event) {
    event.preventDefault();
    try {
      const { data } = await api.post('/auth/login', form);
      dispatch(setCredentials(data));
      navigate('/');
    } catch {
      setError('Invalid credentials or blocked account');
    }
  }
  return <AuthFrame title="Welcome back" subtitle="Secure access for admins, farmers and agriculture experts">
    <form onSubmit={submit} className="grid gap-3">
      <input className="input" placeholder="Email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} />
      <input className="input" placeholder="Password" type="password" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} />
      {error && <p className="text-sm text-red-300">{error}</p>}
      <button className="btn"><LogIn size={17} /> Login</button>
      <div className="flex justify-between text-sm text-white/60"><Link to="/register">Create farmer account</Link><Link to="/forgot-password">Forgot password</Link></div>
    </form>
  </AuthFrame>;
}

export function AuthFrame({ title, subtitle, children }) {
  return (
    <main className="grid min-h-screen place-items-center p-4">
      <motion.section initial={{ opacity: 0, scale: .98 }} animate={{ opacity: 1, scale: 1 }} className="glass w-full max-w-md rounded-lg p-7">
        <div className="mb-6 flex items-center gap-3">
          <div className="grid h-12 w-12 place-items-center rounded-md bg-field text-ink"><Leaf /></div>
          <div><h1 className="text-2xl font-bold">{title}</h1><p className="text-sm text-white/60">{subtitle}</p></div>
        </div>
        {children}
      </motion.section>
    </main>
  );
}
