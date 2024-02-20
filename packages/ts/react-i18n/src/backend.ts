import type { Translations } from './types.js';

export interface I18nBackend {
  loadTranslations(language: string): Promise<Translations>;
}

export class DefaultBackend implements I18nBackend {
  // eslint-disable-next-line @typescript-eslint/require-await
  async loadTranslations(language: string): Promise<Translations> {
    return {};
  }
}
