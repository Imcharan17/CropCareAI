import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { AlertCircle, CheckCircle2, Loader2, RefreshCw, UploadCloud } from 'lucide-react';
import { api } from '../../api/client.js';

export default function DiseaseDetection() {
  const [image, setImage] = useState(null);
  const [preview, setPreview] = useState('');
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  function onImageChange(file) {
    setResult(null);
    setError('');
    if (!file) {
      setImage(null);
      setPreview('');
      return;
    }
    if (!['image/jpeg', 'image/png'].includes(file.type)) {
      setImage(null);
      setPreview('');
      setError('Upload a JPG, JPEG or PNG crop image.');
      return;
    }
    if (file.size > 10 * 1024 * 1024) {
      setImage(null);
      setPreview('');
      setError('Image must be 10MB or smaller.');
      return;
    }
    setImage(file);
    setPreview(URL.createObjectURL(file));
  }

  async function detect() {
    if (!image) return;
    setLoading(true);
    setError('');
    const form = new FormData();
    form.append('image', image);
    try {
      const { data } = await api.post('/disease/detect', form, { headers: { 'Content-Type': 'multipart/form-data' } });
      setResult(data);
    } catch (err) {
      setError(err.response?.data?.message || 'AI detection failed. Please retry with a clear crop image.');
    } finally {
      setLoading(false);
    }
  }

  return <div className="grid gap-4 lg:grid-cols-[1fr_1.1fr]">
    <section className="panel">
      <h3 className="text-xl font-semibold">AI Disease Detection</h3>
      <label className="mt-5 grid min-h-64 cursor-pointer place-items-center overflow-hidden rounded-lg border border-dashed border-field/40 bg-white/5 p-6 text-center transition hover:border-field/70">
        <input hidden type="file" accept="image/png,image/jpeg" onChange={(e) => onImageChange(e.target.files?.[0])} />
        {preview ? <img src={preview} alt="Selected crop" className="max-h-72 w-full rounded-md object-contain" /> :
          <span><UploadCloud className="mx-auto mb-3 text-field" size={42} />Upload JPG, JPEG or PNG crop image</span>}
      </label>
      {image && <p className="mt-3 truncate text-sm text-white/55">{image.name}</p>}
      {error && <div className="mt-4 flex items-start gap-2 rounded-md border border-red-400/30 bg-red-500/10 p-3 text-sm text-red-100"><AlertCircle size={18} /> <span>{error}</span></div>}
      <button className="btn mt-4 w-full" disabled={!image || loading} onClick={detect}>
        {loading ? <><Loader2 className="animate-spin" size={18} /> Analyzing crop image</> : result ? <><RefreshCw size={18} /> Run Again</> : 'Run AI Detection'}
      </button>
    </section>
    <section className="panel">
      <div className="flex items-center justify-between gap-3">
        <h3 className="text-xl font-semibold">Prediction Result</h3>
        {result && <span className="rounded-full bg-emerald-400/10 px-3 py-1 text-xs text-emerald-200">{result.provider || 'ai'}</span>}
      </div>
      {loading && <div className="mt-4 grid min-h-64 place-items-center rounded-lg bg-white/5 text-white/60">
        <Loader2 className="mb-3 animate-spin text-field" size={34} />
        <span>Checking visible symptoms and treatment options...</span>
      </div>}
      {!loading && result ? <PredictionResult result={result} onRaiseTicket={() => navigate('/farmer/tickets', { state: { diseaseReportId: result.id } })} /> : !loading && <p className="mt-4 text-white/60">Results will include disease name, confidence, affected area, severity, treatments and recovery time.</p>}
    </section>
  </div>;
}

function PredictionResult({ result, onRaiseTicket }) {
  const confidence = Math.round((result.confidenceScore || 0) * 100);
  return <div className="mt-4 grid gap-4 text-sm">
    <div className="rounded-lg border border-emerald-300/20 bg-emerald-400/10 p-4">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <p className="text-white/55">Primary prediction</p>
          <h4 className="mt-1 text-2xl font-semibold">{result.diseaseName}</h4>
        </div>
        <div className="flex items-center gap-2 rounded-full bg-black/20 px-3 py-2 text-emerald-100"><CheckCircle2 size={18} /> {confidence}% confidence</div>
      </div>
      <p className="mt-3 text-white/70">{result.diseaseDescription}</p>
      <div className="mt-4 grid gap-3 sm:grid-cols-2">
        <Info label="Severity" value={result.severityLevel} />
        <Info label="Affected area" value={result.affectedArea} />
      </div>
    </div>

    <ListBlock title="Symptoms" items={result.symptoms} />
    <ListBlock title="Causes" items={result.causes} />
    <Info label="Treatment" value={result.treatment} />
    <Info label="Fertilizer recommendation" value={result.recommendedFertilizers} />
    <Info label="Pesticide recommendation" value={result.recommendedPesticides} />
    <Info label="Organic treatment" value={result.organicTreatment} />
    <Info label="Prevention" value={result.preventionMeasures} />
    <Info label="Expected recovery time" value={result.expectedRecoveryTime} />
    {result.id ? <button className="btn w-full sm:w-fit" onClick={onRaiseTicket}>Raise Ticket for This Detection</button> : null}

    {!!result.predictions?.length && <div className="rounded-md bg-white/5 p-3">
      <p className="text-white/50">Possible matches</p>
      <div className="mt-3 grid gap-2">
        {result.predictions.map((item, index) => <div key={`${item.diseaseName}-${index}`} className="flex flex-wrap justify-between gap-2 rounded-md bg-black/15 p-3">
          <span className="font-medium">{item.diseaseName}</span>
          <span className="text-field">{Math.round((item.confidenceScore || 0) * 100)}% - {item.severityLevel}</span>
        </div>)}
      </div>
    </div>}
  </div>;
}

function Info({ label, value }) {
  return <div className="rounded-md bg-white/5 p-3">
    <p className="text-white/50">{label}</p>
    <p className="mt-1 font-medium text-white/85">{value || 'Not available'}</p>
  </div>;
}

function ListBlock({ title, items = [] }) {
  return <div className="rounded-md bg-white/5 p-3">
    <p className="text-white/50">{title}</p>
    {items.length ? <ul className="mt-2 grid gap-1 text-white/85">
      {items.map((item) => <li key={item}>- {item}</li>)}
    </ul> : <p className="mt-1 text-white/60">Not available</p>}
  </div>;
}
