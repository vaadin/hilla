export function __REGISTER__(feature) {
  window.Vaadin ??= {};
  window.Vaadin.registrations ??= [];
  window.Vaadin.registrations.push({
    is: feature ? `${__NAME__}/${feature}` : __NAME__,
    version: __VERSION__,
  });
}
