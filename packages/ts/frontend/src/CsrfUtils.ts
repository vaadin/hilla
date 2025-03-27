import CookieManager from './CookieManager.js';

/** @internal */
export const VAADIN_CSRF_HEADER = 'X-CSRF-Token';
/** @internal */
export const VAADIN_CSRF_COOKIE_NAME = 'csrfToken';
/** @internal */
export const SPRING_CSRF_COOKIE_NAME = 'XSRF-TOKEN';

function extractContentFromMetaTag(element: HTMLMetaElement | null): string | undefined {
  if (element) {
    const value = element.content;
    if (value && value.toLowerCase() !== 'undefined') {
      return value;
    }
  }
  return undefined;
}

/** @internal */
function getSpringCsrfParameterFromMetaTag(doc: Document): string | undefined {
  const csrfParameter = doc.head.querySelector<HTMLMetaElement>('meta[name="_csrf_parameter"]');
  return extractContentFromMetaTag(csrfParameter);
}

/** @internal */
function getSpringCsrfHeaderFromMetaTag(doc: Document): string | undefined {
  const csrfHeader = doc.head.querySelector<HTMLMetaElement>('meta[name="_csrf_header"]');
  return extractContentFromMetaTag(csrfHeader);
}

/** @internal */
function getSpringCsrfTokenFromMetaTag(doc: Document): string | undefined {
  const csrfToken = doc.head.querySelector<HTMLMetaElement>('meta[name="_csrf"]');
  return extractContentFromMetaTag(csrfToken);
}

/** @internal */
export function getSpringCsrfInfo(doc: Document): Record<string, string> {
  const csrfParameter = getSpringCsrfParameterFromMetaTag(doc);
  const csrfHeader = getSpringCsrfHeaderFromMetaTag(doc);
  let csrf = CookieManager.get(SPRING_CSRF_COOKIE_NAME);
  if (!csrf || csrf.length === 0) {
    csrf = getSpringCsrfTokenFromMetaTag(doc);
  }
  const headers: Record<string, string> = {};
  if (csrf && (csrfParameter || csrfHeader)) {
    headers._csrf = csrf;
    if (csrfParameter) {
      // eslint-disable-next-line camelcase
      headers._csrf_parameter = csrfParameter;
    }
    if (csrfHeader) {
      // eslint-disable-next-line camelcase
      headers._csrf_header = csrfHeader;
    }
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
