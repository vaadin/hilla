import { expect } from '@open-wc/testing';
import { cookieExists, deleteCookie, getCookie, removeTrailingSlashFromPath, setCookie } from '../src/CookieUtils.js';

function setTestCookie() {
  document.cookie = `testCookie=test value`;
}

function deleteTestCookie() {
  document.cookie = `testCookie=; Expires=Thu, 01 Jan 1970 00:00:00 GMT`;
}

describe('CookieUtils', () => {
  afterEach(() => {
    deleteTestCookie();
  });

  it('should read cookie', async () => {
    setTestCookie();

    expect(getCookie('testCookie')).to.equal('test value');
    expect(getCookie('notACookie')).to.be.undefined;
  });

  it('should set cookie', async () => {
    expect(getCookie('testCookie')).to.be.undefined;
    setCookie('testCookie', 'test value');
    expect(getCookie('testCookie')).to.equal('test value');
  });

  it('should delete cookie', async () => {
    setTestCookie();

    expect(getCookie('testCookie')).to.equal('test value');
    deleteCookie('testCookie');
    expect(getCookie('testCookie')).to.be.undefined;
  });

  it('should check if cookie exists', async () => {
    expect(cookieExists('testCookie')).to.be.false;

    setTestCookie();

    expect(cookieExists('testCookie')).to.be.true;
  });
});

describe('Related helpers', () => {
  it('should remove trailing slash from path', async () => {
    expect(removeTrailingSlashFromPath('')).to.equal('');
    expect(removeTrailingSlashFromPath('/')).to.equal('/');
    expect(removeTrailingSlashFromPath('/foo')).to.equal('/foo');
    expect(removeTrailingSlashFromPath('/foo/')).to.equal('/foo');
  });
});
