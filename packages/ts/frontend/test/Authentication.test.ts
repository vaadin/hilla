import { expect, use } from '@esm-bundle/chai';
import chaiDom from 'chai-dom';
import fetchMock from 'fetch-mock';
import sinon from 'sinon';
import sinonChai from 'sinon-chai';
import CookieManager from '../src/CookieManager.js';
import { VAADIN_CSRF_HEADER } from '../src/CsrfUtils.js';
import {
  ConnectClient,
  InvalidSessionMiddleware,
  login,
  type LoginResult,
  logout,
  type OnInvalidSessionCallback,
} from '../src/index.js';
import {
  clearSpringCsrfMetaTags,
  setupSpringCsrfMetaTags,
  TEST_SPRING_CSRF_HEADER_NAME,
  TEST_SPRING_CSRF_TOKEN_VALUE,
  TEST_VAADIN_CSRF_TOKEN_VALUE,
  verifySpringCsrfTokenIsCleared,
} from './SpringCsrfTestUtils.test.js';

use(sinonChai);
use(chaiDom);

// `connectClient.call` adds the host and context to the endpoint request.
// we need to add this origin when configuring fetch-mock
const base = window.location.origin;

const JWT_COOKIE_NAME = 'jwt.headerAndPayload';

describe('@vaadin/hilla-frontend', () => {
  describe('Authentication', () => {
    const requestHeaders: Record<string, string> = {};
    const vaadinCsrfToken = '6a60700e-852b-420f-a126-a1c61b73d1ba';
    const happyCaseLogoutResponseText = `<head><meta name="_csrf" content="spring-csrf-token"></meta><meta name="_csrf_header" content="${TEST_SPRING_CSRF_HEADER_NAME}"></meta></head>"}};</script>`;
    const happyCaseLoginResponseText = '';
    const happyCaseResponseHeaders = {
      'Default-url': '/',
      Result: 'success',
      'Spring-CSRF-header': TEST_SPRING_CSRF_HEADER_NAME,
      'Spring-CSRF-token': TEST_SPRING_CSRF_TOKEN_VALUE,
      'Vaadin-CSRF': vaadinCsrfToken,
    };

    function verifySpringCsrfToken(token: string) {
      expect(document.head.querySelector('meta[name="_csrf"]')).to.have.attribute('content', token);
      expect(document.head.querySelector('meta[name="_csrf_header"]')).to.have.attribute(
        'content',
        TEST_SPRING_CSRF_HEADER_NAME,
      );
    }

    beforeEach(() => {
      setupSpringCsrfMetaTags();
      requestHeaders[TEST_SPRING_CSRF_HEADER_NAME] = TEST_SPRING_CSRF_TOKEN_VALUE;
    });
    afterEach(() => {
      // delete window.Vaadin.TypeScript;
      clearSpringCsrfMetaTags();
    });

    describe('login', () => {
      afterEach(() => {
        fetchMock.restore();
      });

      it('should return an error on invalid credentials', async () => {
        fetchMock.post('/login', { redirectUrl: '/login?error' }, { headers: requestHeaders });
        const result = await login('invalid-username', 'invalid-password');
        const expectedResult: LoginResult = {
          error: true,
          errorMessage: 'Check that you have entered the correct username and password and try again.',
          errorTitle: 'Incorrect username or password.',
        };

        expect(fetchMock.calls()).to.have.lengthOf(1);
        expect(result).to.deep.equal(expectedResult);
      });

      it('should return a CSRF token on valid credentials', async () => {
        fetchMock.post(
          '/login',
          {
            body: happyCaseLoginResponseText,
            headers: happyCaseResponseHeaders,
          },
          { headers: requestHeaders },
        );
        const result = await login('valid-username', 'valid-password');
        const expectedResult: LoginResult = {
          defaultUrl: '/',
          error: false,
          redirectUrl: undefined,
          token: vaadinCsrfToken,
        };

        expect(fetchMock.calls()).to.have.lengthOf(1);
        expect(result).to.deep.equal(expectedResult);
      });

      it('should set the csrf tokens on login', async () => {
        fetchMock.post(
          '/login',
          {
            body: happyCaseLoginResponseText,
            headers: {
              ...happyCaseResponseHeaders,
              'Spring-CSRF-header': TEST_SPRING_CSRF_HEADER_NAME,
              'Spring-CSRF-token': 'some-new-spring-token',
              'Vaadin-CSRF': 'some-new-token',
            },
          },
          { headers: requestHeaders },
        );
        await login('valid-username', 'valid-password');
        verifySpringCsrfToken('some-new-spring-token');
      });

      it('should redirect based on request cache after login', async () => {
        // An unthenticated request attempt would be captured by the default
        // request cache, so after login, it should redirect the user to that
        // request
        fetchMock.post(
          '/login',
          {
            body: happyCaseLoginResponseText,
            // mock the unthenticated attempt, which would be
            // saved by the default request cache
            headers: { ...happyCaseResponseHeaders, 'Saved-url': '/protected-view' },
          },
          { headers: requestHeaders },
        );
        const result = await login('valid-username', 'valid-password');
        const expectedResult: LoginResult = {
          defaultUrl: '/',
          error: false,
          redirectUrl: '/protected-view',
          token: vaadinCsrfToken,
        };

        expect(fetchMock.calls()).to.have.lengthOf(1);
        expect(result).to.deep.equal(expectedResult);
      });
    });

    describe('logout', () => {
      let href: sinon.SinonStub;
      let location: Pick<Location, 'href'>;

      beforeEach(() => {
        href = sinon.stub();
        location = Object.defineProperty({ href: '' }, 'href', {
          get() {},
          set: href,
        });
        CookieManager.set(JWT_COOKIE_NAME, 'jwtCookieMockValue');
      });

      afterEach(() => {
        CookieManager.remove(JWT_COOKIE_NAME);
      });

      it('should reload the page on logout', () => {
        logout({ location });
        expect(href).to.have.been.calledOnceWithExactly('/logout');
        expect(CookieManager.get(JWT_COOKIE_NAME)).to.be.undefined;
      });

      it('should accept a custom logout URL', () => {
        logout({ logoutUrl: '/custom-logout', location });
        expect(href).to.have.been.calledOnceWithExactly('/custom-logout');
        expect(CookieManager.get(JWT_COOKIE_NAME)).to.be.undefined;
      });
    });

    describe('InvalidSessionMiddleWare', () => {
      afterEach(() => fetchMock.restore());

      it('should invoke the onInvalidSession callback on 401 response', async () => {
        fetchMock.post(`${base}/connect/FooEndpoint/fooMethod`, 401);

        const invalidSessionCallback = sinon.spy(() => {
          // mock to pass authentication
          fetchMock.restore();
          fetchMock.post(`${base}/connect/FooEndpoint/fooMethod`, { fooData: 'foo' });

          return {
            error: false,
            token: TEST_VAADIN_CSRF_TOKEN_VALUE,
          };
        });

        const middleware = new InvalidSessionMiddleware(invalidSessionCallback as unknown as OnInvalidSessionCallback);

        const client = new ConnectClient({ middlewares: [middleware] });

        await client.call('FooEndpoint', 'fooMethod');

        expect(invalidSessionCallback.calledOnce).to.be.true;

        expect(fetchMock.lastOptions()?.headers).to.deep.include({
          [VAADIN_CSRF_HEADER.toLowerCase()]: TEST_VAADIN_CSRF_TOKEN_VALUE,
        });
      });

      it('should not invoke the onInvalidSession callback on 200 response', async () => {
        fetchMock.post(`${base}/connect/FooEndpoint/fooMethod`, { fooData: 'foo' });

        const invalidSessionCallback = sinon.spy();
        const middleware = new InvalidSessionMiddleware(invalidSessionCallback);

        const client = new ConnectClient({ middlewares: [middleware] });
        await client.call('FooEndpoint', 'fooMethod');

        expect(invalidSessionCallback.called).to.be.false;
      });
    });
  });
});
