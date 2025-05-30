import type { MiddlewareClass, MiddlewareContext, MiddlewareNext } from './Connect.js';
import csrfInfoSource, {
  VAADIN_CSRF_HEADER,
  clearCsrfInfoMeta,
  type CsrfInfo,
  CsrfInfoType,
  extractCsrfInfoFromMeta,
  updateCsrfInfoMeta,
} from './CsrfInfoSource.js';
import { VAADIN_BROWSER_ENVIRONMENT } from './utils.js';

// eslint-disable-next-line @typescript-eslint/no-unnecessary-condition
const cookieManagerPromise = VAADIN_BROWSER_ENVIRONMENT ? import('./CookieManager.js') : undefined;

function createHeaders(headerEntries: ReadonlyArray<readonly [name: string, value: string]>): Headers {
  const headers = new Headers();
  for (const [name, value] of headerEntries) {
    headers.append(name, value);
  }
  return headers;
}

const JWT_COOKIE_NAME = 'jwt.headerAndPayload';

async function getCsrfInfoFromResponseBody(body: string): Promise<CsrfInfo> {
  const doc = new DOMParser().parseFromString(body, 'text/html');
  return extractCsrfInfoFromMeta(doc);
}

async function updateCsrfTokensBasedOnResponse(response: Response): Promise<void> {
  const responseText = await response.text();
  const csrfInfo = await getCsrfInfoFromResponseBody(responseText);
  updateCsrfInfoMeta(csrfInfo, document);
}

async function doFetchLogout(
  logoutUrl: URL | string,
  headerEntries: ReadonlyArray<readonly [name: string, value: string]>,
) {
  const headers = createHeaders(headerEntries);
  const response = await fetch(logoutUrl, { headers, method: 'POST' });
  if (!response.ok) {
    throw new Error(`failed to logout with response ${response.status}`);
  }

  await updateCsrfTokensBasedOnResponse(response);
  csrfInfoSource.reset();

  return response;
}

async function doFormLogout(
  url: URL | string,
  formDataEntries: ReadonlyArray<readonly [name: string, value: string]>,
): Promise<void> {
  const logoutUrl = typeof url === 'string' ? url : url.toString();

  // Create form to send POST request
  const form = document.createElement('form');
  form.setAttribute('method', 'POST');
  form.setAttribute('action', logoutUrl);
  form.style.display = 'none';

  // Add data to form as hidden input fields
  for (const [name, value] of formDataEntries) {
    const input = document.createElement('input');
    input.setAttribute('type', 'hidden');
    input.setAttribute('name', name);
    input.setAttribute('value', value);

    form.appendChild(input);
  }

  // Append form to page and submit it to perform logout and redirect
  document.body.appendChild(form);

  // No code should run after a form submission, as it will navigate away.
  // The promise will reject after a long timeout to avoid executing code after
  // (old user code has a `reload` call that could happen before the form submission).
  return new Promise((_, reject) => {
    setTimeout(() => {
      reject(new Error('Form submission did not navigate away after 10 seconds.'));
    }, 10000);
    form.submit();
  });
}

async function doLogout(doc: Document, options?: LogoutOptions): Promise<Response> {
  // performing fetch logout only makes sense if at least one of the 'navigate'
  // or 'onSuccess' is defined, otherwise we can just do a form logout:
  const shouldSubmitFormLogout = !options?.navigate && !options?.onSuccess;
  // this assumes the default Spring Security logout configuration (handler URL)
  const logoutUrl = options?.logoutUrl ?? 'logout';
  const csrfInfo = doc === document ? await csrfInfoSource.get() : await extractCsrfInfoFromMeta(doc);
  if (shouldSubmitFormLogout) {
    const formDataEntries = csrfInfo.type === CsrfInfoType.SPRING ? csrfInfo.formDataEntries : [];
    await doFormLogout(logoutUrl, formDataEntries);
    // This should never be reached, as form submission will navigate away
    return new Response(null, {
      status: 500,
      statusText: 'Form submission did not navigate away.',
    } as ResponseInit);
  }
  const headerEntries = csrfInfo.type === CsrfInfoType.SPRING ? csrfInfo.headerEntries : [];
  return await doFetchLogout(logoutUrl, headerEntries);
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
    const csrfInfo = await csrfInfoSource.get();
    const headers = createHeaders(csrfInfo.headerEntries);
    headers.append('source', 'typescript');
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
      const springCsrfHeader = response.headers.get('Spring-CSRF-header') ?? undefined;
      const springCsrfToken = response.headers.get('Spring-CSRF-token') ?? undefined;
      if (springCsrfHeader && springCsrfToken) {
        updateCsrfInfoMeta(
          {
            headerEntries: [[springCsrfHeader, springCsrfToken]],
            formDataEntries: [],
            type: CsrfInfoType.SPRING,
            timestamp: Date.now(),
          },
          document,
        );
        csrfInfoSource.reset();
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
  let response: Response | undefined;
  try {
    response = await doLogout(document, options);
  } catch {
    try {
      const noCacheResponse = await fetch('?nocache');
      const responseText = await noCacheResponse.text();
      const doc = new DOMParser().parseFromString(responseText, 'text/html');
      response = await doLogout(doc, options);
    } catch (error) {
      // clear the token if the call fails
      clearCsrfInfoMeta(document);
      csrfInfoSource.reset();
      throw error;
    }
  } finally {
    if (cookieManagerPromise) {
      const cookieManager = (await cookieManagerPromise).default;
      cookieManager.remove(JWT_COOKIE_NAME);
    }
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
