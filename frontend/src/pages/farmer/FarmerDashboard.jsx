import { useEffect, useMemo, useState } from 'react';
import { Upload, Ticket, FileText, Bot } from 'lucide-react';
import { Link } from 'react-router-dom';
import StatCard from '../../components/StatCard.jsx';
import { api } from '../../api/client.js';

export default function FarmerDashboard() {
  const [reports, setReports] = useState([]);
  const [tickets, setTickets] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      api.get('/reports?size=100'),
      api.get('/tickets?size=100'),
    ]).then(([reportsResponse, ticketsResponse]) => {
      setReports(Array.isArray(reportsResponse.data) ? reportsResponse.data : []);
      setTickets(ticketsResponse.data.content || []);
    }).catch(() => {
      setReports([]);
      setTickets([]);
    }).finally(() => setLoading(false));
  }, []);

  const stats = useMemo(() => {
    const diseaseScans = reports.length;
    const aiTickets = tickets.length;
    const resolvedCases = reports.filter((report) => report.currentStatus === 'Resolved' || report.ticketStatus === 'RESOLVED' || report.ticketStatus === 'CLOSED').length;
    const aiReplies = tickets.filter((ticket) => ticket.treatmentRecommendation).length;
    return { diseaseScans, aiTickets, resolvedCases, aiReplies };
  }, [reports, tickets]);

  return <div className="grid gap-4">
    <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
      <StatCard label="Disease scans" value={loading ? '...' : stats.diseaseScans} />
      <StatCard label="AI tickets" value={loading ? '...' : stats.aiTickets} />
      <StatCard label="Resolved cases" value={loading ? '...' : stats.resolvedCases} />
      <StatCard label="AI replies" value={loading ? '...' : stats.aiReplies} />
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
