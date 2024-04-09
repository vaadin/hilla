import { IntlMessageFormat } from 'intl-messageformat';

export class FormatCache {
  readonly #language: string;
  readonly #formats = new Map<string, IntlMessageFormat>();

  constructor(language: string) {
    // Ensure that the language is supported by Intl.NumberFormat, which IntlMessageFormat uses internally
    // Fall back to navigator.language if the given language is not supported
    let supportedLocales: string[] = [];
    try {
      supportedLocales = Intl.NumberFormat.supportedLocalesOf(language);
    } catch (e) {}
    this.#language = supportedLocales.length > 0 ? supportedLocales[0] : navigator.language;
  }

  getFormat(translation: string): IntlMessageFormat {
    let format = this.#formats.get(translation);
    if (!format) {
      format = new IntlMessageFormat(translation, this.#language);
      this.#formats.set(translation, format);
    }
    return format;
  }
}
