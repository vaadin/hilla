function getSpringCsrfHeaderFromDocument(doc: Document): string | undefined {
  const csrfHeader = doc.head.querySelector('meta[name="_csrf_header"]');
  if (csrfHeader) {
    return (csrfHeader as HTMLMetaElement).content;
  }
  return undefined;
}

function getSpringCsrfTokenFromDocument(doc: Document): string | undefined {
  const csrfToken = doc.head.querySelector('meta[name="_csrf"]');
  if (csrfToken) {
    return (csrfToken as HTMLMetaElement).content;
  }
  return undefined;
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
  const csrf = getCsrfTokenFromCookie('XSRF-TOKEN=') ?? getSpringCsrfTokenFromDocument(doc);
  const headers: Record<string, string> = {};
  if (csrf && csrfHeader) {
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
