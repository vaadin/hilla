import type { Translations, TranslationsResult } from './types.js';

export interface I18nBackend {
  loadTranslations(language: string): Promise<TranslationsResult>;
}

export class DefaultBackend implements I18nBackend {
  async loadTranslations(language: string): Promise<TranslationsResult> {
    const response = await fetch(`./?v-r=i18n&langtag=${language}`);
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
