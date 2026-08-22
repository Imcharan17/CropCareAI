import { useEffect, useMemo, useState } from 'react';
import StatCard from '../../components/StatCard.jsx';
import AnalyticsCharts from '../../components/AnalyticsCharts.jsx';
import { api } from '../../api/client.js';

export default function DoctorDashboard() {
  const [tickets, setTickets] = useState([]);
  const [analytics, setAnalytics] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      api.get('/tickets?size=100'),
      api.get('/analytics/dashboard'),
    ]).then(([ticketResponse, analyticsResponse]) => {
      setTickets(ticketResponse.data.content || []);
      setAnalytics(analyticsResponse.data);
    }).catch(() => {
      setTickets([]);
      setAnalytics(null);
    }).finally(() => setLoading(false));
  }, []);

  const summary = useMemo(() => {
    const assigned = tickets.length;
    const resolved = tickets.filter((ticket) => ticket.status === 'RESOLVED' || ticket.status === 'CLOSED').length;
    const open = tickets.filter((ticket) => ticket.status === 'OPEN' || ticket.status === 'UNDER_TREATMENT').length;
    const diseaseCoverage = Object.keys(analytics?.diseaseDistribution || {}).length;
    return { assigned, resolved, open, diseaseCoverage };
  }, [analytics, tickets]);

  return <div className="grid gap-4">
    <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
      <StatCard label="Assigned tickets" value={loading ? '...' : summary.assigned} />
      <StatCard label="Resolved tickets" value={loading ? '...' : summary.resolved} />
      <StatCard label="Active cases" value={loading ? '...' : summary.open} />
      <StatCard label="Disease categories" value={loading ? '...' : summary.diseaseCoverage} />
    </div>
    <AnalyticsCharts data={analytics} loading={loading} />
  </div>;
}
