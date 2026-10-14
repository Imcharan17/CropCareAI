import { useEffect, useState } from 'react';
import { AlertCircle, Bot, CheckCircle2, Loader2, Send } from 'lucide-react';
import { useLocation } from 'react-router-dom';
import { api } from '../../api/client.js';
import { useLanguage } from '../../i18n/LanguageContext.jsx';

export default function TicketTracking() {
  const { t } = useLanguage();
  const [tickets, setTickets] = useState([]);
  const [reports, setReports] = useState([]);
  const [form, setForm] = useState({ title: '', description: '', priority: 'MEDIUM', diseaseReportId: '' });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const location = useLocation();

  useEffect(() => {
    api.get('/tickets?size=50').then(({ data }) => setTickets(data.content || [])).catch(() => {});
    api.get('/reports?size=50').then(({ data }) => {
      const items = Array.isArray(data) ? data : [];
      setReports(items);
      if (location.state?.diseaseReportId) {
        const selected = items.find((item) => item.reportId === location.state.diseaseReportId);
        setForm((current) => ({
          ...current,
          title: selected ? `Support for ${selected.cropName} ${selected.diseaseName}` : current.title,
          diseaseReportId: String(location.state.diseaseReportId),
        }));
      }
    }).catch(() => {});
  }, []);

  function update(field, value) {
    setForm((current) => ({ ...current, [field]: value }));
  }

  async function create(event) {
    event.preventDefault();
    if (!form.title.trim() || !form.description.trim()) {
      setError('Add a title and describe the crop issue before raising a ticket.');
      return;
    }
    setLoading(true);
    setError('');
    try {
      const payload = {
        title: form.title.trim(),
        description: form.description.trim(),
        priority: form.priority,
        diseaseReportId: form.diseaseReportId ? Number(form.diseaseReportId) : null,
      };
      const { data } = await api.post('/tickets', payload);
      setTickets((current) => [data, ...current]);
      setForm({ title: '', description: '', priority: 'MEDIUM', diseaseReportId: '' });
    } catch (err) {
      setError(err.response?.data?.message || 'Could not raise the doctor support request. Please try again.');
    } finally {
      setLoading(false);
    }
  }

  return <div className="grid gap-4">
    <section className="panel">
      <div className="flex items-center gap-3">
        <div className="grid h-10 w-10 place-items-center rounded-md bg-field text-ink"><Bot size={20} /></div>
        <div>
          <h3 className="text-xl font-semibold">{t('Ask a crop-care doctor')}</h3>
          <p className="text-sm text-white/55">{t('A doctor will review your case and post a treatment recommendation here. AI may provide preliminary notes.')}</p>
        </div>
      </div>

      <form onSubmit={create} className="mt-5 grid gap-3">
        <input className="input" placeholder={t('Ticket title')} value={form.title} onChange={(event) => update('title', event.target.value)} />
        <textarea className="input min-h-32 resize-y" placeholder={t('Describe symptoms, crop stage, weather, and what you already tried')} value={form.description} onChange={(event) => update('description', event.target.value)} />
        <div className="grid gap-3 md:grid-cols-2">
          <select className="input" value={form.priority} onChange={(event) => update('priority', event.target.value)}>
            <option value="LOW">{t('Low priority')}</option>
            <option value="MEDIUM">{t('Medium priority')}</option>
            <option value="HIGH">{t('High priority')}</option>
            <option value="URGENT">{t('Urgent priority')}</option>
          </select>
          <select className="input" value={form.diseaseReportId} onChange={(event) => update('diseaseReportId', event.target.value)}>
            <option value="">{t('No linked disease report')}</option>
            {reports.map((report) => <option key={report.reportId} value={report.reportId}>#{report.reportId} {report.diseaseName} - {report.severity}</option>)}
          </select>
        </div>
        {error && <div className="flex items-start gap-2 rounded-md border border-red-400/30 bg-red-500/10 p-3 text-sm text-red-100"><AlertCircle size={18} /> <span>{error}</span></div>}
        <button className="btn w-full md:w-fit" disabled={loading}>
          {loading ? <><Loader2 className="animate-spin" size={17} /> {t('Sending request')}</> : <><Send size={17} /> {t('Send to doctor')}</>}
        </button>
      </form>
    </section>
    <TicketTable tickets={tickets} />
  </div>;
}

export function TicketTable({ tickets }) {
  const { t } = useLanguage();
  return <section className="panel">
    <h3 className="text-xl font-semibold">{t('Doctor support requests')}</h3>
    <div className="mt-4 grid gap-3">
      {tickets.map((ticket) => <article key={ticket.id} className="rounded-md border border-white/10 bg-white/5 p-4">
        <div className="flex flex-wrap items-start justify-between gap-3">
          <div>
            <p className="text-sm text-white/45">Ticket #{ticket.id}</p>
            <h4 className="mt-1 font-semibold">{ticket.title}</h4>
          </div>
          <div className="flex flex-wrap gap-2 text-xs">
            <span className="rounded-full bg-emerald-400/10 px-3 py-1 text-emerald-200"><CheckCircle2 size={13} className="mr-1 inline" />{ticket.status}</span>
            <span className="rounded-full bg-white/10 px-3 py-1 text-white/70">{ticket.priority}</span>
            <span className="rounded-full bg-field/15 px-3 py-1 text-field">{ticket.doctorName && ticket.doctorName !== 'Unassigned' ? ticket.doctorName : t('Awaiting doctor')}</span>
          </div>
        </div>
        {ticket.description && <p className="mt-3 whitespace-pre-line text-sm text-white/65">{ticket.description}</p>}
        {ticket.treatmentRecommendation && <div className="mt-4 whitespace-pre-line rounded-md bg-black/20 p-3 text-sm text-white/78">{ticket.treatmentRecommendation}</div>}
      </article>)}
      {!tickets.length && <p className="text-sm text-white/50">{t('No tickets raised yet.')}</p>}
    </div>
  </section>;
}
