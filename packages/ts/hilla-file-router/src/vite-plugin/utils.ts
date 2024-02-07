const restParamPattern = /\{\.{3}(.+)\}/gu;
const optionalParamPattern = /\{{2}(.+)\}{2}/gu;
const paramPattern = /\{(.+)\}/gu;

/**
 * Converts a file system pattern to a URL pattern string.
 *
 * @param fsPattern - a string representing a file system pattern:
 * - `{param}` - for a required single parameter;
 * - `{{param}}` - for an optional single parameter;
 * - `{...rest}` - for multiple parameters, including none.
 *
 * @returns a string representing a URL pattern, respectively:
 * - `:param`;
 * - `:param?`;
 * - `*`.
 */
export function convertFSPatternToURLPatternString(fsPattern: string): string {
  return (
    fsPattern
      // /url/{...rest}/page -> /url/*/page
      .replaceAll(restParamPattern, '*')
      // /url/{{param}}/page -> /url/:param?/page
      .replaceAll(optionalParamPattern, ':$1?')
      // /url/{param}/page -> /url/:param/page
      .replaceAll(paramPattern, ':$1')
  );
}

/**
 * A small helper function that clears route path of the control characters in
 * order to sort the routes alphabetically.
 */
export function cleanUp(path: string): string {
  return path.replaceAll(restParamPattern, '$1').replaceAll(optionalParamPattern, '$1').replaceAll(paramPattern, '$1');
}
