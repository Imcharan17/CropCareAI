import { Bar, Doughnut, Line } from 'react-chartjs-2';
import { Chart as ChartJS, ArcElement, BarElement, CategoryScale, LinearScale, LineElement, PointElement, Tooltip, Legend } from 'chart.js';

ChartJS.register(ArcElement, BarElement, CategoryScale, LinearScale, LineElement, PointElement, Tooltip, Legend);

export default function AnalyticsCharts() {
  const disease = { labels: ['Late Blight', 'Leaf Spot', 'Mildew', 'Healthy'], datasets: [{ data: [42, 21, 15, 18], backgroundColor: ['#22c55e', '#14b8a6', '#eab308', '#94a3b8'] }] };
  const monthly = { labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun'], datasets: [{ label: 'Cases', data: [18, 24, 32, 29, 41, 55], borderColor: '#22c55e', backgroundColor: '#22c55e' }] };
  const aiTickets = { labels: ['Resolved', 'Follow-up', 'Preventive'], datasets: [{ label: 'AI tickets', data: [34, 22, 18], backgroundColor: '#14b8a6' }] };
  return (
    <div className="grid gap-4 xl:grid-cols-3">
      <div className="panel"><h3 className="mb-3 font-semibold">Disease Distribution</h3><Doughnut data={disease} /></div>
      <div className="panel"><h3 className="mb-3 font-semibold">Monthly Cases</h3><Line data={monthly} /></div>
      <div className="panel"><h3 className="mb-3 font-semibold">AI Support Tickets</h3><Bar data={aiTickets} /></div>
    </div>
  );
}
