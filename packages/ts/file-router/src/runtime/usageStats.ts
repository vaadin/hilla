/* eslint no-undef: 0 */
/* eslint @typescript-eslint/no-unsafe-member-access: 0 */
/* eslint @typescript-eslint/no-unsafe-call: 0 */
/**
 * Reports the usage of the given feature to the Vaadin usage statistics - internal.
 *
 * @param name - The name of the feature.
 * @param packageVersion - The version of the feature.
 */
export function reportUsage(name: string, packageVersion?: string): void {
  window.Vaadin ??= {};
  window.Vaadin.registrations ??= [];
  window.Vaadin.registrations.push({
    is: name,
    version: packageVersion ?? '?',
  });
}
