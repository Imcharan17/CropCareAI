import { useEffect, useState } from 'react';
import { Eye, Search } from 'lucide-react';
import { api } from '../../api/client.js';

const emptyFilters = { search: '', from: '', to: '' };

export default function Reports() {
  const [items, setItems] = useState([]);
  const [filters, setFilters] = useState(emptyFilters);
  const [selected, setSelected] = useState(null);

  useEffect(() => { load(); }, []);

  async function load(next = filters) {
    const { data } = await api.get('/reports', { params: compact({ from: next.from, to: next.to }) });
    const reports = Array.isArray(data) ? data : [];
    setItems(filterBySearch(reports, next.search));
  }

  async function openDetails(reportId) {
    const { data } = await api.get(`/reports/${reportId}`);
    setSelected(data);
  }

  return <div className="grid gap-4">
    <section className="panel">
      <h3 className="text-xl font-semibold">Detection History</h3>
      <p className="mt-1 text-sm text-white/55">Newest crop disease detections across all farmers.</p>
      <div className="mt-4 grid gap-3 lg:grid-cols-[1fr_180px_180px_auto_auto]">
        <input
          className="input"
          placeholder="Search crop, disease, farmer, district, severity or status"
          value={filters.search}
          onChange={(e) => setFilters({ ...filters, search: e.target.value })}
        />
        <input className="input" type="date" value={filters.from} onChange={(e) => setFilters({ ...filters, from: e.target.value })} />
        <input className="input" type="date" value={filters.to} onChange={(e) => setFilters({ ...filters, to: e.target.value })} />
        <button className="btn" onClick={() => load()}><Search size={17} /> Search</button>
        <button className="btn-ghost" onClick={() => { setFilters(emptyFilters); load(emptyFilters); }}>Reset</button>
      </div>
    </section>

    <section className="grid gap-3">
      {items.map((item) => <article key={item.reportId} className="panel">
        <div className="grid gap-4 md:grid-cols-[92px_1fr_auto] md:items-center">
          <div className="h-20 w-20 overflow-hidden rounded-md bg-white/5">
            {item.imageUrl ? <img className="h-full w-full object-cover" src={item.imageUrl} alt={item.diseaseName} /> : <div className="grid h-full place-items-center text-xs text-white/40">No image</div>}
          </div>
          <div>
            <div className="flex flex-wrap items-center gap-2">
              <span className="text-sm text-white/45">Detection #{item.reportId}</span>
              <span className="rounded-full bg-field/15 px-2 py-1 text-xs text-field">{Math.round((item.confidence || 0) * 100)}%</span>
              <span className="rounded-full bg-white/10 px-2 py-1 text-xs">{item.severity}</span>
            </div>
            <h4 className="mt-1 font-semibold">{item.cropName} - {item.diseaseName}</h4>
            <p className="mt-1 text-sm text-white/55">{item.farmerName} - {item.district || item.location} - {new Date(item.detectionDate).toLocaleString()}</p>
            <p className="mt-2 text-sm text-white/75">{item.currentStatus}</p>
          </div>
          <button className="btn-ghost" onClick={() => openDetails(item.reportId)}><Eye size={16} /> View Details</button>
        </div>
      </article>)}
      {!items.length && <section className="panel text-white/55">No detections found.</section>}
    </section>

    {selected && <Details report={selected} onClose={() => setSelected(null)} />}
  </div>;
}

function Details({ report, onClose }) {
  return <div className="fixed inset-0 z-50 grid place-items-center bg-black/70 p-4">
    <article className="max-h-[90vh] w-full max-w-3xl overflow-auto rounded-lg border border-white/10 bg-[#07130d] p-5">
      <div className="flex justify-between gap-3"><h3 className="text-xl font-semibold">Detection #{report.reportNumber}</h3><button className="btn-ghost" onClick={onClose}>Close</button></div>
      {report.uploadedImageUrl && <img src={report.uploadedImageUrl} alt="Uploaded crop" className="mt-4 max-h-72 rounded-md object-contain" />}
      <div className="mt-4 grid gap-3">
        <Info label="Crop Name" value={report.crop?.name} />
        <Info label="Disease Name" value={report.detectedDisease} />
        <Info label="Scientific Name" value={report.scientificName} />
        <Info label="Confidence Score" value={`${Math.round((report.confidenceScore || 0) * 100)}%`} />
        <Info label="Severity" value={report.severityLevel} />
        <Info label="Symptoms" value={(report.symptoms || []).join(', ')} />
        <Info label="Possible Causes" value={(report.possibleCauses || []).join(', ')} />
        <Info label="AI Diagnosis" value={report.aiRecommendation} />
        <Info label="Recommended Treatment" value={report.treatmentRecommendation} />
        <Info label="Preventive Measures" value={report.preventiveMeasures} />
        <Info label="Recovery Status" value={report.estimatedRecoveryTime} />
        <Info label="Related Support Ticket" value={report.ticketStatus || 'No support ticket raised'} />
      </div>
    </article>
  </div>;
}

function Info({ label, value }) {
  return <div className="rounded-md bg-white/5 p-3"><p className="text-sm text-white/50">{label}</p><p className="mt-1 whitespace-pre-line text-white/85">{value || 'Not available'}</p></div>;
}

function compact(filters) {
  return Object.fromEntries(Object.entries(filters).filter(([, value]) => value));
}

function filterBySearch(items, search) {
  if (!search?.trim()) return items;
  const words = normalize(search).split(' ').filter(Boolean);
  return items.filter((item) => {
    const haystack = normalize([
      item.reportId,
      item.farmerName,
      item.mobileNumber,
      item.cropName,
      item.diseaseName,
      item.severity,
      item.district,
      item.location,
      item.currentStatus,
      item.ticketStatus,
      item.treatmentSummary,
      item.aiRecommendationSummary,
      Math.round((item.confidence || 0) * 100),
    ].filter(Boolean).join(' '));
    return words.every((word) => haystack.includes(word));
  });
}

function normalize(value) {
  return String(value).toLowerCase().replace(/[_-]+/g, ' ').replace(/\s+/g, ' ').trim();
}
