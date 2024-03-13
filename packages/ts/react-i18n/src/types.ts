export type Translations = Record<string, string>;

export type TranslationsResult = {
  translations: Translations;
  resolvedLanguage?: string;
};

export interface I18nOptions {
  language?: string;
}
