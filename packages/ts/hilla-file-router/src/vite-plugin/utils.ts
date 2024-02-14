const wildcardParamPattern = /\{\.{3}(.+)\}/gu;
const optionalParamPattern = /\{{2}(.+)\}{2}/gu;
const paramPattern = /\{(.+)\}/gu;

/**
 * The type of route parameter.
 */
export enum RouteParamType {
  Required = 'req',
  Optional = 'opt',
  Wildcard = '*',
}

const routeParamTypeMap: ReadonlyMap<RegExp, RouteParamType> = new Map([
  [wildcardParamPattern, RouteParamType.Wildcard],
  [optionalParamPattern, RouteParamType.Optional],
  [paramPattern, RouteParamType.Required],
]);

/**
 * Converts a file system pattern to a URL pattern string.
 *
 * @param segment - a string representing a file system pattern:
 * - `{param}` - for a required single parameter;
 * - `{{param}}` - for an optional single parameter;
 * - `{...wildcard}` - for multiple parameters, including none.
 *
 * @returns a string representing a URL pattern, respectively:
 * - `:param`;
 * - `:param?`;
 * - `*`.
 */
export function convertFSRouteSegmentToURLPatternFormat(segment: string): string {
  return (
    segment
      // /url/{...wildcard}/page -> /url/*/page
      .replaceAll(wildcardParamPattern, '*')
      // /url/{{param}}/page -> /url/:param?/page
      .replaceAll(optionalParamPattern, ':$1?')
      // /url/{param}/page -> /url/:param/page
      .replaceAll(paramPattern, ':$1')
  );
}

/**
 * Extracts the parameter name and its type from the route segment.
 *
 * @param segment - A part of the FS route URL.
 * @returns A map of parameter names and their types.
 */
export function extractParameterFromRouteSegment(segment: string): Readonly<Record<string, RouteParamType>> {
  const params: Record<string, RouteParamType> = {};

  routeParamTypeMap.forEach((type, pattern) => {
    let match: RegExpExecArray | null;
    while ((match = pattern.exec(segment)) != null) {
      params[match[1]] = type;
    }
  });

  return params;
}

/**
 * A small helper function that clears route path of the control characters in
 * order to sort the routes alphabetically.
 */
export function cleanUp(path: string): string {
  return path
    .replaceAll(wildcardParamPattern, '$1')
    .replaceAll(optionalParamPattern, '$1')
    .replaceAll(paramPattern, '$1');
}
