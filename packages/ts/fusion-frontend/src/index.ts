export * from './Authentication.js';
export * from './Connect.js';

const $wnd = window as any;
/* c8 ignore next 2 */
$wnd.Vaadin = $wnd.Vaadin || {};
$wnd.Vaadin.registrations = $wnd.Vaadin.registrations || [];
$wnd.Vaadin.registrations.push({
  is: '@vaadin/fusion-frontend',
  version: /* updated-by-script */ '0.0.17',
});
