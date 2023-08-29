import type { VaadinWindow } from './types.js';

export * from './Binder.js';
export * from './BinderRoot.js';
export * from './Field.js';
export * from './Models.js';
export * from './Validation.js';
export * from './Validators.js';
export * from './Validity.js';

declare const __VERSION__: string;

const $wnd = window as VaadinWindow;

$wnd.Vaadin ??= {};
$wnd.Vaadin.registrations ??= [];
$wnd.Vaadin.registrations.push({
  is: '@hilla/form',
  version: /* updated-by-script */ '2.2.0-alpha10',
});
