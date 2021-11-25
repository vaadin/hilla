/** @internal */
export function getCookie(name: string): string | undefined {
  const prefix = `${name}=`;
  return document.cookie
    .split(/;[ ]?/)
    .filter((cookie) => cookie.startsWith(prefix))
    .map((cookie) => cookie.slice(prefix.length))[0];
}

/** @internal */
export function setCookie(name: string, value: string, options?: Record<string, string>): void {
  const _options = {
    [name]: value,
    ...options,
  };
  document.cookie = Object.entries(_options)
    .map(([key, val]) => `${key}=${val}`)
    .join('; ');
}

/** @internal */
export function deleteCookie(name: string, options?: Record<string, string>): void {
  setCookie(name, '', { Expires: 'Thu, 01 Jan 1970 00:00:00 GMT', ...options });
}

/** @internal */
export function cookieExists(name: string): boolean {
  return getCookie(name) !== undefined;
}

/**
 * Remove trailing '/' if the path is not exactly '/'.
 * @internal
 */
export function removeTrailingSlashFromPath(path: string) {
  if (path.length > 1 && path.substr(-1, 1) === '/') {
    path = path.substr(0, path.length - 1);
  }
  return path;
}
