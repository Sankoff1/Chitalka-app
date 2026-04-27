import type { AppLocale } from './types';

import en from './locales/en.json';
import ru from './locales/ru.json';

const catalogs: Record<AppLocale, Record<string, unknown>> = {
  ru: ru as Record<string, unknown>,
  en: en as Record<string, unknown>,
};

function getNested(obj: unknown, path: string[]): string | undefined {
  let cur: unknown = obj;
  for (const key of path) {
    if (cur === null || typeof cur !== 'object') {
      return undefined;
    }
    cur = (cur as Record<string, unknown>)[key];
  }
  return typeof cur === 'string' ? cur : undefined;
}

/**
 * Синхронный перевод по вложенному пути (например `drawer.settings`), без React.
 */
export function tSync(locale: AppLocale, path: string, vars?: Record<string, string | number>): string {
  const parts = path.split('.').filter(Boolean);
  const raw = getNested(catalogs[locale], parts) ?? getNested(catalogs.ru, parts) ?? path;
  if (!vars) {
    return raw;
  }
  return raw.replace(/\{\{(\w+)\}\}/g, (_, name: string) =>
    vars[name] !== undefined ? String(vars[name]) : ''
  );
}

export function bookFallbackLabels(locale: AppLocale): {
  untitled: string;
  unknownAuthor: string;
} {
  return {
    untitled: tSync(locale, 'book.untitled'),
    unknownAuthor: tSync(locale, 'book.unknownAuthor'),
  };
}
