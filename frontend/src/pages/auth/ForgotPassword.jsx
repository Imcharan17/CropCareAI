import { useState } from 'react';
import { Mail } from 'lucide-react';
import { api } from '../../api/client.js';
import { AuthFrame } from './Login.jsx';

export default function ForgotPassword() {
  const [email, setEmail] = useState('');
  const [message, setMessage] = useState('');

  async function submit(event) {
    event.preventDefault();
    await api.post('/auth/forgot-password', { email });
    setMessage('If the email exists, a reset token has been sent.');
  }

  return <AuthFrame title="Reset access" subtitle="Recover access through the secure mail workflow">
    <form onSubmit={submit} className="grid gap-3">
      <input className="input" placeholder="Registered email" value={email} onChange={(event) => setEmail(event.target.value)} />
      {message && <p className="text-sm text-emerald-200">{message}</p>}
      <button className="btn"><Mail size={17} /> Send reset token</button>
    </form>
  </AuthFrame>;
}
