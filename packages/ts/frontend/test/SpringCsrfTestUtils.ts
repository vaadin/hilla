/* eslint-disable @typescript-eslint/no-unused-expressions */
import { expect } from 'vitest';

export const TEST_SPRING_CSRF_HEADER_NAME = 'x-xsrf-token';
export const TEST_SPRING_CSRF_TOKEN_VALUE = 'spring-csrf-token';
export const TEST_VAADIN_CSRF_TOKEN_VALUE = 'vaadin-csrf-token';

export const TEST_SPRING_CSRF_META_TAG_NAME = '_csrf';

export function setupSpringCsrfMetaTags(
  csrfToken: string = TEST_SPRING_CSRF_TOKEN_VALUE,
  csrfMetaTagName: string = TEST_SPRING_CSRF_META_TAG_NAME,
): void {
  let csrfMetaTag = document.head.querySelector<HTMLMetaElement>('meta[name="_csrf"]');
  let csrfHeaderNameMetaTag = document.head.querySelector<HTMLMetaElement>('meta[name="_csrf_header"]');
  let csrfParameterNameMetaTag = document.head.querySelector<HTMLMetaElement>('meta[name="_csrf_parameter"]');

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

  if (!csrfParameterNameMetaTag) {
    csrfParameterNameMetaTag = document.createElement('meta');
    csrfParameterNameMetaTag.name = '_csrf_parameter';
    document.head.appendChild(csrfParameterNameMetaTag);
  }
  csrfParameterNameMetaTag.content = '_csrf';
}

export function clearSpringCsrfMetaTags(): void {
  Array.from(
    document.head.querySelectorAll('meta[name="_csrf"], meta[name="_csrf_header"], meta[name="_csrf_parameter"]'),
  ).forEach((el) => el.remove());
}

export function verifySpringCsrfTokenIsCleared(): void {
  expect(document.head.querySelector('meta[name="_csrf"]')).to.be.null;
  expect(document.head.querySelector('meta[name="_csrf_header"]')).to.be.null;
  expect(document.head.querySelector('meta[name="_csrf_parameter"]')).to.be.null;
}
