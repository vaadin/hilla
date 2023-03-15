import { expect } from '@esm-bundle/chai';

export const TEST_SPRING_CSRF_HEADER_NAME = 'x-xsrf-token';
export const TEST_SPRING_CSRF_TOKEN_VALUE = 'spring-csrf-token';
export const TEST_VAADIN_CSRF_TOKEN_VALUE = 'vaadin-csrf-token';

const TEST_SPRING_CSRF_META_TAG_NAME = '_csrf';

export function setupSpringCsrfMetaTags(
  csrfToken = TEST_SPRING_CSRF_TOKEN_VALUE,
  csrfMetaTagName = TEST_SPRING_CSRF_META_TAG_NAME,
) {
  let csrfMetaTag = document.head.querySelector('meta[name="_csrf"]') as HTMLMetaElement | null;
  let csrfHeaderNameMetaTag = document.head.querySelector('meta[name="_csrf_header"]') as HTMLMetaElement | null;

  if (!csrfMetaTag) {
    csrfMetaTag = document.createElement('meta');
    csrfMetaTag.name = csrfMetaTagName;
    document.head.appendChild(csrfMetaTag);
  }
  csrfMetaTag.content = csrfToken;

  if (!csrfHeaderNameMetaTag) {
    csrfHeaderNameMetaTag = document.createElement('meta');
    csrfHeaderNameMetaTag.name = '_csrf_header';
    document.head.appendChild(csrfHeaderNameMetaTag);
  }
  csrfHeaderNameMetaTag.content = TEST_SPRING_CSRF_HEADER_NAME;
}

export function clearSpringCsrfMetaTags() {
  Array.from(document.head.querySelectorAll('meta[name="_csrf"], meta[name="_csrf_header"]')).forEach((el) =>
    el.remove(),
  );
}

export function verifySpringCsrfTokenIsCleared() {
  expect(document.head.querySelector('meta[name="_csrf"]')).to.be.null;
  expect(document.head.querySelector('meta[name="_csrf_header"]')).to.be.null;
}
