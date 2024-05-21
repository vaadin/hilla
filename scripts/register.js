export function __REGISTER__(feature, vaadinObj = (window.Vaadin ??= {})) {
  vaadinObj.registrations ??= [];
  vaadinObj.registrations.push({
    is: feature ? `${__NAME__}/${feature}` : __NAME__,
    version: __VERSION__,
  });
}
