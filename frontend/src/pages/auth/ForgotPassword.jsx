import { useState } from 'react';
import { Mail } from 'lucide-react';
import { api } from '../../api/client.js';
import { getApiErrorMessage, useNotification } from '../../components/NotificationProvider.jsx';
import { AuthFrame } from './Login.jsx';
import { useLanguage } from '../../i18n/LanguageContext.jsx';

export default function ForgotPassword() {
  const { t } = useLanguage();
  const [email, setEmail] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const { notify } = useNotification();

  async function submit(event) {
    event.preventDefault();
    setIsSubmitting(true);
    try {
      await api.post('/auth/forgot-password', { email });
      notify('If the email exists, a reset token has been sent.', 'success');
    } catch (error) {
      notify(getApiErrorMessage(error, 'Unable to start password recovery. Please try again.'), 'error');
    } finally {
      setIsSubmitting(false);
    }
  }

  return <AuthFrame title="Reset access" subtitle="Recover access through the secure mail workflow">
    <form onSubmit={submit} className="grid gap-3">
      <input className="input" placeholder={t('Registered email')} value={email} onChange={(event) => setEmail(event.target.value)} />
      <button className="btn" disabled={isSubmitting}><Mail size={17} /> {isSubmitting ? t('Sending...') : t('Send reset token')}</button>
    </form>
  </AuthFrame>;
}
