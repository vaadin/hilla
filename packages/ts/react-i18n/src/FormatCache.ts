import { IntlMessageFormat } from 'intl-messageformat';

export class FormatCache {
  readonly #language: string;
  readonly #formats = new Map<string, IntlMessageFormat>();

  constructor(language: string) {
    this.#language = language;
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
