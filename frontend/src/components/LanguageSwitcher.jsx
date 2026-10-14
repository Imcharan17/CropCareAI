import { Languages } from 'lucide-react';
import { useLanguage } from '../i18n/LanguageContext.jsx';

export default function LanguageSwitcher() {
  const { language, setLanguage, t } = useLanguage();
  return <label className="flex items-center gap-2 rounded-md border border-white/15 bg-black/10 px-3 py-2 text-sm">
    <Languages size={17} className="text-emerald-200" />
    <span className="sr-only">{t('Language')}</span>
    <select className="bg-transparent text-white outline-none" aria-label={t('Language')} value={language} onChange={(event) => setLanguage(event.target.value)}>
      <option className="bg-[#10231a]" value="en">{t('English')}</option>
      <option className="bg-[#10231a]" value="te">{t('Telugu')}</option>
      <option className="bg-[#10231a]" value="hi">{t('Hindi')}</option>
    </select>
  </label>;
}
