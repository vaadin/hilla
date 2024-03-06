import { batch, signal, type Signal } from '@vaadin/hilla-react-signals';
import { DefaultBackend, type I18nBackend } from './backend.js';
import { getLanguageSettings, updateLanguageSettings } from './settings.js';
import type { I18nOptions, Translations } from './types.js';

function determineInitialLanguage(options?: I18nOptions): string {
  // Use explicitly configured language if defined
  if (options?.language) {
    return options.language;
  }
  // Use last used language as fallback
  const settings = getLanguageSettings();
  if (settings?.language) {
    return settings.language;
  }
  // Otherwise use browser language
  return navigator.language;
}

export class I18n {
  readonly #backend: I18nBackend = new DefaultBackend();

  readonly #language: Signal<string | undefined> = signal(undefined);
  readonly #translations: Signal<Translations> = signal({});

  get language(): Signal<string | undefined> {
    return this.#language;
  }

  async configure(options?: I18nOptions): Promise<void> {
    const initialLanguage = determineInitialLanguage(options);
    await this.updateLanguage(initialLanguage);
  }

  async setLanguage(newLanguage: string): Promise<void> {
    await this.updateLanguage(newLanguage, true);
  }

  private async updateLanguage(newLanguage: string, updateSettings = false) {
    if (this.#language.value === newLanguage) {
      return;
    }

    const newTranslations = await this.loadTranslations(newLanguage);
    // Update all signals together to avoid triggering side effects multiple times
    batch(() => {
      this.#translations.value = newTranslations;
      this.#language.value = newLanguage;

      if (updateSettings) {
        updateLanguageSettings({
          language: newLanguage,
        });
      }
    });
  }

  private async loadTranslations(newLanguage: string) {
    try {
      return await this.#backend.loadTranslations(newLanguage);
    } catch (e) {
      // TODO proper error handling, maybe allow developer to hook into this
      console.error(`Failed to load translations for language: ${newLanguage}`, e);
      return {};
    }
  }

  translate(key: string): string {
    return this.#translations.value[key] || key;
  }
}

export const i18n = new I18n();

export function translate(key: string): string {
  return i18n.translate(key);
}
