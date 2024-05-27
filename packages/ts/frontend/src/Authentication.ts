import type { MiddlewareClass, MiddlewareContext, MiddlewareNext } from './Connect.js';
import CookieManager from './CookieManager.js';
import { getSpringCsrfInfo, getSpringCsrfTokenHeadersForAuthRequest, VAADIN_CSRF_HEADER } from './CsrfUtils.js';

const JWT_COOKIE_NAME = 'jwt.headerAndPayload';

function getSpringCsrfTokenFromResponseBody(body: string): Record<string, string> {
  const doc = new DOMParser().parseFromString(body, 'text/html');
  return getSpringCsrfInfo(doc);
}

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

const getVaadinCsrfTokenFromResponseBody = (body: string): string | undefined => {
  const match = /window\.Vaadin = \{TypeScript: \{"csrfToken":"([0-9a-zA-Z\\-]{36})"\}\};/iu.exec(body);
  return match ? match[1] : undefined;
};

async function updateCsrfTokensBasedOnResponse(response: Response): Promise<string | undefined> {
  const responseText = await response.text();
  const token = getVaadinCsrfTokenFromResponseBody(responseText);
  const springCsrfTokenInfo = getSpringCsrfTokenFromResponseBody(responseText);
  updateSpringCsrfMetaTags(springCsrfTokenInfo);

  return token;
}

async function doLogout(logoutUrl: URL | string, headers: Record<string, string>) {
  const response = await fetch(logoutUrl, { headers, method: 'POST' });
  if (!response.ok) {
    throw new Error(`failed to logout with response ${response.status}`);
  }

  await updateCsrfTokensBasedOnResponse(response);

  return response;
}

export interface LoginResult {
  error: boolean;
  token?: string;
  errorTitle?: string;
  errorMessage?: string;
  redirectUrl?: string;
  defaultUrl?: string;
}

export type SuccessCallback = () => Promise<void> | void;

export type NavigateFunction = (path: string) => void;

export interface LoginOptions {
  /**
   * The URL for login request, defaults to `/login`.
   */
  loginProcessingUrl?: URL | string;

  /**
   * The success callback.
   */
  onSuccess?: SuccessCallback;

  /**
   * The navigation callback, called after successful login. The default
   * reloads the page.
   */
  navigate?: NavigateFunction;
}

export interface LogoutOptions {
  /**
   * The URL for logout request, defaults to `/logout`.
   */
  logoutUrl?: URL | string;

  /**
   * The success callback.
   */
  onSuccess?: SuccessCallback;

  /**
   * The navigation callback, called after successful logout. The default
   * reloads the page.
   */
  navigate?: NavigateFunction;
}

function normalizePath(url: string): string {
  // URL with context path
  const effectiveBaseURL = new URL('.', document.baseURI);
  const effectiveBaseURI = effectiveBaseURL.toString();

  let normalized = url;

  // Strip context path prefix
  if (normalized.startsWith(effectiveBaseURL.pathname)) {
    return `/${normalized.slice(effectiveBaseURL.pathname.length)}`;
  }

  // Strip base URI
  normalized = normalized.startsWith(effectiveBaseURI) ? `/${normalized.slice(effectiveBaseURI.length)}` : normalized;

  return normalized;
}

/**
 * Navigates to the provided path using page reload.
 *
 * @param to - navigation target path
 */
function navigateWithPageReload(to: string) {
  // Consider absolute path to be within application context
  const url = to.startsWith('/') ? new URL(`.${to}`, document.baseURI) : to;
  window.location.replace(url);
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

      if (options?.onSuccess) {
        await options.onSuccess();
      }

      const url = savedUrl ?? defaultUrl ?? document.baseURI;
      const toPath = normalizePath(url);
      const navigate = options?.navigate ?? navigateWithPageReload;
      navigate(toPath);

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
export async function logout(options?: LogoutOptions): Promise<void> {
  // this assumes the default Spring Security logout configuration (handler URL)
  const logoutUrl = options?.logoutUrl ?? 'logout';
  let response: Response | undefined;
  try {
    const headers = getSpringCsrfTokenHeadersForAuthRequest(document);
    response = await doLogout(logoutUrl, headers);
  } catch {
    try {
      const noCacheResponse = await fetch('?nocache');
      const responseText = await noCacheResponse.text();
      const doc = new DOMParser().parseFromString(responseText, 'text/html');
      const headers = getSpringCsrfTokenHeadersForAuthRequest(doc);
      response = await doLogout(logoutUrl, headers);
    } catch (error) {
      // clear the token if the call fails
      clearSpringCsrfMetaTags();
      throw error;
    }
  } finally {
    CookieManager.remove(JWT_COOKIE_NAME);
    if (response && response.ok && response.redirected) {
      if (options?.onSuccess) {
        await options.onSuccess();
      }
      const toPath = normalizePath(response.url);
      const navigate = options?.navigate ?? navigateWithPageReload;
      navigate(toPath);
    }
  }
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
