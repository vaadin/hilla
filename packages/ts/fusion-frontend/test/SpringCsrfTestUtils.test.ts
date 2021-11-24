import { expect } from '@open-wc/testing';

export const TET_SPRING_CSRF_HEADER_NAME = 'x-xsrf-token';
export const TEST_SPRING_CSRF_TOKEN_VALUE = 'spring-csrf-token';
export const TEST_VAADIN_CSRF_TOKEN_VALUE = 'vaadin-csrf-token';

export function setupSpringCsrfMetaTags(csrfToken = TEST_SPRING_CSRF_TOKEN_VALUE) {
  let csrfMetaTag = document.head.querySelector('meta[name="_csrf"]') as HTMLMetaElement | null;
  let csrfHeaderNameMetaTag = document.head.querySelector('meta[name="_csrf_header"]') as HTMLMetaElement | null;

  if (!csrfMetaTag) {
    csrfMetaTag = document.createElement('meta');
    csrfMetaTag.name = '_csrf';
    document.head.appendChild(csrfMetaTag);
  }
  csrfMetaTag.content = csrfToken;

  if (!csrfHeaderNameMetaTag) {
    csrfHeaderNameMetaTag = document.createElement('meta');
    csrfHeaderNameMetaTag.name = '_csrf_header';
    document.head.appendChild(csrfHeaderNameMetaTag);
  }
  csrfHeaderNameMetaTag.content = TET_SPRING_CSRF_HEADER_NAME;
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

export function setCookie(name: string, value: string): void {
  document.cookie = `${name}=${value}`;
}

export function clearCookie(name: string): void {
  document.cookie = `${name}=; Expires=Thu, 01 Jan 1970 00:00:00 GMT`;
}
