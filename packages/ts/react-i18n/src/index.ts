import { batch, computed, type ReadonlySignal, signal, type Signal } from '@vaadin/hilla-react-signals';
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

const keyLiteralMarker: unique symbol = Symbol('keyMarker');

/**
 * A type for translation keys. It is a string with a special marker.
 */
export type I18nKey = string & { [keyLiteralMarker]: unknown };

export class I18n {
  readonly #backend: I18nBackend = new DefaultBackend();

  readonly #initialized: Signal<boolean> = signal(false);
  readonly #language: Signal<string | undefined> = signal(undefined);
  readonly #translations: Signal<Translations> = signal({});
  readonly #resolvedLanguage: Signal<string | undefined> = signal(undefined);
  readonly #chunks = new Set<string>();
  readonly #alreadyRequestedKeys: Signal<ReadonlySet<string>> = signal(new Set());
  readonly #batchedKeys = new Set<string>();
  #batchedKeysPromise: Promise<ReadonlySet<string>> | undefined = undefined;

  #formatCache: FormatCache = new FormatCache(navigator.language);
  readonly #translationSignalCache = new Map<string, ReadonlySignal<string>>();

  constructor() {
    // @ts-expect-error import.meta.hot does not have TS definitions
    if (import.meta.hot) {
      // @ts-expect-error import.meta.hot does not have TS definitions
      // eslint-disable-next-line
      import.meta.hot.on('translations-update', () => {
        // eslint-disable-next-line @typescript-eslint/no-floating-promises
        this.reloadTranslations();
      });
    }
  }

  /**
   * Returns a signal indicating whether the I18n instance has been initialized.
   * The instance is considered initialized after `configure` has been called
   * and translations for the initial language have been loaded. Can be used to
   * show a placeholder or loading indicator until the translations are ready.
   *
   * Subscribers to this signal will be notified when initialization is complete
   * and translations are ready to be used.
   */
  get initialized(): ReadonlySignal<boolean> {
    return this.#initialized;
  }

  /**
   * Returns a signal with the currently configured language.
   *
   * Subscribers to this signal will be notified when the language has changed
   * and new translations have been loaded.
   */
  get language(): ReadonlySignal<string | undefined> {
    return this.#language;
  }

  /**
   * Returns a signal with the resolved language. The resolved language is the
   * language that was actually used to load translations. It may differ from
   * the configured language if there are no translations available for the
   * configured language. For example, when setting the language to "de-DE" but
   * translations are only available for "de", the resolved language will be
   * "de".
   */
  get resolvedLanguage(): ReadonlySignal<string | undefined> {
    return this.#resolvedLanguage;
  }

  /**
   * Initializes the I18n instance. This method should be called once to load
   * translations for the initial language. The `translate` API will not return
   * properly translated strings until the initializations has completed.
   *
   * The initialization runs asynchronously as translations are loaded from the
   * backend. The method returns a promise that resolves when the translations
   * have been loaded, after which the `translate` API can safely be used.
   *
   * The initial language is determined as follows:
   * 1. If a user opens the app for the first time, the browser language is used.
   * 2. If the language has been changed in a previous usage of the app using
   * `setLanguage`, the last used language is used. The last used language is
   * automatically stored in local storage.
   *
   * Alternatively, the initial language can be explicitly configured using the
   * `language` option. The language should be a valid IETF BCP 47 language tag,
   * such as `en` or `en-US`.
   *
   * @param options - Optional options object to specify the initial language.
   */
  async configure(options?: I18nOptions): Promise<void> {
    const language = determineInitialLanguage(options);
    await this.updateLanguage({ language });
  }

  /**
   * Changes the current language and loads translations for the new language.
   * Components using the `translate` API will automatically re-render, and
   * subscribers to the `language` signal will be notified, when the new
   * translations have been loaded.
   *
   * The language should be a valid IETF BCP 47 language tag, such as `en` or
   * `en-US`.
   *
   * If there is no translation file for that specific language tag, the backend
   * will try to load the translation file for the parent language tag. For
   * example, if there is no translation file for `en-US`, the backend will try
   * to load the translation file for `en`. Otherwise, it will fall back to the
   * default translation file `translations.properties`.
   *
   * Changing the language is an asynchronous operation. The method returns a
   * promise that resolves when the translations for the new language have been
   * loaded.
   *
   * @param newLanguage - a valid IETF BCP 47 language tag, such as `en` or `en-US`
   */
  async setLanguage(newLanguage: string): Promise<void> {
    await this.updateLanguage({ language: newLanguage, updateSettings: true });
  }

  /**
   * Registers the chunk name for loading translations, and loads the
   * translations for the specified chunk.
   *
   * @internal only for automatic internal calls from production JS bundles
   *
   * @param chunkName - the production JS bundle chunk name
   */
  async registerChunk(chunkName: string): Promise<void> {
    if (this.#chunks.has(chunkName)) {
      return;
    }

    this.#chunks.add(chunkName);

    if (this.#language.value) {
      await this.updateLanguage({ chunkName });
    }
  }

  private async requestKeys(keys: readonly string[]): Promise<void> {
    if (this.#batchedKeysPromise) {
      // Keys request is being queued - add keys to the batch.
      for (const key of keys) {
        this.#batchedKeys.add(key);
      }
      return;
    }

    const nonBatchedKeys = keys.filter((key) => !this.#batchedKeys.has(key));
    if (nonBatchedKeys.length === 0) {
      // Keys request for these is already in progress - skip another request.
      return;
    }

    // New request
    this.#batchedKeys.clear();
    for (const key of nonBatchedKeys) {
      this.#batchedKeys.add(key);
    }
    // Wait to possibly collect other synchronously requested keys
    this.#batchedKeysPromise = Promise.resolve(this.#batchedKeys);
    await this.#batchedKeysPromise;
    this.#batchedKeysPromise = undefined;
    const batchedKeys = [...this.#batchedKeys];
    return this.updateLanguage({ keys: batchedKeys }).then(() => {
      console.warn(['A server call was made to translate keys those were not loaded:', ...batchedKeys].join('\n  - '));
    });
  }

  private async updateLanguage(
    options:
      | { readonly language: string; updateSettings?: boolean }
      | { readonly chunkName: string }
      | { readonly keys: string[] },
  ) {
    const { language, updateSettings, chunkName, keys } = {
      language: this.#language.value,
      updateSettings: false,
      chunkName: undefined,
      keys: undefined,
      ...options,
    };

    const partialLoad = !!chunkName || !!keys?.length;

    if (language === undefined || (language === this.#language.value && !partialLoad)) {
      return;
    }

    const chunks = chunkName
      ? [chunkName] // New chunk is registered, load only that
      : this.#chunks.size && !keys?.length
        ? [...this.#chunks.values()] // Load the new language for all chunks registered so far
        : undefined; // Load the new language without specifying chunks, assuming dev. mode or keys requested

    let translationsResult: TranslationsResult;
    try {
      translationsResult = await this.#backend.loadTranslations(language, chunks, keys);
    } catch (e) {
      console.error(`Failed to load translations for language: ${language}`, e);
      return;
    }

    // Update all signals together to avoid triggering side effects multiple times
    batch(() => {
      this.#translations.value = partialLoad
        ? { ...this.#translations.value, ...translationsResult.translations }
        : translationsResult.translations;
      this.#resolvedLanguage.value = translationsResult.resolvedLanguage;
      if (language !== this.#language.value) {
        this.#language.value = language;
        this.#formatCache = new FormatCache(language);
      }
      this.#initialized.value = !!language;

      if (!partialLoad) {
        this.#alreadyRequestedKeys.value = new Set<string>();
      } else if (keys?.length) {
        this.#alreadyRequestedKeys.value = new Set([...this.#alreadyRequestedKeys.value, ...keys]);
      }

      if (updateSettings) {
        updateLanguageSettings({ language });
      }
    });
  }

  /**
   * Reloads all translations for the current language. This method should only
   * be used for HMR in development mode.
   */
  private async reloadTranslations() {
    const currentLanguage = this.#language.value;
    if (!currentLanguage) {
      return;
    }

    let translationsResult: TranslationsResult;
    try {
      translationsResult = await this.#backend.loadTranslations(currentLanguage);
    } catch (e) {
      console.error(`Failed to reload translations for language: ${currentLanguage}`, e);
      return;
    }

    // Update all signals together to avoid triggering side effects multiple times
    batch(() => {
      this.#translations.value = translationsResult.translations;
      this.#resolvedLanguage.value = translationsResult.resolvedLanguage;
      this.#formatCache = new FormatCache(currentLanguage);
    });
  }

  /**
   * Returns a translated string for the given translation key. The key should
   * match a key in the loaded translations. If no translation is found for the
   * key, the key itself is returned.
   *
   * Translations may contain placeholders, following the ICU MessageFormat
   * syntax. They can be replaced by passing a `params` object with placeholder
   * values, where keys correspond to the placeholder names and values are the
   * replacement value. Values should be basic types such as strings, numbers,
   * or dates that match the placeholder format configured in the translation
   * string. For example, when using a placeholder `{count, number}`, the value
   * should be a number, when using `{date, date}`, the value should be a Date
   * object, and so on.
   *
   * This method internally accesses a signal, meaning that React components
   * that use it will automatically re-render when translations change.
   * Likewise, signal effects automatically subscribe to translation changes
   * when calling this method.
   *
   * @param key - The key to translate
   * @param params - Optional object with placeholder values
   */
  translate(key: I18nKey, params?: Record<string, unknown>): string {
    const translation = this.#translations.value[key];
    if (!translation) {
      return this.handleMissingTranslation(key);
    }
    const format = this.#formatCache.getFormat(translation);
    return format.format(params) as string;
  }

  /**
   * Creates a computed signal with translated string for to the given key.
   * This method uses dynamic loading and does not guarantee immediate
   * availability of the translation.
   *
   * If the translation for the given key has not been loaded yet at the time
   * of the call, loads the translation for the key and updates the returned
   * signal value.
   *
   * When given an `undefined` key, returns empty string signal value.
   *
   * @param key - The translation key to translate
   * @param params - Optional object with placeholder values
   */
  translateDynamic(key: string | undefined, params?: Record<string, unknown>): ReadonlySignal<string> {
    // Return a signal that depends on #translations and #language signals.
    // If the key is not found, it will wait for a language to be defined
    // and then try to load the key from the server.
    if (this.#translationSignalCache.has(key ?? '')) {
      return this.#translationSignalCache.get(key ?? '')!;
    }

    if (!key) {
      const translationSignal = computed(() => '');
      this.#translationSignalCache.set('', translationSignal);
      return translationSignal;
    }

    const translationSignal = computed(() => {
      const translation = this.#translations.value[key];

      if (!translation) {
        if (this.#alreadyRequestedKeys.value.has(key)) {
          // No hope to load this key, return it as is
          return this.handleMissingTranslation(key);
        }

        if (this.#language.value) {
          // eslint-disable-next-line no-void
          void this.requestKeys([key]);
        }

        // Prevent flashing the key in the UI
        return '';
      }

      const format = this.#formatCache.getFormat(translation);
      return format.format(params) as string;
    });

    this.#translationSignalCache.set(key, translationSignal);
    return translationSignal;
  }

  private handleMissingTranslation(key: string): string {
    const lang = this.#language.value ? `${this.#language.value.split(/[_-]/u)[0]}: ` : '';
    return `!${lang}${key}`;
  }
}

/**
 * The global I18n instance that is used to initialize translations, change the
 * current language, and translate strings.
 */
const i18n: I18n = new I18n();

/**
 * A tagged template literal function to create translation keys.
 * The {@link translate} function requires using this tag.
 * E.g.:
 *   translate(key`my.translation.key`)
 */
function keyTag(strings: readonly string[], ..._values: never[]): I18nKey {
  return Object.assign(strings[0], { [keyLiteralMarker]: undefined }) as I18nKey;
}

/**
 * Returns a translated string for the given translation key. The key should
 * match a key in the loaded translations. If no translation is found for the
 * key, a modified version of the key is returned to indicate that the translation
 * is missing.
 *
 * Translations may contain placeholders, following the ICU MessageFormat
 * syntax. They can be replaced by passing a `params` object with placeholder
 * values, where keys correspond to the placeholder names and values are the
 * replacement value. Values should be basic types such as strings, numbers,
 * or dates that match the placeholder format configured in the translation
 * string. For example, when using a placeholder `{count, number}`, the value
 * should be a number, when using `{date, date}`, the value should be a Date
 * object, and so on.
 *
 * This method internally accesses a signal, meaning that React components
 * that use it will automatically re-render when translations change.
 * Likewise, signal effects automatically subscribe to translation changes
 * when calling this method.
 *
 * This function is a shorthand for `i18n.translate` of the global I18n instance.
 *
 * @param key - The translation key to translate
 * @param params - Optional object with placeholder values
 */
export function translate(key: I18nKey, params?: Record<string, unknown>): string {
  return i18n.translate(key, params);
}

export { i18n, keyTag as key };
