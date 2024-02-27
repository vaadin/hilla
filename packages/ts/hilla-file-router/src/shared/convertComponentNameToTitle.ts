const viewPattern = /view/giu;
const upperCaseSplitPattern = /(?=[A-Z])/gu;

/**
 * Converts the name of the component to a page title.
 *
 * @param component - The component to convert the name from.
 *
 * @returns The page title.
 */
export function convertComponentNameToTitle(component?: unknown): string | undefined {
  let name: string | undefined;

  if (typeof component === 'string') {
    name = component;
  } else if (
    component &&
    (typeof component === 'object' || typeof component === 'function') &&
    'name' in component &&
    typeof component.name === 'string'
  ) {
    ({ name } = component);
  }

  if (name) {
    return name.replace(viewPattern, '').split(upperCaseSplitPattern).join(' ');
  }

  return undefined;
}
