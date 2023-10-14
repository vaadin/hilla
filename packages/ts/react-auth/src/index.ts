import type { VaadinWindow } from './types.js';

export * from './ProtectedRoute.js';
export * from './useAuth.js';

declare const __VERSION__: string;

const $wnd = window as VaadinWindow;

$wnd.Vaadin ??= {};
$wnd.Vaadin.registrations ??= [];
$wnd.Vaadin.registrations.push({
  is: '@hilla/react-auth',
  version: __VERSION__,
});
