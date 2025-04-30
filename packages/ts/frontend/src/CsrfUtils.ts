import CookieManager from './CookieManager.js';

/** @internal */
export const VAADIN_CSRF_HEADER = 'X-CSRF-Token';
/** @internal */
export const VAADIN_CSRF_COOKIE_NAME = 'csrfToken';
/** @internal */
export const SPRING_CSRF_COOKIE_NAME = 'XSRF-TOKEN';

function extractContentFromMetaTag(doc: Document, metaTag: string): string | undefined {
  const element = doc.head.querySelector<HTMLMetaElement>(`meta[name="${metaTag}"]`);
  if (element) {
    const value = element.content;
    if (value && value.toLowerCase() !== 'undefined') {
      return value;
    }
  }
  return undefined;
}

export function getSpringCsrfInfo(doc: Document): Record<string, string> {
  let csrf = CookieManager.get(SPRING_CSRF_COOKIE_NAME);
  if (!csrf || csrf.length === 0) {
    csrf = extractContentFromMetaTag(doc, '_csrf');
  }
  const csrfHeader = extractContentFromMetaTag(doc, '_csrf_header');

  const headers: Record<string, string> = {};
  if (csrf && csrfHeader) {
    headers._csrf = csrf;
    // eslint-disable-next-line camelcase
    headers._csrf_header = csrfHeader;

    const csrfParameter = extractContentFromMetaTag(doc, '_csrf_parameter');
    if (csrfParameter) {
      // eslint-disable-next-line camelcase
      headers._csrf_parameter = csrfParameter;
    }
  }
  return headers;
}

/** @internal */
export function getSpringCsrfTokenHeadersForAuthRequest(doc: Document): Record<string, string> {
  const csrfInfo = getSpringCsrfInfo(doc);
  const headers: Record<string, string> = {};
  if (csrfInfo._csrf && csrfInfo._csrf_header) {
    headers[csrfInfo._csrf_header] = csrfInfo._csrf;
  }
  return headers;
}

/** @internal */
export function getCsrfTokenHeadersForEndpointRequest(doc: Document): Record<string, string> {
  const headers: Record<string, string> = {};

  const csrfInfo = getSpringCsrfInfo(doc);
  if (csrfInfo._csrf && csrfInfo._csrf_header) {
    headers[csrfInfo._csrf_header] = csrfInfo._csrf;
  } else {
    headers[VAADIN_CSRF_HEADER] = CookieManager.get(VAADIN_CSRF_COOKIE_NAME) ?? '';
  }

  return headers;
}

/** @internal */
export function getSpringCsrfTokenParametersForAuthRequest(doc: Document): Record<string, string> {
  const csrfInfo = getSpringCsrfInfo(doc);
  const parameters: Record<string, string> = {};
  if (csrfInfo._csrf && csrfInfo._csrf_parameter) {
    parameters[csrfInfo._csrf_parameter] = csrfInfo._csrf;
  }
  return parameters;
}
