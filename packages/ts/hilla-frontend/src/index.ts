import type { VaadinWindow } from './types.js';

export * from './Authentication.js';
export * from './Connect.js';
export * from './EndpointErrors.js';
export { FluxConnection, State } from './FluxConnection.js';

declare const __VERSION__: string;

const $wnd = window as VaadinWindow;

$wnd.Vaadin ??= {};
$wnd.Vaadin.registrations ??= [];
$wnd.Vaadin.registrations.push({
  is: '@hilla/frontend',
  version: /* updated-by-script */ '2.2.0-alpha10',
});
