import { expect } from '@open-wc/testing';
import fetchMock from 'fetch-mock/esm/client';
import sinon from 'sinon';
import { ConnectClient, InvalidSessionMiddleware, login, LoginResult, logout, OnInvalidSessionCallback } from '../src';
import { VAADIN_CSRF_HEADER } from '../src/CsrfUtils';
import {
  clearSpringCsrfMetaTags,
  setupSpringCsrfMetaTags,
  TEST_SPRING_CSRF_TOKEN_VALUE,
  TEST_VAADIN_CSRF_TOKEN_VALUE,
  TET_SPRING_CSRF_HEADER_NAME,
  verifySpringCsrfTokenIsCleared,
} from './SpringCsrfTestUtils.test';

// `connectClient.call` adds the host and context to the endpoint request.
// we need to add this origin when configuring fetch-mock
const base = window.location.origin;
const $wnd = window as any;

describe('Authentication', () => {
  const requestHeaders: Record<string, string> = {};
  const vaadinCsrfToken = '6a60700e-852b-420f-a126-a1c61b73d1ba';
  const happyCaseLogoutResponseText = `<head><meta name="_csrf" content="spring-csrf-token"></meta><meta name="_csrf_header" content="${TET_SPRING_CSRF_HEADER_NAME}"></meta></head>"}};</script>`;
  const happyCaseLoginResponseText = '';
  const happyCaseResponseHeaders = {
    'Vaadin-CSRF': vaadinCsrfToken,
    Result: 'success',
    'Default-url': '/',
    'Spring-CSRF-header': TET_SPRING_CSRF_HEADER_NAME,
    'Spring-CSRF-token': TEST_SPRING_CSRF_TOKEN_VALUE,
  };
  let originalCookie;

  function verifySpringCsrfToken(token: string) {
    expect(document.head.querySelector('meta[name="_csrf"]')!.getAttribute('content')).to.equal(token);
    expect(document.head.querySelector('meta[name="_csrf_header"]')!.getAttribute('content')).to.equal(
      TET_SPRING_CSRF_HEADER_NAME
    );
  }

  beforeEach(() => {
    setupSpringCsrfMetaTags();
    originalCookie = document.cookie;
    requestHeaders[TET_SPRING_CSRF_HEADER_NAME] = TEST_SPRING_CSRF_TOKEN_VALUE;
  });
  afterEach(() => {
    // @ts-ignore
    delete window.Vaadin.TypeScript;
    document.cookie = originalCookie;
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
        errorTitle: 'Incorrect username or password.',
        errorMessage: 'Check that you have entered the correct username and password and try again.',
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
        { headers: requestHeaders }
      );
      const result = await login('valid-username', 'valid-password');
      const expectedResult: LoginResult = {
        error: false,
        token: vaadinCsrfToken,
        defaultUrl: '/',
        redirectUrl: undefined,
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
            'Vaadin-CSRF': 'some-new-token',
            'Spring-CSRF-header': TET_SPRING_CSRF_HEADER_NAME,
            'Spring-CSRF-token': 'some-new-spring-token',
          },
        },
        { headers: requestHeaders }
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
        { headers: requestHeaders }
      );
      const result = await login('valid-username', 'valid-password');
      const expectedResult: LoginResult = {
        error: false,
        token: vaadinCsrfToken,
        defaultUrl: '/',
        redirectUrl: '/protected-view',
      };

      expect(fetchMock.calls()).to.have.lengthOf(1);
      expect(result).to.deep.equal(expectedResult);
    });
  });

  describe('logout', () => {
    afterEach(() => fetchMock.restore());

    it('should set the csrf token on logout', async () => {
      fetchMock.post(
        '/logout',
        {
          body: happyCaseLogoutResponseText,
          redirectUrl: '/logout?login',
        },
        { headers: requestHeaders }
      );
      await logout();
      expect(fetchMock.calls()).to.have.lengthOf(1);
    });

    it('should clear the csrf tokens on failed server logout', async () => {
      const fakeError = new Error('unable to connect');
      fetchMock.post(
        '/logout',
        () => {
          throw fakeError;
        },
        { headers: requestHeaders }
      );
      fetchMock.get('?nocache', {
        body: happyCaseLogoutResponseText,
      });
      try {
        await logout();
      } catch (err) {
        expect(err).to.equal(fakeError);
      }
      expect(fetchMock.calls()).to.have.lengthOf(3);
      verifySpringCsrfTokenIsCleared();
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
        { headers: requestHeaders, overwriteRoutes: false, repeat: 1 }
      );
      await logout();
      expect(fetchMock.calls()).to.have.lengthOf(3);
      verifySpringCsrfToken(TEST_SPRING_CSRF_TOKEN_VALUE);
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
        { headers: requestHeaders, overwriteRoutes: false, repeat: 1 }
      );

      try {
        await logout();
      } catch (err) {
        expect(err).to.equal(fakeError);
      }
      expect(fetchMock.calls()).to.have.lengthOf(3);

      setupSpringCsrfMetaTags();
    });

    // when the page has been opend too long the session has expired
    it('should retry when expired spring csrf metas in the doc', async () => {
      const expiredSpringCsrfToken = `expired-${TEST_SPRING_CSRF_TOKEN_VALUE}`;

      setupSpringCsrfMetaTags(expiredSpringCsrfToken);

      const headersWithExpiredSpringCsrfToken: Record<string, string> = {};
      headersWithExpiredSpringCsrfToken[TET_SPRING_CSRF_HEADER_NAME] = expiredSpringCsrfToken;

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
        { headers: requestHeaders, overwriteRoutes: false, repeat: 1 }
      );
      await logout();
      expect(fetchMock.calls()).to.have.lengthOf(3);
      verifySpringCsrfToken(TEST_SPRING_CSRF_TOKEN_VALUE);
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
