// The default locale to use for renderers, filter inputs, etc.
// If undefined, the browser's locale will be used.
// Allows to modify the locale for testing purposes.
// eslint-disable-next-line import/no-mutable-exports
export let defaultLocale: string | undefined;

export function setDefaultLocale(locale: string | undefined): void {
  defaultLocale = locale;
}
