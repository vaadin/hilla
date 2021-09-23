export const VAADIN_CSRF_HEADER = 'X-CSRF-Token';
export const VAADIN_CSRF_COOKIE_NAME = 'csrfToken';
export const SPRING_CSRF_COOKIE_NAME = 'XSRF-TOKEN';

function getSpringCsrfHeaderFromMetaTag(doc: Document): string {
  const csrfHeader = doc.head.querySelector('meta[name="_csrf_header"]');
  return (csrfHeader && (csrfHeader as HTMLMetaElement).content) || '';
}

function getSpringCsrfTokenFromMetaTag(doc: Document): string {
  const csrfToken = doc.head.querySelector('meta[name="_csrf"]');
  return (csrfToken && (csrfToken as HTMLMetaElement).content) || '';
}

function getCookieValue(cookieName: string): string {
  const prefix = `${cookieName}=`;
  return (
    document.cookie
      .split(/;[ ]?/)
      .filter((cookie) => cookie.startsWith(prefix))
      .map((cookie) => cookie.slice(prefix.length))[0] || ''
  );
}

export function getSpringCsrfInfo(doc: Document): Record<string, string> {
  const csrfHeader = getSpringCsrfHeaderFromMetaTag(doc);
  let csrf = getCookieValue(SPRING_CSRF_COOKIE_NAME);
  if (csrf.length === 0) {
    csrf = getSpringCsrfTokenFromMetaTag(doc);
  }
  const headers: Record<string, string> = {};
  if (csrf.length > 0 && csrfHeader.length > 0) {
    headers._csrf = csrf;
    headers._csrf_header = csrfHeader;
  }
  return headers;
}

export function getSpringCsrfTokenHeadersForAuthRequest(doc: Document): Record<string, string> {
  const csrfInfo = getSpringCsrfInfo(doc);
  const headers: Record<string, string> = {};
  if (csrfInfo._csrf && csrfInfo._csrf_header) {
    headers[csrfInfo._csrf_header] = csrfInfo._csrf;
  }
  return headers;
}

export function getCsrfTokenHeadersForEndpointRequest(doc: Document): Record<string, string> {
  const headers: Record<string, string> = {};

  const csrfInfo = getSpringCsrfInfo(doc);
  if (csrfInfo._csrf && csrfInfo._csrf_header) {
    headers[csrfInfo._csrf_header] = csrfInfo._csrf;
  } else {
    headers[VAADIN_CSRF_HEADER] = getCookieValue(VAADIN_CSRF_COOKIE_NAME);
  }

  return headers;
}
