export function __REGISTER__() {
  window.Vaadin ??= {};
  window.Vaadin.registrations ??= [];
  window.Vaadin.registrations.push({ is: __NAME__, version: __VERSION__ });
}
