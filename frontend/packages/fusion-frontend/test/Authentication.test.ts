import { expect } from '@open-wc/testing';
import fetchMock from 'fetch-mock/esm/client';
import sinon from 'sinon';
import { ConnectClient, InvalidSessionMiddleware, login, LoginResult, logout, OnInvalidSessionCallback } from '../src';

// `connectClient.call` adds the host and context to the endpoint request.
// we need to add this origin when configuring fetch-mock
const base = window.location.origin;
const $wnd = window as any;

describe('Authentication', () => {
  const springCsrfToken = 'spring-csrf-token';
  const springCsrfHeaderName = 'X-CSRF-TOKEN';
  const requestHeaders: Record<string, string> = {};
  const vaadinCsrfToken = '6a60700e-852b-420f-a126-a1c61b73d1ba';
  const happyCaseLogoutResponseText = `<head><meta name="_csrf" content="spring-csrf-token"></meta><meta name="_csrf_header" content="X-CSRF-TOKEN"></meta></head><script>window.Vaadin = {TypeScript: {"csrfToken":"${vaadinCsrfToken}"}};</script>`;
  const happyCaseLoginResponseText = '';
  const happyCaseResponseHeaders = {
    'Vaadin-CSRF': vaadinCsrfToken,
    Result: 'success',
    'Default-url': '/',
    'Spring-CSRF-header': springCsrfHeaderName,
    'Spring-CSRF-token': springCsrfToken,
  };

  function verifySpringCsrfToken(token: string) {
    expect(document.head.querySelector('meta[name="_csrf"]')!.getAttribute('content')).to.equal(token);
    expect(document.head.querySelector('meta[name="_csrf_header"]')!.getAttribute('content')).to.equal(
      springCsrfHeaderName
    );
  }
  function verifySpringCsrfTokenIsCleared() {
    expect(document.head.querySelector('meta[name="_csrf"]')).to.be.null;
    expect(document.head.querySelector('meta[name="_csrf_header"]')).to.be.null;
  }

  function clearSpringCsrfMetaTags() {
    Array.from(document.head.querySelectorAll('meta[name="_csrf"], meta[name="_csrf_header"]')).forEach((el) =>
      el.remove()
    );
  }
  function setupSpringCsrfMetaTags(csrfToken = springCsrfToken) {
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
  beforeEach(() => {
    setupSpringCsrfMetaTags();
    requestHeaders[springCsrfHeaderName] = springCsrfToken;
  });
  afterEach(() => {
    // @ts-ignore
    delete window.Vaadin.TypeScript;
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
            'Spring-CSRF-header': 'X-CSRF-TOKEN',
            'Spring-CSRF-token': 'some-new-spring-token',
          },
        },
        { headers: requestHeaders }
      );
      await login('valid-username', 'valid-password');
      expect($wnd.Vaadin.TypeScript.csrfToken).to.equal('some-new-token');
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
    beforeEach(() => {
      $wnd.Vaadin.TypeScript = {};
      $wnd.Vaadin.TypeScript.csrfToken = vaadinCsrfToken;
    });
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
      expect($wnd.Vaadin.TypeScript.csrfToken).to.equal(vaadinCsrfToken);
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
      expect($wnd.Vaadin.TypeScript.csrfToken).to.be.undefined;
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
      expect($wnd.Vaadin.TypeScript.csrfToken).to.equal(vaadinCsrfToken);
      verifySpringCsrfToken(springCsrfToken);
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
      expect($wnd.Vaadin.TypeScript.csrfToken).to.be.undefined;

      setupSpringCsrfMetaTags();
    });

    // when the page has been opend too long the session has expired
    it('should retry when expired spring csrf metas in the doc', async () => {
      const expiredSpringCsrfToken = `expired-${springCsrfToken}`;

      setupSpringCsrfMetaTags(expiredSpringCsrfToken);

      const headersWithExpiredSpringCsrfToken: Record<string, string> = {};
      headersWithExpiredSpringCsrfToken[springCsrfHeaderName] = expiredSpringCsrfToken;

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
      expect($wnd.Vaadin.TypeScript.csrfToken).to.equal(vaadinCsrfToken);
      verifySpringCsrfToken(springCsrfToken);
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
          token: 'csrf-token',
        };
      });

      const middleware = new InvalidSessionMiddleware(invalidSessionCallback as unknown as OnInvalidSessionCallback);

      const client = new ConnectClient({ middlewares: [middleware] });

      await client.call('FooEndpoint', 'fooMethod');

      expect(invalidSessionCallback.calledOnce).to.be.true;

      expect(fetchMock.lastOptions()?.headers).to.deep.include({
        'x-csrf-token': 'csrf-token',
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
