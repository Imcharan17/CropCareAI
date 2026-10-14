import { useState } from 'react';
import { useDispatch } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import { api } from '../../api/client.js';
import { getApiErrorMessage, useNotification } from '../../components/NotificationProvider.jsx';
import { setCredentials } from '../../features/auth/authSlice.js';
import { AuthFrame } from './Login.jsx';
import { useLanguage } from '../../i18n/LanguageContext.jsx';

export default function Register() {
  const { t } = useLanguage();
  const [form, setForm] = useState({
    fullName: '',
    email: '',
    password: '',
    phone: '',
    role: 'ROLE_FARMER',
    farmLocation: '',
    primaryCrop: '',
    specialization: ''
  });

  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { notify } = useNotification();
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function submit(event) {
    event.preventDefault();

    setIsSubmitting(true);
    try {
      const response = await api.post('/auth/register', form);
      dispatch(setCredentials(response.data));
      notify('Account created successfully. Welcome to CropCare AI.', 'success');
      navigate('/');
    } catch (error) {
      notify(getApiErrorMessage(error, t('Unable to create the account. Please try again.')), 'error');
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <AuthFrame
      title="Create account"
      subtitle="Create your farmer account"
    >
      <form onSubmit={submit} className="grid gap-3">
        <input
          className="input"
          placeholder={t('Full Name')}
          value={form.fullName}
          onChange={(e) =>
            setForm({ ...form, fullName: e.target.value })
          }
          required
        />

        <input
          className="input"
          type="email"
          placeholder={t('Email')}
          value={form.email}
          onChange={(e) =>
            setForm({ ...form, email: e.target.value })
          }
          required
        />

        <input
          className="input"
          placeholder={t('Phone Number')}
          value={form.phone}
          onChange={(e) =>
            setForm({ ...form, phone: e.target.value })
          }
          required
        />

        <>
          <input
            className="input"
            placeholder={t('Farm Location')}
            value={form.farmLocation}
            onChange={(e) => setForm({ ...form, farmLocation: e.target.value })}
          />
          <input
            className="input"
            placeholder={t('Primary Crop')}
            value={form.primaryCrop}
            onChange={(e) => setForm({ ...form, primaryCrop: e.target.value })}
          />
        </>

        <input
          className="input"
          type="password"
          placeholder={t('Password')}
          value={form.password}
          onChange={(e) =>
            setForm({ ...form, password: e.target.value })
          }
          required
        />

        <button type="submit" className="btn" disabled={isSubmitting}>
          {isSubmitting ? t('Creating account...') : t('Register')}
        </button>
      </form>
    </AuthFrame>
  );
}
