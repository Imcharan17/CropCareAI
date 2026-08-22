import StatCard from '../../components/StatCard.jsx';
import AnalyticsCharts from '../../components/AnalyticsCharts.jsx';

export default function DoctorDashboard() {
  return <div className="grid gap-4"><div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4"><StatCard label="Assigned tickets" value="24" /><StatCard label="Resolved" value="19" /><StatCard label="Avg response" value="2.4h" /><StatCard label="Rating" value="4.8" /></div><AnalyticsCharts /></div>;
}
