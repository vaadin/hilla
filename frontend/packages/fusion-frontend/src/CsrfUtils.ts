export const VAADIN_CSRF_HEADER = 'X-CSRF-Token';
export const VAADIN_CSRF_COOKIE_PREFIX = 'csrfToken=';

function getSpringCsrfHeaderFromDocument(doc: Document): string {
  const csrfHeader = doc.head.querySelector('meta[name="_csrf_header"]');
  return (csrfHeader && (csrfHeader as HTMLMetaElement).content) || '';
}

function getSpringCsrfTokenFromDocument(doc: Document): string {
  const csrfToken = doc.head.querySelector('meta[name="_csrf"]');
  return (csrfToken && (csrfToken as HTMLMetaElement).content) || '';
}

export function getCsrfTokenFromCookie(csrfCookieNamePrefix: string): string {
  return (
    document.cookie
      .split(/;[ ]?/)
      .filter((cookie) => cookie.startsWith(csrfCookieNamePrefix))
      .map((cookie) => cookie.slice(csrfCookieNamePrefix.length))[0] || ''
  );
}

export function getSpringCsrfInfo(doc: Document): Record<string, string> {
  const csrfHeader = getSpringCsrfHeaderFromDocument(doc);
  const SPRING_CSRF_COOKIE_PREFIX = 'XSRF-TOKEN=';
  let csrf = getCsrfTokenFromCookie(SPRING_CSRF_COOKIE_PREFIX);
  if (csrf.length === 0) {
    csrf = getSpringCsrfTokenFromDocument(doc);
  }
  const headers: Record<string, string> = {};
  if (csrf.length > 0 && csrfHeader.length > 0) {
    headers._csrf = csrf;
    headers._csrf_header = csrfHeader;
  }
  return headers;
}

export function getSpringCsrfTokenHeadersFromDocument(doc: Document): Record<string, string> {
  const csrfInfo = getSpringCsrfInfo(doc);
  const headers: Record<string, string> = {};
  if (csrfInfo._csrf && csrfInfo._csrf_header) {
    headers[csrfInfo._csrf_header] = csrfInfo._csrf;
  }
  return headers;
}

export function getCsrfTokenHeaders(doc: Document): Record<string, string> {
  const headers: Record<string, string> = {};

  const csrfInfo = getSpringCsrfInfo(doc);
  if (csrfInfo._csrf && csrfInfo._csrf_header) {
    headers[csrfInfo._csrf_header] = csrfInfo._csrf;
  } else {
    headers[VAADIN_CSRF_HEADER] = getCsrfTokenFromCookie(VAADIN_CSRF_COOKIE_PREFIX);
  }

  return headers;
}
