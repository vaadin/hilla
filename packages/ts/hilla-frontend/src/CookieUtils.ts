import Cookies from 'js-cookie';

const { pathname } = new URL(document.baseURI);

const _Cookies: Cookies.CookiesStatic<string> = Cookies.withAttributes({
  path: pathname.endsWith('/') ? pathname.slice(0, -1) : pathname,
});

export default _Cookies;
