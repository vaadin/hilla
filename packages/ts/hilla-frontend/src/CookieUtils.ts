import _Cookie from 'js-cookie';

const Cookie = _Cookie.withAttributes({ path: new URL(document.baseURI).pathname.replace(/.+\/$/, '') });

export default Cookie;
