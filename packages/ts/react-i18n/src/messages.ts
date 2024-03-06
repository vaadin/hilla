import { IntlMessageFormat } from 'intl-messageformat';

export class Messages {
  readonly #messages: Record<string, IntlMessageFormat | string>;
  readonly #language: string;

  constructor(messages: Record<string, string>, language: string) {
    this.#messages = messages;
    this.#language = language;
  }

  format(key: string, params?: Record<string, unknown>): string {
    let messageOrFormat = this.#messages[key];
    if (!messageOrFormat) {
      return key;
    }
    if (!(messageOrFormat instanceof IntlMessageFormat)) {
      messageOrFormat = new IntlMessageFormat(messageOrFormat, this.#language);
      this.#messages[key] = messageOrFormat;
    }
    return messageOrFormat.format(params) as string;
  }
}
