import type { Translations } from './types.js';
import { SuccessfulResponseType } from './types.js';

export interface I18nBackend {
  loadTranslations(language: string): Promise<Translations>;

  get allowedResponseTypes(): Set<SuccessfulResponseType>;

  set allowedResponseTypes(allowedResponseTypes: Set<SuccessfulResponseType>);
}

export class DefaultBackend implements I18nBackend {
  readonly #allowedResponseTypes: Set<SuccessfulResponseType> = new Set<SuccessfulResponseType>([
    SuccessfulResponseType.EXACT_MATCH,
    SuccessfulResponseType.UNDETERMINED,
    SuccessfulResponseType.FALLBACK_TO_DEFAULT,
    SuccessfulResponseType.BEST_MATCH,
  ]);

  async loadTranslations(language: string): Promise<Translations> {
    return fetch(`./?v-r=i18n&langtag=${language}`).then(async (response) => {
      if (!response.ok) {
        return Promise.reject(new Error('Failed fetching translations.'));
      }
      const retrievedLocale = response.headers.get('X-Vaadin-Retrieved-Locale');
      const successfulResponseType = this.getSuccessfulResponseType(language, retrievedLocale);
      if (!this.#allowedResponseTypes.has(successfulResponseType)) {
        return Promise.reject(
          new Error(this.getNotAllowedResponseMessage(successfulResponseType, retrievedLocale, language)),
        );
      }
      return response.json();
    });
  }

  get allowedResponseTypes(): Set<SuccessfulResponseType> {
    return this.#allowedResponseTypes;
  }

  set allowedResponseTypes(allowedResponseTypes: Set<SuccessfulResponseType>) {
    this.#allowedResponseTypes.clear();
    allowedResponseTypes.forEach((allowedResponseType) => this.#allowedResponseTypes.add(allowedResponseType));
  }

  private getNotAllowedResponseMessage(
    successfulResponseType: SuccessfulResponseType,
    retrievedLocale: string | null,
    requestedLocale: string,
  ) {
    switch (successfulResponseType) {
      case SuccessfulResponseType.BEST_MATCH:
        return `Best match locale ${retrievedLocale} in response for requested locale ${requestedLocale} is not allowed.`;
      case SuccessfulResponseType.UNDETERMINED:
        return `Undetermined locale in response for requested locale ${requestedLocale} is not allowed.`;
      case SuccessfulResponseType.FALLBACK_TO_DEFAULT:
        return `Fallback to default in response for requested locale ${requestedLocale} is not allowed.`;
      default:
        return `Locale in response ${retrievedLocale} is not allowed for requested locale ${requestedLocale}.`;
    }
  }

  private getSuccessfulResponseType(requestedLocale: string, retrievedLocale: string | null): SuccessfulResponseType {
    if (!retrievedLocale) {
      return SuccessfulResponseType.UNDETERMINED;
    }
    if (retrievedLocale === 'und') {
      if (requestedLocale) {
        return SuccessfulResponseType.FALLBACK_TO_DEFAULT;
      }
    } else if (!this.areLocalesEqual(requestedLocale, retrievedLocale)) {
      return SuccessfulResponseType.BEST_MATCH;
    }
    return SuccessfulResponseType.EXACT_MATCH;
  }

  private areLocalesEqual(locale1: string, locale2: string): boolean {
    return this.getTokensFromLocale(locale1) === this.getTokensFromLocale(locale2);
  }

  private getTokensFromLocale(locale: string): string[] | string | null {
    if (!locale) {
      return null;
    }
    if (locale.includes('_')) {
      return locale.split('_');
    }
    if (locale.includes('-')) {
      return locale.split('-');
    }
    return locale;
  }
}
