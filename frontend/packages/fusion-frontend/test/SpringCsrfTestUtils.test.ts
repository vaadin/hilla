import { expect } from '@open-wc/testing';
export const springCsrfToken = 'spring-csrf-token';
export const springCsrfHeaderName = 'x-xsrf-token';
export const springCsrfCookieName = 'XSRF-TOKEN';
export const vaadinCsrfToken = 'vaadin-csrf-token';

export function setupSpringCsrfMetaTags(csrfToken = springCsrfToken) {
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
  csrfHeaderNameMetaTag.content = springCsrfHeaderName;
}

export function clearSpringCsrfMetaTags() {
  Array.from(document.head.querySelectorAll('meta[name="_csrf"], meta[name="_csrf_header"]')).forEach((el) =>
    el.remove()
  );
}

export function verifySpringCsrfTokenIsCleared() {
  expect(document.head.querySelector('meta[name="_csrf"]')).to.be.null;
  expect(document.head.querySelector('meta[name="_csrf_header"]')).to.be.null;
}

export function setCookie(name: string, value: string) {
  document.cookie = name + '=' + value;
}

export function clearCookie(name: string) {
  setCookie(name, '');
}
