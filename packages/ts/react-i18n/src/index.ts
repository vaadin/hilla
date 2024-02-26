import { signal, type Signal } from '@vaadin/hilla-react-signals';
import { DefaultBackend, type I18nBackend } from './backend.js';
import type { I18nOptions, Translations } from './types.js';

function determineInitialLanguage(options?: I18nOptions): string {
  // Use explicitly configured language or browser language
  return options?.language ?? navigator.language;
}

export class I18n {
  private readonly _backend: I18nBackend = new DefaultBackend();

  private readonly _language: Signal<string | undefined> = signal(undefined);
  private readonly _translations: Signal<Translations> = signal({});

  get language(): Signal<string | undefined> {
    return this._language;
  }

  async configure(options?: I18nOptions): Promise<void> {
    const initialLanguage = determineInitialLanguage(options);
    await this.setLanguage(initialLanguage);
  }

  async setLanguage(newLanguage: string): Promise<void> {
    if (this._language.value === newLanguage) {
      return;
    }

    this._translations.value = await this.loadTranslations(newLanguage);
    this._language.value = newLanguage;
  }

  private async loadTranslations(newLanguage: string) {
    try {
      return await this._backend.loadTranslations(newLanguage);
    } catch (e) {
      // TODO proper error handling, maybe allow developer to hook into this
      console.error(`Failed to load translations for language: ${newLanguage}`, e);
      return {};
    }
  }

  translate(key: string): string {
    return this._translations.value[key] || key;
  }
}
