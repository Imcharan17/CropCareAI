import { useEffect, useState } from 'react';
import { Ban, CheckCircle2 } from 'lucide-react';
import { api } from '../../api/client.js';
import { getApiErrorMessage, useNotification } from '../../components/NotificationProvider.jsx';

const emptyDoctor = { fullName: '', email: '', password: '', phone: '', specialization: '' };

export default function UserManagement() {
  const [users, setUsers] = useState([]);
  const [doctor, setDoctor] = useState(emptyDoctor);
  const [saving, setSaving] = useState(false);
  const { notify } = useNotification();
  const load = () => api.get('/admin/users?size=100').then(({ data }) => setUsers(data.content || []));

  useEffect(() => { load(); }, []);

  async function createDoctor(event) {
    event.preventDefault();
    setSaving(true);
    try {
      await api.post('/admin/users/doctors', doctor);
      setDoctor(emptyDoctor);
      notify('Doctor account created successfully.', 'success');
      await load();
    } catch (error) {
      notify(getApiErrorMessage(error, 'Unable to create doctor account.'), 'error');
    } finally {
      setSaving(false);
    }
  }

  async function toggle(user) {
    try {
      await api.patch(`/admin/users/${user.id}/block?blocked=${!user.blocked}`);
      await load();
    } catch (error) {
      notify(getApiErrorMessage(error, 'Unable to update this user.'), 'error');
    }
  }

  return <div className="grid gap-6">
    <section className="panel">
      <h2 className="mb-2 text-xl font-semibold">Create doctor account</h2>
      <p className="mb-4 text-sm text-white/60">Only administrators can add doctors. Share the temporary password with the doctor securely.</p>
      <form onSubmit={createDoctor} className="grid gap-3 md:grid-cols-2">
        <input className="input" placeholder="Doctor full name" value={doctor.fullName} onChange={(e) => setDoctor({ ...doctor, fullName: e.target.value })} required />
        <input className="input" type="email" placeholder="Doctor email" value={doctor.email} onChange={(e) => setDoctor({ ...doctor, email: e.target.value })} required />
        <input className="input" type="tel" placeholder="Phone number" value={doctor.phone} onChange={(e) => setDoctor({ ...doctor, phone: e.target.value })} />
        <input className="input" placeholder="Specialization" value={doctor.specialization} onChange={(e) => setDoctor({ ...doctor, specialization: e.target.value })} required />
        <input className="input md:col-span-2" type="password" minLength={8} placeholder="Temporary password (at least 8 characters)" value={doctor.password} onChange={(e) => setDoctor({ ...doctor, password: e.target.value })} required />
        <button className="btn md:col-span-2" type="submit" disabled={saving}>{saving ? 'Creating doctor...' : 'Create doctor'}</button>
      </form>
    </section>

    <section className="panel overflow-x-auto">
      <h2 className="mb-4 text-xl font-semibold">User accounts</h2>
      <table className="w-full min-w-[820px] text-left text-sm">
        <thead className="text-white/50"><tr><th className="p-3">Name</th><th>Email</th><th>Roles</th><th>Status</th><th>Action</th></tr></thead>
        <tbody>{users.map((user) => <tr key={user.id} className="border-t border-white/10">
          <td className="p-3">{user.fullName}</td><td>{user.email}</td><td>{user.roles?.join(', ')}</td><td>{user.blocked ? 'Blocked' : 'Active'}</td>
          <td><button className="btn-ghost" onClick={() => toggle(user)}>{user.blocked ? <CheckCircle2 size={16} /> : <Ban size={16} />}{user.blocked ? 'Unblock' : 'Block'}</button></td>
        </tr>)}</tbody>
      </table>
    </section>
  </div>;
}
