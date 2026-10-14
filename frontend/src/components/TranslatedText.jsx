import { useEffect, useState } from 'react';
import { api } from '../api/client.js';
import { useLanguage } from '../i18n/LanguageContext.jsx';

const cache = new Map();
let pending = new Map();
let timer;

function translate(text, language) {
  const key = `${language}\u0000${text}`;
  if (cache.has(key)) return Promise.resolve(cache.get(key));
  return new Promise((resolve) => {
    const entry = pending.get(key) || { key, text, language, resolvers: [] };
    entry.resolvers.push(resolve);
    pending.set(key, entry);
    clearTimeout(timer);
    timer = setTimeout(flushTranslations, 35);
  });
}

async function flushTranslations() {
  const entries = [...pending.values()];
  pending = new Map();
  const groups = new Map();
  entries.forEach((entry) => {
    const group = groups.get(entry.language) || [];
    group.push(entry);
    groups.set(entry.language, group);
  });

  await Promise.all([...groups].flatMap(([language, languageEntries]) => {
    const chunks = [];
    let chunk = [];
    let characters = 0;
    languageEntries.forEach((entry) => {
      if (chunk.length >= 20 || characters + entry.text.length > 12000) {
        if (chunk.length) chunks.push(chunk);
        chunk = [];
        characters = 0;
      }
      chunk.push(entry);
      characters += entry.text.length;
    });
    if (chunk.length) chunks.push(chunk);
    return chunks.map(async (chunk) => {
      let values = chunk.map((entry) => entry.text);
      try {
        const { data } = await api.post('/translation', { language, texts: chunk.map((entry) => entry.text) });
        if (Array.isArray(data.translations) && data.translations.length === chunk.length) values = data.translations;
      } catch {
        // Keep the original text visible if translation is unavailable.
      }
      chunk.forEach((entry, index) => {
        const value = values[index] || entry.text;
        cache.set(entry.key, value);
        entry.resolvers.forEach((resolve) => resolve(value));
      });
    });
  }));
}

export default function TranslatedText({ children, as: Element = 'span', ...props }) {
  const { language } = useLanguage();
  const text = children == null ? '' : String(children);
  const [display, setDisplay] = useState(text);

  useEffect(() => {
    let active = true;
    if (!text || language === 'en') {
      setDisplay(text);
    } else {
      setDisplay(text);
      translate(text, language).then((value) => {
        if (active) setDisplay(value);
      });
    }
    return () => { active = false; };
  }, [text, language]);

  return <Element {...props}>{display}</Element>;
}
