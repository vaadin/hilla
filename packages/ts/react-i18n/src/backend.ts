import type { Translations, TranslationsResult } from './types.js';

export interface I18nBackend {
  loadTranslations(language: string, chunks?: readonly string[]): Promise<TranslationsResult>;
}

export class DefaultBackend implements I18nBackend {
  async loadTranslations(language: string, chunks?: readonly string[]): Promise<TranslationsResult> {
    const params = new URLSearchParams([
      ['v-r', 'i18n'],
      ['langtag', language],
      ...(chunks ?? []).map((chunk) => ['chunks', chunk]),
    ]);
    const response = await fetch(`./?${params.toString()}`);
    if (!response.ok) {
      throw new Error('Failed fetching translations.');
    }
    const retrievedLocale = response.headers.get('X-Vaadin-Retrieved-Locale');
    const translations: Translations = await response.json();
    return {
      translations,
      resolvedLanguage: retrievedLocale ?? undefined,
    };
  }
}
