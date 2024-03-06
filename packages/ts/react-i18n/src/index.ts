import { batch, signal, type Signal } from '@vaadin/hilla-react-signals';
import { DefaultBackend, type I18nBackend } from './backend.js';
import { Messages } from './messages';
import { getLanguageSettings, updateLanguageSettings } from './settings.js';
import type { I18nOptions } from './types.js';

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
  readonly #messages: Signal<Messages> = signal(new Messages({}, ''));

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
      this.#messages.value = new Messages(newTranslations, newLanguage);
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

  translate(key: string, params?: Record<any, unknown>): string {
    return this.#messages.value.format(key, params);
  }
}

export const i18n = new I18n();

export function translate(key: string, params?: Record<any, unknown>): string {
  return i18n.translate(key, params);
}
