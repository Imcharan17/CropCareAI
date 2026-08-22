import { useSelector } from 'react-redux';

export default function Profile() {
  const user = useSelector((state) => state.auth.user);
  return <section className="panel max-w-2xl"><h3 className="text-xl font-semibold">Profile</h3><div className="mt-4 grid gap-3"><input className="input" value={user?.fullName || ''} readOnly /><input className="input" value={user?.email || ''} readOnly /><button className="btn w-fit">Update Profile</button></div></section>;
}
