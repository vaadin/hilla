export * from './ConnectionState.js';
export * from './ConnectionIndicator.js';

const $wnd = window as any;
$wnd.Vaadin = $wnd.Vaadin || {};
$wnd.Vaadin.registrations = $wnd.Vaadin.registrations || [];
$wnd.Vaadin.registrations.push({
  is: '@vaadin/common-frontend',
  version: /* updated-by-script */ '0.0.10',
});
