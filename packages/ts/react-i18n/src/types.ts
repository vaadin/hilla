export type Translations = Record<string, string>;

export const enum SuccessfulResponseType {
  EXACT_MATCH = 0,
  UNDETERMINED = 1,
  FALLBACK_TO_DEFAULT = 2,
  BEST_MATCH = 3,
}

export interface I18nOptions {
  language?: string;
  allowedResponseTypes?: Set<SuccessfulResponseType>;
}
