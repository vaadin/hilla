import type { MiddlewareClass, MiddlewareContext, MiddlewareNext } from './Connect.js';
import CookieManager from './CookieManager.js';
import { getSpringCsrfTokenHeadersForAuthRequest, VAADIN_CSRF_HEADER } from './CsrfUtils.js';

const JWT_COOKIE_NAME = 'jwt.headerAndPayload';

function clearSpringCsrfMetaTags() {
  Array.from(document.head.querySelectorAll('meta[name="_csrf"], meta[name="_csrf_header"]')).forEach((el) =>
    el.remove(),
  );
}

function updateSpringCsrfMetaTags(springCsrfInfo: Record<string, string>) {
  clearSpringCsrfMetaTags();
  const headerNameMeta: HTMLMetaElement = document.createElement('meta');
  headerNameMeta.name = '_csrf_header';
  headerNameMeta.content = springCsrfInfo._csrf_header;
  document.head.appendChild(headerNameMeta);
  const tokenMeta: HTMLMetaElement = document.createElement('meta');
  tokenMeta.name = '_csrf';
  tokenMeta.content = springCsrfInfo._csrf;
  document.head.appendChild(tokenMeta);
}

export interface LoginResult {
  error: boolean;
  token?: string;
  errorTitle?: string;
  errorMessage?: string;
  redirectUrl?: string;
  defaultUrl?: string;
}

export interface LoginOptions {
  loginProcessingUrl?: string;
}

export interface LogoutOptions {
  logoutUrl?: string;
  location?: Pick<Location, 'href'>;
}

/**
 * A helper method for Spring Security based form login.
 * @param username - username
 * @param password - password
 * @param options - defines additional options, e.g, the loginProcessingUrl etc.
 */
export async function login(username: string, password: string, options?: LoginOptions): Promise<LoginResult> {
  try {
    const data = new FormData();
    data.append('username', username);
    data.append('password', password);

    const loginProcessingUrl = options?.loginProcessingUrl ?? 'login';
    const headers = getSpringCsrfTokenHeadersForAuthRequest(document);
    headers.source = 'typescript';
    const response = await fetch(loginProcessingUrl, {
      body: data,
      headers,
      method: 'POST',
    });

    // This code assumes that a VaadinSavedRequestAwareAuthenticationSuccessHandler is used on the server side,
    // setting these header values based on the "source=typescript" header set above

    const result = response.headers.get('Result');
    const savedUrl = response.headers.get('Saved-url') ?? undefined;
    const defaultUrl = response.headers.get('Default-url') ?? undefined;
    const loginSuccessful = response.ok && result === 'success';

    if (loginSuccessful) {
      const vaadinCsrfToken = response.headers.get('Vaadin-CSRF') ?? undefined;

      const springCsrfHeader = response.headers.get('Spring-CSRF-header') ?? undefined;
      const springCsrfToken = response.headers.get('Spring-CSRF-token') ?? undefined;
      if (springCsrfHeader && springCsrfToken) {
        const springCsrfTokenInfo: Record<string, string> = {};
        springCsrfTokenInfo._csrf = springCsrfToken;
        // eslint-disable-next-line camelcase
        springCsrfTokenInfo._csrf_header = springCsrfHeader;
        updateSpringCsrfMetaTags(springCsrfTokenInfo);
      }

      return {
        defaultUrl,
        error: false,
        redirectUrl: savedUrl,
        token: vaadinCsrfToken,
      };
    }
    return {
      error: true,
      errorMessage: 'Check that you have entered the correct username and password and try again.',
      errorTitle: 'Incorrect username or password.',
    };
  } catch (e: unknown) {
    if (e instanceof Error) {
      return {
        error: true,
        errorMessage: e.message,
        errorTitle: e.name,
      };
    }

    throw e;
  }
}

/**
 * A helper method for Spring Security based form logout
 * @param options - defines additional options, e.g, the logoutUrl.
 */
export function logout({ logoutUrl = '/logout', location = window.location }: LogoutOptions = {}): void {
  CookieManager.remove(JWT_COOKIE_NAME);
  location.href = logoutUrl;
}

/**
 * It defines what to do when it detects a session is invalid. E.g.,
 * show a login view.
 * It takes an <code>EndpointCallContinue</code> parameter, which can be
 * used to continue the endpoint call.
 */
export type OnInvalidSessionCallback = () => Promise<LoginResult>;

/**
 * A helper class for handling invalid sessions during an endpoint call.
 * E.g., you can use this to show user a login page when the session has
 * expired.
 */
export class InvalidSessionMiddleware implements MiddlewareClass {
  private readonly onInvalidSessionCallback: OnInvalidSessionCallback;

  constructor(onInvalidSessionCallback: OnInvalidSessionCallback) {
    this.onInvalidSessionCallback = onInvalidSessionCallback;
  }

  async invoke(context: MiddlewareContext, next: MiddlewareNext): Promise<Response> {
    const clonedContext = { ...context };
    clonedContext.request = context.request.clone();
    const response = await next(context);
    if (response.status === 401) {
      const loginResult = await this.onInvalidSessionCallback();
      if (loginResult.token) {
        clonedContext.request.headers.set(VAADIN_CSRF_HEADER, loginResult.token);
        return next(clonedContext) as Promise<Response>;
      }
    }
    return response;
  }
}
