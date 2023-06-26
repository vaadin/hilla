import Cookies from 'js-cookie';

export function calculatePath({ pathname }: URL) {
  return pathname.length > 1 && pathname.endsWith('/') ? pathname.slice(0, -1) : pathname;
}

const CookieManager: Cookies.CookiesStatic = Cookies.withAttributes({
  path: calculatePath(new URL(document.baseURI)),
});

export default CookieManager;
