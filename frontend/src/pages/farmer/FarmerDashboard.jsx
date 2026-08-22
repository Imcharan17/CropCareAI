import { Upload, Ticket, FileText, Bot } from 'lucide-react';
import { Link } from 'react-router-dom';
import StatCard from '../../components/StatCard.jsx';

export default function FarmerDashboard() {
  return <div className="grid gap-4">
    <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
      <StatCard label="Disease scans" value="18" /><StatCard label="AI tickets" value="3" /><StatCard label="Resolved cases" value="12" /><StatCard label="AI replies" value="6" />
    </div>
    <section className="panel">
      <h3 className="text-xl font-semibold">Field Command Center</h3>
      <div className="mt-4 grid gap-3 md:grid-cols-4">
        {[
          ['Upload crop image', Upload, '/farmer/detect'],
          ['Raise AI ticket', Ticket, '/farmer/tickets'],
          ['Disease reports', FileText, '/farmer/history'],
          ['AI recommendations', Bot, '/farmer/tickets'],
        ].map(([label, Icon, to]) => (
          <Link key={label} to={to} className="rounded-md border border-white/10 bg-white/5 p-4 transition hover:border-field/40 hover:bg-white/10"><Icon className="mb-3 text-field" /><p>{label}</p></Link>
        ))}
      </div>
    </section>
  </div>;
}
