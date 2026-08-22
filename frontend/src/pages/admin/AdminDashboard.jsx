import { useEffect, useMemo, useState } from 'react';
import { Bar, Doughnut } from 'react-chartjs-2';
import { Chart as ChartJS, ArcElement, BarElement, CategoryScale, LinearScale, Tooltip, Legend } from 'chart.js';
import StatCard from '../../components/StatCard.jsx';
import { api } from '../../api/client.js';

ChartJS.register(ArcElement, BarElement, CategoryScale, LinearScale, Tooltip, Legend);

export default function AdminDashboard() {
  const [dashboard, setDashboard] = useState(null);

  useEffect(() => {
    api.get('/reports/dashboard').then(({ data }) => setDashboard(data)).catch(() => setDashboard(null));
  }, []);

  const severity = useMemo(() => chartData(dashboard?.severityDistribution), [dashboard]);
  const diseases = useMemo(() => chartData(dashboard?.diseaseDistribution), [dashboard]);
  const status = useMemo(() => chartData({
    'Needs attention': dashboard?.pendingTickets || 0,
    Resolved: dashboard?.resolvedTickets || 0,
  }), [dashboard]);
  const highSeverity = (dashboard?.severityDistribution?.HIGH || 0) + (dashboard?.severityDistribution?.CRITICAL || 0);

  if (!dashboard) {
    return <div className="grid gap-4">
      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">{[1, 2, 3, 4].map((item) => <div key={item} className="panel h-28 animate-pulse bg-white/5" />)}</div>
      <div className="grid gap-4 xl:grid-cols-3">{[1, 2, 3].map((item) => <div key={item} className="panel h-80 animate-pulse bg-white/5" />)}</div>
    </div>;
  }

  return <div className="grid gap-4">
    <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
      <StatCard label="Most affected crop" value={dashboard.mostAffectedCrop} />
      <StatCard label="Most common disease" value={dashboard.mostCommonDisease} />
      <StatCard label="High severity" value={highSeverity} />
      <StatCard label="Detections this week" value={dashboard.detectionsThisWeek} />
    </div>
    <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
      <StatCard label="Total detections" value={dashboard.totalDiseaseReports} />
      <StatCard label="Average confidence" value={`${Math.round((dashboard.averageAiConfidence || 0) * 100)}%`} />
      <StatCard label="Under treatment" value={dashboard.pendingTickets} />
      <StatCard label="Resolved tickets" value={dashboard.resolvedTickets} />
    </div>
    <div className="grid gap-4 xl:grid-cols-3">
      <ChartPanel title="Disease Trends"><Doughnut data={diseases} /></ChartPanel>
      <ChartPanel title="Severity"><Bar data={severity} /></ChartPanel>
      <ChartPanel title="Treatment Status"><Doughnut data={status} /></ChartPanel>
    </div>
  </div>;
}

function ChartPanel({ title, children }) {
  return <div className="panel"><h3 className="mb-3 font-semibold">{title}</h3>{children}</div>;
}

function chartData(source = {}) {
  const labels = Object.keys(source);
  const values = Object.values(source);
  return {
    labels,
    datasets: [{ data: values, backgroundColor: ['#22c55e', '#14b8a6', '#eab308', '#f97316', '#94a3b8', '#38bdf8'] }],
  };
}
