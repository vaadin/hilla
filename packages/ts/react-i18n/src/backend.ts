import type { Translations, TranslationsResult } from './types.js';

export interface I18nBackend {
  loadTranslations(language: string): Promise<TranslationsResult>;
}

export class DefaultBackend implements I18nBackend {
  async loadTranslations(language: string): Promise<TranslationsResult> {
    return fetch(`./?v-r=i18n&langtag=${language}`).then(async (response) => {
      if (!response.ok) {
        return Promise.reject(new Error('Failed fetching translations.'));
      }
      const retrievedLocale = response.headers.get('X-Vaadin-Retrieved-Locale');
      const translations: Translations = await response.json().then((t) => t);
      const translationsResult: TranslationsResult = {
        translations,
        resolvedLanguage: retrievedLocale ?? undefined,
      };
      return Promise.resolve(translationsResult);
    });
  }
}
