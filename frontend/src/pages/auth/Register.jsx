import { useState } from 'react';
import { useDispatch } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import { api } from '../../api/client.js';
import { setCredentials } from '../../features/auth/authSlice.js';
import { AuthFrame } from './Login.jsx';

export default function Register() {
  const [form, setForm] = useState({
    fullName: '',
    email: '',
    password: '',
    phone: '',
    role: 'ROLE_FARMER',
    farmLocation: '',
    primaryCrop: ''
  });

  const dispatch = useDispatch();
  const navigate = useNavigate();

  async function submit(event) {
    event.preventDefault();

    try {
      console.log('Register Request:', JSON.stringify(form, null, 2));

      const response = await api.post('/auth/register', form);

      console.log('Register Success:', response.data);

      dispatch(setCredentials(response.data));
      navigate('/');
    } catch (error) {
      console.error('Register Error:', error);

      if (error.response) {
        console.log('Status:', error.response.status);
        console.log('Data:', error.response.data);

        alert(
          typeof error.response.data === 'string'
            ? error.response.data
            : JSON.stringify(error.response.data, null, 2)
        );
      } else {
        alert(error.message);
      }
    }
  }

  return (
    <AuthFrame
      title="Create account"
      subtitle="Start crop diagnosis and AI support"
    >
      <form onSubmit={submit} className="grid gap-3">
        <input
          className="input"
          placeholder="Full Name"
          value={form.fullName}
          onChange={(e) =>
            setForm({ ...form, fullName: e.target.value })
          }
          required
        />

        <input
          className="input"
          type="email"
          placeholder="Email"
          value={form.email}
          onChange={(e) =>
            setForm({ ...form, email: e.target.value })
          }
          required
        />

        <input
          className="input"
          placeholder="Phone Number"
          value={form.phone}
          onChange={(e) =>
            setForm({ ...form, phone: e.target.value })
          }
          required
        />

        <select
          className="input"
          value={form.role}
          onChange={(e) =>
            setForm({ ...form, role: e.target.value })
          }
        >
          <option value="ROLE_FARMER">Farmer</option>
        </select>

        <input
          className="input"
          placeholder="Farm Location"
          value={form.farmLocation}
          onChange={(e) =>
            setForm({ ...form, farmLocation: e.target.value })
          }
        />

        <input
          className="input"
          placeholder="Primary Crop"
          value={form.primaryCrop}
          onChange={(e) =>
            setForm({ ...form, primaryCrop: e.target.value })
          }
        />

        <input
          className="input"
          type="password"
          placeholder="Password"
          value={form.password}
          onChange={(e) =>
            setForm({ ...form, password: e.target.value })
          }
          required
        />

        <button type="submit" className="btn">
          Register
        </button>
      </form>
    </AuthFrame>
  );
}
