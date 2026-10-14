import { useEffect, useState } from 'react';
import { AlertCircle, CheckCircle2, Loader2, UserRoundCheck } from 'lucide-react';
import { api } from '../../api/client.js';
import { useLanguage } from '../../i18n/LanguageContext.jsx';
import TranslatedText from '../../components/TranslatedText.jsx';

export default function AssignedTickets() {
  const { t } = useLanguage();
  const [tickets, setTickets] = useState([]);
  const [drafts, setDrafts] = useState({});
  const [loading, setLoading] = useState(true);
  const [busyId, setBusyId] = useState(null);
  const [error, setError] = useState('');

  async function loadTickets() {
    setLoading(true);
    try {
      const { data } = await api.get('/tickets?size=100');
      setTickets(data.content || []);
      setDrafts((current) => {
        const next = { ...current };
        for (const ticket of data.content || []) {
          if (next[ticket.id] === undefined) next[ticket.id] = '';
        }
        return next;
      });
    } catch (err) {
      setError(err.response?.data?.message || t('Could not load the doctor ticket queue.'));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { loadTickets(); }, []);

  async function claim(ticketId) {
    setBusyId(ticketId);
    setError('');
    try {
      const { data } = await api.patch(`/tickets/${ticketId}/claim`);
      setTickets((current) => current.map((ticket) => ticket.id === ticketId ? data : ticket));
    } catch (err) {
      setError(err.response?.data?.message || 'Could not claim this ticket. Refresh the queue and try again.');
    } finally {
      setBusyId(null);
    }
  }

  async function resolve(ticketId) {
    const recommendation = drafts[ticketId]?.trim();
    if (!recommendation) {
      setError(t('Add your treatment recommendation before resolving the ticket.'));
      return;
    }
    setBusyId(ticketId);
    setError('');
    try {
      const { data } = await api.patch(`/tickets/${ticketId}/status`, null, {
        params: { status: 'RESOLVED', recommendation },
      });
      setTickets((current) => current.map((ticket) => ticket.id === ticketId ? data : ticket));
    } catch (err) {
      setError(err.response?.data?.message || 'Could not resolve this ticket. Please try again.');
    } finally {
      setBusyId(null);
    }
  }

  return <section className="panel">
    <div className="flex flex-wrap items-start justify-between gap-3">
      <div>
        <h3 className="text-xl font-semibold">{t('Farmer support tickets')}</h3>
        <p className="mt-1 text-sm text-white/55">{t('Claim an open case, review the details, and share a treatment recommendation.')}</p>
      </div>
      <button className="btn" type="button" onClick={loadTickets} disabled={loading}>{t('Refresh queue')}</button>
    </div>

    {error && <div className="mt-4 flex items-start gap-2 rounded-md border border-red-400/30 bg-red-500/10 p-3 text-sm text-red-100"><AlertCircle size={18} /> <span>{error}</span></div>}

    <div className="mt-5 grid gap-4">
      {tickets.map((ticket) => {
        const unassigned = !ticket.doctorName || ticket.doctorName === 'Unassigned';
        const resolved = ticket.status === 'RESOLVED' || ticket.status === 'CLOSED';
        return <article key={ticket.id} className="rounded-md border border-white/10 bg-white/5 p-4">
          <div className="flex flex-wrap items-start justify-between gap-3">
            <div>
              <p className="text-sm text-white/45">Ticket #{ticket.id} · {ticket.priority} priority</p>
              <h4 className="mt-1 font-semibold"><TranslatedText>{ticket.title}</TranslatedText></h4>
              <p className="mt-1 text-sm text-white/55">{t('Farmer:')} {ticket.farmerName}</p>
            </div>
            <div className="flex flex-wrap items-center gap-2 text-xs">
              <span className="rounded-full bg-white/10 px-3 py-1 text-white/75">{t(ticket.status.replaceAll('_', ' '))}</span>
              {!unassigned && <span className="rounded-full bg-emerald-400/10 px-3 py-1 text-emerald-200">{t('Assigned to you')}</span>}
            </div>
          </div>

          {ticket.description && <p className="mt-4 whitespace-pre-line text-sm text-white/75"><TranslatedText>{ticket.description}</TranslatedText></p>}
          {ticket.treatmentRecommendation && <div className="mt-4 whitespace-pre-line rounded-md bg-black/20 p-3 text-sm text-white/65"><span className="font-medium text-white/80">{t('Preliminary AI notes')}</span><br /><TranslatedText>{ticket.treatmentRecommendation}</TranslatedText></div>}

          {unassigned ? <button className="btn mt-4" type="button" onClick={() => claim(ticket.id)} disabled={busyId === ticket.id}>
            {busyId === ticket.id ? <><Loader2 className="animate-spin" size={17} /> {t('Claiming')}</> : <><UserRoundCheck size={17} /> {t('Claim case')}</>}
          </button> : !resolved ? <div className="mt-4 grid gap-3">
            <textarea
              className="input min-h-28 resize-y"
              placeholder={t('Write your diagnosis and treatment recommendation for the farmer')}
              value={drafts[ticket.id] || ''}
              onChange={(event) => setDrafts((current) => ({ ...current, [ticket.id]: event.target.value }))}
            />
            <button className="btn w-fit" type="button" onClick={() => resolve(ticket.id)} disabled={busyId === ticket.id}>
              {busyId === ticket.id ? <><Loader2 className="animate-spin" size={17} /> {t('Saving')}</> : <><CheckCircle2 size={17} /> {t('Resolve ticket')}</>}
            </button>
          </div> : <p className="mt-4 text-sm text-emerald-200">This ticket has been resolved.</p>}
        </article>;
      })}
      {!loading && !tickets.length && <p className="text-sm text-white/50">{t('No open or assigned tickets right now.')}</p>}
      {loading && <p className="text-sm text-white/50">Loading tickets…</p>}
    </div>
  </section>;
}
