import { batch, type ReadonlySignal, signal, type Signal } from '@vaadin/hilla-react-signals';
import { DefaultBackend, type I18nBackend } from './backend.js';
import { FormatCache } from './FormatCache.js';
import { getLanguageSettings, updateLanguageSettings } from './settings.js';
import type { I18nOptions, Translations, TranslationsResult } from './types.js';

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

  readonly #initialized: Signal<boolean> = signal(false);
  readonly #language: Signal<string | undefined> = signal(undefined);
  readonly #translations: Signal<Translations> = signal({});
  readonly #resolvedLanguage: Signal<string | undefined> = signal(undefined);
  #formatCache: FormatCache = new FormatCache(navigator.language);

  constructor() {
    // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
    if (!(window as any).Vaadin?.featureFlags?.hillaI18n) {
      // Remove when removing feature flag
      throw new Error(
        `The Hilla I18n API is currently considered experimental and may change in the future. To use it you need to explicitly enable it in Copilot or by adding com.vaadin.experimental.hillaI18n=true to vaadin-featureflags.properties`,
      );
    }
  }

  get initialized(): ReadonlySignal<boolean> {
    return this.#initialized;
  }

  get language(): ReadonlySignal<string | undefined> {
    return this.#language;
  }

  get resolvedLanguage(): ReadonlySignal<string | undefined> {
    return this.#resolvedLanguage;
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

    let translationsResult: TranslationsResult;
    try {
      translationsResult = await this.#backend.loadTranslations(newLanguage);
    } catch (e) {
      console.error(`Failed to load translations for language: ${newLanguage}`, e);
      return;
    }

    // Update all signals together to avoid triggering side effects multiple times
    batch(() => {
      this.#translations.value = translationsResult.translations;
      this.#language.value = newLanguage;
      this.#resolvedLanguage.value = translationsResult.resolvedLanguage;
      this.#formatCache = new FormatCache(newLanguage);
      this.#initialized.value = true;

      if (updateSettings) {
        updateLanguageSettings({
          language: newLanguage,
        });
      }
    });
  }

  translate(key: string, params?: Record<string, unknown>): string {
    const translation = this.#translations.value[key];
    if (!translation) {
      return key;
    }
    const format = this.#formatCache.getFormat(translation);
    return format.format(params) as string;
  }
}

export const i18n = new I18n();

export function translate(key: string, params?: Record<string, unknown>): string {
  return i18n.translate(key, params);
}
