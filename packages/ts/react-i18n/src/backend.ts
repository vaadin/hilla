import type { Translations } from './types.js';

export interface I18nBackend {
  loadTranslations(language: string): Promise<Translations>;
}

export class DefaultBackend implements I18nBackend {
  async loadTranslations(language: string): Promise<Translations> {
    const url = `./?v-r=i18n&langtag=${language}`;
    return fetch(url).then(async (response) => {
      if (response.status === 200) {
        return response.json();
      }
      return Promise.reject(new Error(`Failed to load translations for language: ${language}`));
    });
  }
}
