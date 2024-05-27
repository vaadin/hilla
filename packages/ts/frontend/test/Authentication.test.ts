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
  login as originalLogin,
  type LoginOptions,
  type LoginResult,
  logout as originalLogout,
  type LogoutOptions,
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

    const navigate = sinon.fake<[string], void>();
    const onSuccess = sinon.fake.resolves(undefined);

    const login = async (username: string, password: string, loginOptions?: LoginOptions) =>
      originalLogin(username, password, {
        navigate,
        ...(loginOptions ?? {}),
      });
    const logout = async (logoutOptions?: LogoutOptions) =>
      originalLogout({
        navigate,
        ...(logoutOptions ?? {}),
      });

    beforeEach(() => {
      setupSpringCsrfMetaTags();
      requestHeaders[TEST_SPRING_CSRF_HEADER_NAME] = TEST_SPRING_CSRF_TOKEN_VALUE;
    });
    afterEach(() => {
      // delete window.Vaadin.TypeScript;
      clearSpringCsrfMetaTags();
      onSuccess.resetHistory();
      navigate.resetHistory();
    });

    describe('login', () => {
      afterEach(() => {
        fetchMock.restore();
      });

      it('should return an error on invalid credentials', async () => {
        fetchMock.post('/login', { redirectUrl: '/login?error' }, { headers: requestHeaders });
        const result = await login('invalid-username', 'invalid-password', { onSuccess });
        const expectedResult: LoginResult = {
          error: true,
          errorMessage: 'Check that you have entered the correct username and password and try again.',
          errorTitle: 'Incorrect username or password.',
        };

        expect(fetchMock.calls()).to.have.lengthOf(1);
        expect(result).to.deep.equal(expectedResult);
        expect(onSuccess).to.not.be.called;
        expect(navigate).to.not.be.called;
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
        expect(navigate).to.be.calledOnceWithExactly('/');
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
        const result = await login('valid-username', 'valid-password', { onSuccess });
        const expectedResult: LoginResult = {
          defaultUrl: '/',
          error: false,
          redirectUrl: '/protected-view',
          token: vaadinCsrfToken,
        };

        expect(fetchMock.calls()).to.have.lengthOf(1);
        expect(result).to.deep.equal(expectedResult);
        expect(onSuccess).to.be.calledBefore(navigate);
        expect(navigate).to.be.calledOnceWithExactly('/protected-view');
      });
    });

    describe('logout', () => {
      afterEach(() => {
        fetchMock.restore();
        CookieManager.remove(JWT_COOKIE_NAME);
      });

      it('should set the csrf token on logout', async () => {
        fetchMock.post(
          '/logout',
          {
            body: happyCaseLogoutResponseText,
            redirectUrl: '/logout?login',
          },
          { headers: requestHeaders },
        );
        await logout();
        expect(fetchMock.calls()).to.have.lengthOf(1);
        verifySpringCsrfToken(TEST_SPRING_CSRF_TOKEN_VALUE);
        expect(navigate).to.be.calledOnceWithExactly('/logout?login');
      });

      it('should clear the csrf tokens on failed server logout', async () => {
        const fakeError = new Error('unable to connect');
        fetchMock.post(
          '/logout',
          () => {
            throw fakeError;
          },
          { headers: requestHeaders },
        );
        fetchMock.get('?nocache', {
          body: happyCaseLogoutResponseText,
        });

        let thrownError;
        try {
          await logout({ onSuccess });
        } catch (err) {
          thrownError = err;
        }
        expect(thrownError).to.equal(fakeError);
        expect(fetchMock.calls()).to.have.lengthOf(3);
        verifySpringCsrfTokenIsCleared();
        expect(navigate).to.not.be.called;
      });

      it('should clear the JWT cookie on logout', async () => {
        fetchMock.post(
          '/logout',
          {
            body: happyCaseLogoutResponseText,
            redirectUrl: '/logout?login',
          },
          { headers: requestHeaders },
        );

        CookieManager.set(JWT_COOKIE_NAME, 'jwtCookieMockValue');
        await logout();

        expect(fetchMock.calls()).to.have.lengthOf(1);
        expect(CookieManager.get(JWT_COOKIE_NAME)).to.be.undefined;
        expect(navigate).to.be.calledOnceWithExactly('/logout?login');
      });

      it('should clear the JWT cookie on failed server logout', async () => {
        const fakeError = new Error('unable to connect');
        fetchMock.post('/logout', () => {
          throw fakeError;
        });
        fetchMock.get('?nocache', () => {
          throw fakeError;
        });

        CookieManager.set(JWT_COOKIE_NAME, 'jwtCookieMockValue');
        let thrownError;
        try {
          await logout();
        } catch (err) {
          thrownError = err;
        }
        expect(thrownError).to.equal(fakeError);
        expect(CookieManager.get(JWT_COOKIE_NAME)).to.be.undefined;
        expect(navigate).to.not.be.called;
      });

      // when started the app offline, the spring csrf meta tags are not available
      it('should retry when no spring csrf metas in the doc', async () => {
        clearSpringCsrfMetaTags();

        verifySpringCsrfTokenIsCleared();
        fetchMock.post('/logout', 403, { repeat: 1 });
        fetchMock.get('?nocache', {
          body: happyCaseLogoutResponseText,
        });
        fetchMock.post(
          '/logout',
          {
            body: happyCaseLogoutResponseText,
            redirectUrl: '/logout?login',
          },
          { headers: requestHeaders, overwriteRoutes: false, repeat: 1 },
        );
        await logout();
        expect(fetchMock.calls()).to.have.lengthOf(3);
        verifySpringCsrfToken(TEST_SPRING_CSRF_TOKEN_VALUE);
        expect(navigate).to.be.calledOnceWithExactly('/logout?login');
      });

      // when started the app offline, the spring csrf meta tags are not available
      it('should retry when no spring csrf metas in the doc and clear the csrf token on failed server logout with the retry', async () => {
        clearSpringCsrfMetaTags();

        verifySpringCsrfTokenIsCleared();

        fetchMock.post('/logout', 403, { repeat: 1 });
        fetchMock.get('?nocache', {
          body: happyCaseLogoutResponseText,
        });
        const fakeError = new Error('server error');
        fetchMock.post(
          '/logout',
          () => {
            throw fakeError;
          },
          { headers: requestHeaders, overwriteRoutes: false, repeat: 1 },
        );

        let thrownError;
        try {
          await logout();
        } catch (err) {
          thrownError = err;
        }
        expect(thrownError).to.equal(fakeError);
        expect(fetchMock.calls()).to.have.lengthOf(3);
        expect(navigate).to.not.be.called;

        setupSpringCsrfMetaTags();
      });

      // when the page has been opend too long the session has expired
      it('should retry when expired spring csrf metas in the doc', async () => {
        const expiredSpringCsrfToken = `expired-${TEST_SPRING_CSRF_TOKEN_VALUE}`;

        setupSpringCsrfMetaTags(expiredSpringCsrfToken);

        const headersWithExpiredSpringCsrfToken: Record<string, string> = {};
        headersWithExpiredSpringCsrfToken[TEST_SPRING_CSRF_HEADER_NAME] = expiredSpringCsrfToken;

        fetchMock.post('/logout', 403, { headers: headersWithExpiredSpringCsrfToken, repeat: 1 });
        fetchMock.get('?nocache', {
          body: happyCaseLogoutResponseText,
        });
        fetchMock.post(
          '/logout',
          {
            body: happyCaseLogoutResponseText,
            redirectUrl: '/logout?login',
          },
          { headers: requestHeaders, overwriteRoutes: false, repeat: 1 },
        );
        await logout({ onSuccess });
        expect(fetchMock.calls()).to.have.lengthOf(3);
        verifySpringCsrfToken(TEST_SPRING_CSRF_TOKEN_VALUE);
        expect(onSuccess).to.be.calledBefore(navigate);
        expect(navigate).to.be.calledOnceWithExactly('/logout?login');
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
