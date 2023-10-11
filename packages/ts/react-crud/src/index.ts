import type { VaadinWindow } from './vaadin.types.js';

export * from './autogrid.js';
export type * from './crud.js';
export * from './autoform.js';
export * from './autocrud.js';

declare const __VERSION__: string;

const $wnd = window as VaadinWindow;

$wnd.Vaadin ??= {};
$wnd.Vaadin.registrations ??= [];
$wnd.Vaadin.registrations.push({
  is: '@hilla/react-grid',
  version: __VERSION__,
});
