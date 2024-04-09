import { RouteParamType } from '../shared/routeParamType.js';

const routeParamTypeMap: ReadonlyMap<RouteParamType, RegExp> = new Map([
  [RouteParamType.Wildcard, /\{\.{3}(.+)\}/gu],
  [RouteParamType.Optional, /\{{2}(.+)\}{2}/gu],
  [RouteParamType.Required, /\{(.+)\}/gu],
]);

// eslint-disable-next-line consistent-return
function getReplacer(type: RouteParamType): string {
  // eslint-disable-next-line default-case
  switch (type) {
    case RouteParamType.Wildcard:
      return '*';
    case RouteParamType.Optional:
      return ':$1?';
    case RouteParamType.Required:
      return ':$1';
  }
}

/**
 * Converts a file system pattern to an URL pattern string.
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
  let res = segment;

  routeParamTypeMap.forEach((pattern, type) => {
    res = res.replaceAll(pattern, getReplacer(type));
  });

  return res;
}

/**
 * Extracts the parameter name and its type from the route segment.
 *
 * @param segment - A part of the FS route URL.
 * @returns A map of parameter names and their types.
 */
export function extractParameterFromRouteSegment(segment: string): Readonly<Record<string, RouteParamType>> {
  let _segment = segment;
  const params: Record<string, RouteParamType> = {};

  for (const [type, pattern] of routeParamTypeMap) {
    const _pattern = new RegExp(pattern.source, pattern.flags);
    _segment = _segment.replaceAll(_pattern, (match) => {
      const key = match.replaceAll(pattern, getReplacer(type));
      params[key] = type;
      return '';
    });
  }

  return params;
}

/**
 * A small helper function that clears route path of the control characters in
 * order to sort the routes alphabetically.
 */
export function cleanUp(path: string): string {
  let res = path;

  for (const pattern of routeParamTypeMap.values()) {
    res = res.replaceAll(pattern, '$1');
  }

  return res;
}
