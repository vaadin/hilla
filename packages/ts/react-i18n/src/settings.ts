import CookieManager from '@vaadin/hilla-frontend/CookieManager.js';

export const VAADIN_LANGUAGE_SETTINGS_COOKIE_NAME = 'vaadinLanguageSettings';

export interface LanguageSettings {
  language?: string;
}

export function getLanguageSettings(): LanguageSettings | undefined {
  const cookie = CookieManager.get(VAADIN_LANGUAGE_SETTINGS_COOKIE_NAME);
  if (!cookie) return undefined;

  try {
    return JSON.parse(cookie);
  } catch (e) {
    // Ignore
    return undefined;
  }
}

export function updateLanguageSettings(updates: Partial<LanguageSettings>): void {
  const settings = getLanguageSettings() ?? {};
  const newSettings = {
    ...settings,
    ...updates,
  };
  const json = JSON.stringify(newSettings);
  CookieManager.set(VAADIN_LANGUAGE_SETTINGS_COOKIE_NAME, json);
}
