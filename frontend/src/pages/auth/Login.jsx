import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useDispatch } from 'react-redux';
import { motion } from 'framer-motion';
import { Leaf, LogIn } from 'lucide-react';
import { api } from '../../api/client.js';
import { getApiErrorMessage, useNotification } from '../../components/NotificationProvider.jsx';
import { setCredentials } from '../../features/auth/authSlice.js';
import LanguageSwitcher from '../../components/LanguageSwitcher.jsx';
import { useLanguage } from '../../i18n/LanguageContext.jsx';

export default function Login() {
  const { t } = useLanguage();
  const [form, setForm] = useState({ email: '', password: '' });
  const [isSubmitting, setIsSubmitting] = useState(false);
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { notify } = useNotification();
  async function submit(event) {
    event.preventDefault();
    setIsSubmitting(true);
    try {
      const { data } = await api.post('/auth/login', form);
      dispatch(setCredentials(data));
      navigate('/');
    } catch (error) {
      notify(getApiErrorMessage(error, t('Invalid credentials or blocked account.')), 'error');
    } finally {
      setIsSubmitting(false);
    }
  }
  return <AuthFrame title="Welcome back" subtitle="Secure access for admins, farmers and agriculture experts">
    <form onSubmit={submit} className="grid gap-3">
      <input className="input" placeholder={t('Email')} value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} />
      <input className="input" placeholder={t('Password')} type="password" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} />
      <button className="btn" disabled={isSubmitting}><LogIn size={17} /> {isSubmitting ? t('Signing in...') : t('Login')}</button>
      <div className="flex justify-between text-sm text-white/60"><Link to="/register">{t('Create farmer account')}</Link><Link to="/forgot-password">{t('Forgot password')}</Link></div>
    </form>
  </AuthFrame>;
}

export function AuthFrame({ title, subtitle, children }) {
  const { t } = useLanguage();
  return (
    <main className="relative grid min-h-screen place-items-center p-4">
      <div className="absolute right-4 top-4"><LanguageSwitcher /></div>
      <motion.section initial={{ opacity: 0, scale: .98 }} animate={{ opacity: 1, scale: 1 }} className="glass w-full max-w-md rounded-lg p-7">
        <div className="mb-6 flex items-center gap-3">
          <div className="grid h-12 w-12 place-items-center rounded-md bg-field text-ink"><Leaf /></div>
          <div><h1 className="text-2xl font-bold">{t(title)}</h1><p className="text-sm text-white/60">{t(subtitle)}</p></div>
        </div>
        {children}
      </motion.section>
    </main>
  );
}
