import { useSelector } from 'react-redux';

import { useLanguage } from '../../i18n/LanguageContext.jsx';

export default function Profile() {
  const { t } = useLanguage();
  const user = useSelector((state) => state.auth.user);
  return <section className="panel max-w-2xl"><h3 className="text-xl font-semibold">{t('Profile')}</h3><div className="mt-4 grid gap-3"><input className="input" value={user?.fullName || ''} readOnly /><input className="input" value={user?.email || ''} readOnly /><button className="btn w-fit">{t('Update Profile')}</button></div></section>;
}
