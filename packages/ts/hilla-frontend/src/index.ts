export * from './Authentication.js';
export * from './Connect.js';
export { FluxConnection, State } from './FluxConnection.js';

declare const __VERSION__: string;

/* c8 ignore next 2 */
window.Vaadin ??= {};
window.Vaadin.registrations ??= [];
window.Vaadin.registrations.push({
  is: '@hilla/frontend',
  version: __VERSION__,
});
