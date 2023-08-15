export * from './Authentication.js';
export * from './Connect.js';
export { FluxConnection, State } from './FluxConnection.js';

const $wnd = window as any;
/* c8 ignore next 2 */
$wnd.Vaadin = $wnd.Vaadin || {};
$wnd.Vaadin.registrations = $wnd.Vaadin.registrations || [];
$wnd.Vaadin.registrations.push({
  is: '@hilla/frontend',
  version: /* updated-by-script */ '2.2.0-alpha6',
});
