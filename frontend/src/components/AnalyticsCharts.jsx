import { Bar, Doughnut, Line } from 'react-chartjs-2';
import { Chart as ChartJS, ArcElement, BarElement, CategoryScale, LinearScale, LineElement, PointElement, Tooltip, Legend } from 'chart.js';

ChartJS.register(ArcElement, BarElement, CategoryScale, LinearScale, LineElement, PointElement, Tooltip, Legend);

const palette = ['#22c55e', '#14b8a6', '#eab308', '#94a3b8', '#38bdf8', '#f97316'];

export default function AnalyticsCharts({ data, loading = false }) {
  if (loading) {
    return <div className="grid gap-4 xl:grid-cols-3">{[1, 2, 3].map((item) => <div key={item} className="panel h-80 animate-pulse bg-white/5" />)}</div>;
  }

  const disease = doughnutDataset(data?.diseaseDistribution, ['No data'], [1]);
  const monthly = lineDataset(data?.doctorPerformance, 'Cases handled');
  const aiTickets = barDataset(data?.ticketResolutionRate, 'Tickets');

  return (
    <div className="grid gap-4 xl:grid-cols-3">
      <div className="panel"><h3 className="mb-3 font-semibold">Disease Distribution</h3><Doughnut data={disease} /></div>
      <div className="panel"><h3 className="mb-3 font-semibold">Doctor Workload</h3><Line data={monthly} /></div>
      <div className="panel"><h3 className="mb-3 font-semibold">Ticket Outcomes</h3><Bar data={aiTickets} /></div>
    </div>
  );
}

function doughnutDataset(source = {}, fallbackLabels = ['No data'], fallbackValues = [1]) {
  const labels = Object.keys(source || {});
  const values = Object.values(source || {});
  return {
    labels: labels.length ? labels : fallbackLabels,
    datasets: [{
      data: values.length ? values : fallbackValues,
      backgroundColor: palette,
    }],
  };
}

function lineDataset(source = {}, label) {
  const labels = Object.keys(source || {});
  const values = Object.values(source || {});
  return {
    labels: labels.length ? labels : ['No data'],
    datasets: [{
      label,
      data: values.length ? values : [0],
      borderColor: '#22c55e',
      backgroundColor: '#22c55e',
      tension: 0.3,
    }],
  };
}

function barDataset(source = {}, label) {
  const labels = Object.keys(source || {});
  const values = Object.values(source || {});
  return {
    labels: labels.length ? labels : ['No data'],
    datasets: [{
      label,
      data: values.length ? values : [0],
      backgroundColor: '#14b8a6',
    }],
  };
}
