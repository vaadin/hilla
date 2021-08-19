import type { MiddlewareClass, MiddlewareContext, MiddlewareNext } from './Connect';

const $wnd = window as any;

function updateVaadinCsrfToken(token: string | undefined) {
  $wnd.Vaadin.TypeScript = $wnd.Vaadin.TypeScript || {};
  $wnd.Vaadin.TypeScript.csrfToken = token;
}

function getSpringCsrfInfoFromDocument(doc: Document): Record<string, string> {
  const csrf = doc.head.querySelector('meta[name="_csrf"]');
  const csrfHeader = doc.head.querySelector('meta[name="_csrf_header"]');
  const headers: Record<string, string> = {};
  if (csrf !== null && csrfHeader !== null) {
    headers._csrf = (csrf as HTMLMetaElement).content;
    headers._csrf_header = (csrfHeader as HTMLMetaElement).content;
  }
  return headers;
}

function getSpringCsrfTokenHeadersFromDocument(doc: Document): Record<string, string> {
  const csrfInfo = getSpringCsrfInfoFromDocument(doc);
  const headers: Record<string, string> = {};
  if (csrfInfo._csrf && csrfInfo._csrf_header) {
    headers[csrfInfo._csrf_header] = csrfInfo._csrf;
  }
  return headers;
}

function getSpringCsrfTokenFromResponseBody(body: string): Record<string, string> {
  const doc = new DOMParser().parseFromString(body, 'text/html');
  return getSpringCsrfInfoFromDocument(doc);
}

function clearSpringCsrfMetaTags() {
  Array.from(document.head.querySelectorAll('meta[name="_csrf"], meta[name="_csrf_header"]')).forEach((el) =>
    el.remove()
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
  const match = body.match(/window\.Vaadin = \{TypeScript: \{"csrfToken":"([0-9a-zA-Z\\-]{36})"}};/i);
  return match ? match[1] : undefined;
};

async function updateCsrfTokensBasedOnResponse(response: Response): Promise<string | undefined> {
  const responseText = await response.text();
  const token = getVaadinCsrfTokenFromResponseBody(responseText);
  updateVaadinCsrfToken(token);
  const springCsrfTokenInfo = getSpringCsrfTokenFromResponseBody(responseText);
  updateSpringCsrfMetaTags(springCsrfTokenInfo);

  return token;
}

async function doLogout(logoutUrl: string, headers: Record<string, string>) {
  const response = await fetch(logoutUrl, { method: 'POST', headers });
  if (!response.ok) {
    throw new Error(`failed to logout with response ${response.status}`);
  }

  await updateCsrfTokensBasedOnResponse(response);
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
}

/**
 * A helper method for Spring Security based form login.
 * @param username
 * @param password
 * @param options defines additional options, e.g, the loginProcessingUrl etc.
 */
export async function login(username: string, password: string, options?: LoginOptions): Promise<LoginResult> {
  try {
    const data = new FormData();
    data.append('username', username);
    data.append('password', password);

    const loginProcessingUrl = options && options.loginProcessingUrl ? options.loginProcessingUrl : 'login';
    const headers = getSpringCsrfTokenHeadersFromDocument(document);
    headers.source = 'typescript';
    const response = await fetch(loginProcessingUrl, {
      method: 'POST',
      body: data,
      headers,
    });

    // This code assumes that a VaadinSavedRequestAwareAuthenticationSuccessHandler is used on the server side,
    // setting these header values based on the "source=typescript" header set above

    const result = response.headers.get('Result');
    const savedUrl = response.headers.get('Saved-url') || undefined;
    const defaultUrl = response.headers.get('Default-url') || undefined;
    const loginSuccessful = response.ok && result === 'success';

    if (loginSuccessful) {
      const vaadinCsrfToken = response.headers.get('Vaadin-CSRF') || undefined;
      updateVaadinCsrfToken(vaadinCsrfToken);

      const springCsrfHeader = response.headers.get('Spring-CSRF-header') || undefined;
      const springCsrfToken = response.headers.get('Spring-CSRF-token') || undefined;
      if (springCsrfHeader && springCsrfToken) {
        const springCsrfTokenInfo: Record<string, string> = {};
        springCsrfTokenInfo._csrf = springCsrfToken;
        springCsrfTokenInfo._csrf_header = springCsrfHeader;
        updateSpringCsrfMetaTags(springCsrfTokenInfo);
      }

      return {
        error: false,
        token: vaadinCsrfToken,
        redirectUrl: savedUrl,
        defaultUrl,
      };
    }
    return {
      error: true,
      errorTitle: 'Incorrect username or password.',
      errorMessage: 'Check that you have entered the correct username and password and try again.',
    };
  } catch (e) {
    return {
      error: true,
      errorTitle: e.name,
      errorMessage: e.message,
    };
  }
}

/**
 * A helper method for Spring Security based form logout
 * @param options defines additional options, e.g, the logoutUrl.
 */
export async function logout(options?: LogoutOptions) {
  // this assumes the default Spring Security logout configuration (handler URL)
  const logoutUrl = options && options.logoutUrl ? options.logoutUrl : 'logout';
  try {
    const headers = getSpringCsrfTokenHeadersFromDocument(document);
    await doLogout(logoutUrl, headers);
  } catch {
    try {
      const response = await fetch('?nocache');
      const responseText = await response.text();
      const doc = new DOMParser().parseFromString(responseText, 'text/html');
      const headers = getSpringCsrfTokenHeadersFromDocument(doc);
      await doLogout(logoutUrl, headers);
    } catch (error) {
      // clear the token if the call fails
      delete $wnd.Vaadin?.TypeScript?.csrfToken;
      clearSpringCsrfMetaTags();
      throw error;
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
        clonedContext.request.headers.set('X-CSRF-Token', loginResult.token);
        return next(clonedContext);
      }
    }
    return response;
  }
}
