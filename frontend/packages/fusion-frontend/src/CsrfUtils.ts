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

export function getSpringCsrfInfoFromDocument(doc: Document): Record<string, string> {
  const csrfHeader = getSpringCsrfHeaderFromDocument(doc);
  let csrf = getCsrfTokenFromCookie('XSRF-TOKEN=');
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
  const csrfInfo = getSpringCsrfInfoFromDocument(doc);
  const headers: Record<string, string> = {};
  if (csrfInfo._csrf && csrfInfo._csrf_header) {
    headers[csrfInfo._csrf_header] = csrfInfo._csrf;
  }
  return headers;
}
