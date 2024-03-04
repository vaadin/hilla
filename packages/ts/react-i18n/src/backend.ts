import type { Translations } from './types.js';

export interface I18nBackend {
  loadTranslations(language: string): Promise<Translations>;
}

export class DefaultBackend implements I18nBackend {
  async loadTranslations(language: string): Promise<Translations> {
    return fetch(this.getUrl(language)).then(async (response) => {
      if (!response.ok) {
        const errorMessage = this.generateErrorMessage(response);
        return Promise.reject(new Error(errorMessage));
      }
      this.logExceptionalTranslations(response, language);
      return response.json();
    });
  }

  private logExceptionalTranslations(response: Response, language: string): void {
    const retrievedLocale = response.headers.get('X-Vaadin-Retrieved-Locale');
    if (!retrievedLocale) {
      this.log('The locale for loaded translations cannot be determined.');
      return;
    }
    if (retrievedLocale === 'und') {
      if (language) {
        this.log(`Translations for ${language} cannot be loaded. Using the default translations.`);
      }
      return;
    }
    if (!this.areLanguageTagsEqual(language, retrievedLocale)) {
      this.log(
        `Translations for ${language} cannot be loaded. Using translations for the best match ${retrievedLocale} instead.`,
      );
    }
  }

  private areLanguageTagsEqual(languageTag1: string, languageTag2: string): boolean {
    return this.getTokensFromLanguageTag(languageTag1) === this.getTokensFromLanguageTag(languageTag2);
  }

  private getTokensFromLanguageTag(languageTag: string): string[] | string | null {
    if (!languageTag) {
      return null;
    }
    if (languageTag.includes('_')) {
      return languageTag.split('_');
    }
    if (languageTag.includes('-')) {
      return languageTag.split('-');
    }
    return languageTag;
  }

  private generateErrorMessage(response: Response): string {
    if (response.statusText) {
      return `Failed fetching translations with the reason: ${response.statusText}`;
    }
    return 'Failed fetching translations.';
  }

  private getUrl(language: string) {
    return `./?v-r=i18n&langtag=${language}`;
  }

  private log(logMessage: string) {
    console.warn(logMessage);
  }
}
