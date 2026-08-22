import { useEffect, useState } from 'react';
import AnalyticsCharts from '../../components/AnalyticsCharts.jsx';
import { api } from '../../api/client.js';

export default function Analytics() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.get('/analytics/dashboard')
      .then(({ data: response }) => setData(response))
      .catch(() => setData(null))
      .finally(() => setLoading(false));
  }, []);

  return <AnalyticsCharts data={data} loading={loading} />;
}
