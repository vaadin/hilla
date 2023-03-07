import _Cookie from 'js-cookie';

const { pathname } = new URL(document.baseURI);
const Cookie = _Cookie.withAttributes({ path: pathname.endsWith('/') ? pathname.slice(0, -1) : pathname });

export default Cookie;
