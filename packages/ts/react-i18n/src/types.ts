export type Translations = Readonly<Record<string, string>>;

export type TranslationsResult = Readonly<{
  translations: Translations;
  resolvedLanguage?: string;
}>;

export interface I18nOptions {
  /**
   * Allows to explicitly set the initial language. Should be a valid
   * IETF BCP 47 language tag, such as `en` or `en-US`
   */
  language?: string;
}
