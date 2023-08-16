import type { VaadinWindow } from './types.js';

export * from './Binder.js';
export * from './Field.js';
export * from './Models.js';
export * from './Validation.js';
export * from './Validators.js';

declare const __VERSION__: string;

const $wnd = window as VaadinWindow;

$wnd.Vaadin ??= {};
$wnd.Vaadin.registrations ??= [];
$wnd.Vaadin.registrations.push({
  is: '@hilla/form',
  version: __VERSION__,
});
