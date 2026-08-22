import { useEffect, useState } from 'react';
import { Ban, CheckCircle2 } from 'lucide-react';
import { api } from '../../api/client.js';

export default function UserManagement() {
  const [users, setUsers] = useState([]);
  const load = () => api.get('/admin/users?size=100').then(({ data }) => setUsers(data.content || []));

  useEffect(() => { load(); }, []);

  async function toggle(user) {
    await api.patch(`/admin/users/${user.id}/block?blocked=${!user.blocked}`);
    load();
  }

  return <section className="panel overflow-x-auto"><table className="w-full min-w-[820px] text-left text-sm"><thead className="text-white/50"><tr><th className="p-3">Name</th><th>Email</th><th>Roles</th><th>Status</th><th>Action</th></tr></thead><tbody>{users.map((user) => <tr key={user.id} className="border-t border-white/10"><td className="p-3">{user.fullName}</td><td>{user.email}</td><td>{user.roles?.join(', ')}</td><td>{user.blocked ? 'Blocked' : 'Active'}</td><td><button className="btn-ghost" onClick={() => toggle(user)}>{user.blocked ? <CheckCircle2 size={16} /> : <Ban size={16} />}{user.blocked ? 'Unblock' : 'Block'}</button></td></tr>)}</tbody></table></section>;
}
