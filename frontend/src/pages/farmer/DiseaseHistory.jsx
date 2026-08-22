import { useEffect, useMemo, useState } from 'react';
import { Eye, LifeBuoy, Search } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { api } from '../../api/client.js';

const emptyFilters = { crop: '', disease: '', status: '', from: '', to: '' };

export default function DiseaseHistory() {
  const [items, setItems] = useState([]);
  const [filters, setFilters] = useState(emptyFilters);
  const [selected, setSelected] = useState(null);
  const navigate = useNavigate();

  useEffect(() => { load(); }, []);

  async function load(next = filters) {
    const { data } = await api.get('/reports', { params: compact(next) });
    setItems(Array.isArray(data) ? data : []);
  }

  async function openDetails(reportId) {
    const { data } = await api.get(`/reports/${reportId}`);
    setSelected(data);
  }

  const options = useMemo(() => ({
    crops: unique(items, 'cropName'),
    diseases: unique(items, 'diseaseName'),
    statuses: unique(items, 'currentStatus'),
  }), [items]);

  return <div className="grid gap-4">
    <section className="panel">
      <h3 className="text-xl font-semibold">My Detection History</h3>
      <p className="mt-1 text-sm text-white/55">Previous crop scans and AI recommendations, newest first.</p>
      <div className="mt-4 grid gap-3 md:grid-cols-5">
        <select className="input" value={filters.crop} onChange={(e) => setFilters({ ...filters, crop: e.target.value })}><option value="">All crops</option>{options.crops.map((item) => <option key={item}>{item}</option>)}</select>
        <select className="input" value={filters.disease} onChange={(e) => setFilters({ ...filters, disease: e.target.value })}><option value="">All diseases</option>{options.diseases.map((item) => <option key={item}>{item}</option>)}</select>
        <select className="input" value={filters.status} onChange={(e) => setFilters({ ...filters, status: e.target.value })}><option value="">All status</option>{options.statuses.map((item) => <option key={item}>{item}</option>)}</select>
        <input className="input" type="date" value={filters.from} onChange={(e) => setFilters({ ...filters, from: e.target.value })} />
        <input className="input" type="date" value={filters.to} onChange={(e) => setFilters({ ...filters, to: e.target.value })} />
      </div>
      <div className="mt-3 flex gap-2"><button className="btn" onClick={() => load()}><Search size={17} /> Search</button><button className="btn-ghost" onClick={() => { setFilters(emptyFilters); load(emptyFilters); }}>Reset</button></div>
    </section>

    <section className="grid gap-3">
      {items.map((item) => <article key={item.reportId} className="panel">
        <div className="grid gap-4 md:grid-cols-[92px_1fr]">
          <div className="h-20 w-20 overflow-hidden rounded-md bg-white/5">{item.imageUrl ? <img className="h-full w-full object-cover" src={item.imageUrl} alt={item.diseaseName} /> : null}</div>
          <div>
            <div className="flex flex-wrap items-center justify-between gap-3">
              <div>
                <h4 className="font-semibold">{item.cropName} - {item.diseaseName}</h4>
                <p className="mt-1 text-sm text-white/55">{new Date(item.detectionDate).toLocaleString()}</p>
              </div>
              <div className="flex gap-2 text-xs">
                <span className="rounded-full bg-field/15 px-3 py-1 text-field">{Math.round((item.confidence || 0) * 100)}%</span>
                <span className="rounded-full bg-white/10 px-3 py-1">{item.severity}</span>
                <span className="rounded-full bg-emerald-400/10 px-3 py-1 text-emerald-100">{item.currentStatus}</span>
              </div>
            </div>
            <p className="mt-3 text-sm text-white/75">{item.aiRecommendationSummary || item.treatmentSummary}</p>
            <div className="mt-4 flex flex-wrap gap-2">
              <button className="btn-ghost" onClick={() => openDetails(item.reportId)}><Eye size={16} /> View Details</button>
              {!item.ticketStatus && <button className="btn" onClick={() => navigate('/farmer/tickets', { state: { diseaseReportId: item.reportId } })}><LifeBuoy size={16} /> Raise Support Ticket</button>}
            </div>
          </div>
        </div>
      </article>)}
      {!items.length && <section className="panel text-white/55">No detection history yet.</section>}
    </section>

    {selected && <Details report={selected} onClose={() => setSelected(null)} />}
  </div>;
}

function Details({ report, onClose }) {
  return <div className="fixed inset-0 z-50 grid place-items-center bg-black/70 p-4">
    <article className="max-h-[90vh] w-full max-w-3xl overflow-auto rounded-lg border border-white/10 bg-[#07130d] p-5">
      <div className="flex justify-between gap-3"><h3 className="text-xl font-semibold">Detection #{report.reportNumber}</h3><button className="btn-ghost" onClick={onClose}>Close</button></div>
      {report.uploadedImageUrl && <img src={report.uploadedImageUrl} alt="Uploaded crop" className="mt-4 max-h-64 rounded-md object-contain" />}
      <div className="mt-4 grid gap-3">
        <Info label="Disease" value={`${report.detectedDisease} (${report.scientificName})`} />
        <Info label="Confidence / Severity" value={`${Math.round((report.confidenceScore || 0) * 100)}% / ${report.severityLevel}`} />
        <Info label="Symptoms" value={(report.symptoms || []).join(', ')} />
        <Info label="Possible Causes" value={(report.possibleCauses || []).join(', ')} />
        <Info label="AI Diagnosis" value={report.aiRecommendation} />
        <Info label="Treatment" value={report.treatmentRecommendation} />
        <Info label="Prevention" value={report.preventiveMeasures} />
        <Info label="Fertilizers" value={report.recommendedFertilizers} />
        <Info label="Pesticides" value={report.recommendedPesticides} />
        <Info label="Organic Alternatives" value={report.organicAlternatives} />
        <Info label="Recovery Status" value={report.estimatedRecoveryTime} />
      </div>
    </article>
  </div>;
}

function Info({ label, value }) {
  return <div className="rounded-md bg-white/5 p-3"><p className="text-sm text-white/50">{label}</p><p className="mt-1 whitespace-pre-line text-white/85">{value || 'Not available'}</p></div>;
}

function unique(items, key) {
  return [...new Set(items.map((item) => item[key]).filter(Boolean))];
}

function compact(filters) {
  return Object.fromEntries(Object.entries(filters).filter(([, value]) => value));
}
