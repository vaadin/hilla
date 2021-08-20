/* eslint-disable max-classes-per-file */
import { ConnectionIndicator, ConnectionState } from '@vaadin/client-commons';

const $wnd = window as any;
$wnd.Vaadin = $wnd.Vaadin || {};
$wnd.Vaadin.registrations = $wnd.Vaadin.registrations || [];
$wnd.Vaadin.registrations.push({
  is: 'endpoint',
});

/**
 * An exception that gets thrown when the Vaadin backend responds
 * with not ok status.
 */
export class EndpointError extends Error {
  /**
   * The optional name of the exception that was thrown on a backend
   */
  type?: string;

  /**
   * The optional detail object, containing additional information sent
   * from a backend
   */
  detail?: any;

  /**
   * @param message the `message` property value
   * @param type the `type` property value
   * @param detail the `detail` property value
   */
  constructor(message: string, type?: string, detail?: any) {
    super(message);
    this.type = type;
    this.detail = detail;
  }
}

/**
 * An exception that gets thrown if Vaadin endpoint responds
 * with non-ok status and provides additional info
 * on the validation errors occurred.
 */
export class EndpointValidationError extends EndpointError {
  /**
   * An original validation error message.
   */
  validationErrorMessage: string;

  /**
   * An array of the validation errors.
   */
  validationErrorData: ValidationErrorData[];

  /**
   * @param message the `message` property value
   * @param validationErrorData the `validationErrorData` property value
   * @param type the `type` property value
   */
  constructor(message: string, validationErrorData: ValidationErrorData[], type?: string) {
    super(message, type, validationErrorData);
    this.validationErrorMessage = message;
    this.detail = null;
    this.validationErrorData = validationErrorData;
  }
}

/**
 * An exception that gets thrown for unexpected HTTP response.
 */
export class EndpointResponseError extends Error {
  /**
   * The optional response object, containing the HTTP response error
   */
  response: Response;

  /**
   * @param message the `message` property value
   * @param response the `response` property value
   */
  constructor(message: string, response: Response) {
    super(message);
    this.response = response;
  }
}

interface ConnectExceptionData {
  message: string;
  type: string;
  detail?: any;
  validationErrorData?: ValidationErrorData[];
}

const throwConnectException = (errorJson: ConnectExceptionData) => {
  if (errorJson.validationErrorData) {
    throw new EndpointValidationError(errorJson.message, errorJson.validationErrorData, errorJson.type);
  } else {
    throw new EndpointError(errorJson.message, errorJson.type, errorJson.detail);
  }
};

/**
 * Throws a TypeError if the response is not 200 OK.
 * @param response The response to assert.
 * @ignore
 */
const assertResponseIsOk = async (response: Response): Promise<void> => {
  if (!response.ok) {
    const errorText = await response.text();
    let errorJson: ConnectExceptionData | null;
    try {
      errorJson = JSON.parse(errorText);
    } catch (ignored) {
      // not a json
      errorJson = null;
    }

    if (errorJson !== null) {
      throwConnectException(errorJson);
    } else if (errorText !== null && errorText.length > 0) {
      throw new EndpointResponseError(errorText, response);
    } else {
      throw new EndpointError(`expected "200 OK" response, but got ${response.status} ${response.statusText}`);
    }
  }
};

/**
 * An object, containing all data for the particular validation error.
 */
export class ValidationErrorData {
  /**
   * The validation error message.
   */
  message: string;

  /**
   * The parameter name that caused the validation error.
   */
  parameterName?: string;

  /**
   * @param message the `message` property value
   * @param parameterName the `parameterName` property value
   */
  constructor(message: string, parameterName?: string) {
    this.message = message;
    this.parameterName = parameterName;
  }
}

/**
 * The `ConnectClient` constructor options.
 */
export interface ConnectClientOptions {
  /**
   * The `prefix` property value.
   */
  prefix?: string;

  /**
   * The `middlewares` property value.
   */
  middlewares?: Middleware[];
}

export interface EndpointCallMetaInfo {
  /**
   * The endpoint name.
   */
  endpoint: string;

  /**
   * The method name to call on in the endpoint class.
   */
  method: string;

  /**
   * Optional object with method call arguments.
   */
  params?: any;
}

/**
 * An object with the call arguments and the related Request instance.
 * See also {@link ConnectClient.call | the call() method in ConnectClient}.
 */
export interface MiddlewareContext extends EndpointCallMetaInfo {
  /**
   * The Fetch API Request object reflecting the other properties.
   */
  request: Request;
}

/**
 * An async middleware callback that invokes the next middleware in the chain
 * or makes the actual request.
 * @param context The information about the call and request
 */
export type MiddlewareNext = (context: MiddlewareContext) => Promise<Response> | Response;

/**
 * An interface that allows defining a middleware as a class.
 */
export interface MiddlewareClass {
  /**
   * @param context The information about the call and request
   * @param next Invokes the next in the call chain
   */
  invoke(context: MiddlewareContext, next: MiddlewareNext): Promise<Response> | Response;
}

/**
 * An async callback function that can intercept the request and response
 * of a call.
 */
type MiddlewareFunction = (context: MiddlewareContext, next: MiddlewareNext) => Promise<Response> | Response;

/**
 * An async callback that can intercept the request and response
 * of a call, could be either a function or a class.
 */
export type Middleware = MiddlewareClass | MiddlewareFunction;

/**
 * Vaadin Connect client class is a low-level network calling utility. It stores
 * a prefix and facilitates remote calls to endpoint class methods
 * on the Vaadin backend.
 *
 * Example usage:
 *
 * ```js
 * const client = new ConnectClient();
 * const responseData = await client.call('MyEndpoint', 'myMethod');
 * ```
 *
 * ### Prefix
 *
 * The client supports an `prefix` constructor option:
 * ```js
 * const client = new ConnectClient({prefix: '/my-connect-prefix'});
 * ```
 *
 * The default prefix is '/connect'.
 *
 */
export class ConnectClient {
  /**
   * The Vaadin endpoint prefix
   */
  prefix = '/connect';

  /**
   * The array of middlewares that are invoked during a call.
   */
  middlewares: Middleware[] = [];

  /**
   * @param options Constructor options.
   */
  constructor(options: ConnectClientOptions = {}) {
    if (options.prefix) {
      this.prefix = options.prefix;
    }

    if (options.middlewares) {
      this.middlewares = options.middlewares;
    }

    // add connection indicator to DOM
    ConnectionIndicator.create();

    // Listen to browser online/offline events and update the loading indicator accordingly.
    // Note: if Flow.ts is loaded, it instead handles the state transitions.
    $wnd.addEventListener('online', () => {
      if (!this.isFlowLoaded()) {
        $wnd.Vaadin.connectionState.state = ConnectionState.CONNECTED;
      }
    });
    $wnd.addEventListener('offline', () => {
      if (!this.isFlowLoaded()) {
        $wnd.Vaadin.connectionState.state = ConnectionState.CONNECTION_LOST;
      }
    });
  }

  /**
   * Makes a JSON HTTP request to the `${prefix}/${endpoint}/${method}` URL,
   * optionally supplying the provided params as a JSON request body,
   * and asynchronously returns the parsed JSON response data.
   *
   * @param endpoint Endpoint name.
   * @param method Method name to call in the endpoint class.
   * @param params Optional object to be send in JSON request body.
   * @param options Optional client options for this call.
   * @returns {} Decoded JSON response data.
   */
  async call(endpoint: string, method: string, params?: any): Promise<any> {
    if (arguments.length < 2) {
      throw new TypeError(`2 arguments required, but got only ${arguments.length}`);
    }

    const headers: Record<string, string> = {
      Accept: 'application/json',
      'Content-Type': 'application/json',
      'X-CSRF-Token': this.getCsrfTokenFromCookie(),
    };

    // helper to keep the undefined value in object after JSON.stringify
    const nullForUndefined = (obj: Record<string, unknown>): Record<string, unknown> =>
      Object.keys(obj).reduce((_obj, property) => {
        if (_obj[property] === undefined) {
          _obj[property] = null;
        }

        return _obj;
      }, obj);

    const request = new Request(`${this.prefix}/${endpoint}/${method}`, {
      method: 'POST',
      headers,
      body: params !== undefined ? JSON.stringify(nullForUndefined(params)) : undefined,
    });

    // The middleware `context`, includes the call arguments and the request
    // constructed from them
    const initialContext: MiddlewareContext = {
      endpoint,
      method,
      params,
      request,
    };

    // The internal middleware to assert and parse the response. The internal
    // response handling should come last after the other middlewares are done
    // with processing the response. That is why this middleware is first
    // in the final middlewares array.
    const responseHandlerMiddleware: Middleware = async (
      context: MiddlewareContext,
      next: MiddlewareNext
    ): Promise<Response> => {
      const response = await next(context);
      await assertResponseIsOk(response);
      const text = await response.text();
      return JSON.parse(text, (_, value: any) => (value === null ? undefined : value));
    };

    // The actual fetch call itself is expressed as a middleware
    // chain item for our convenience. Always having an ending of the chain
    // this way makes the folding down below more concise.
    const fetchNext: MiddlewareNext = async (context: MiddlewareContext): Promise<Response> => {
      $wnd.Vaadin.connectionState.loadingStarted();
      return fetch(context.request)
        .then((response) => {
          $wnd.Vaadin.connectionState.loadingFinished();
          return response;
        })
        .catch((error) => {
          $wnd.Vaadin.connectionState.loadingFailed();
          return Promise.reject(error);
        });
    };

    // Assemble the final middlewares array from internal
    // and external middlewares
    const middlewares = [responseHandlerMiddleware as Middleware].concat(this.middlewares);

    // Fold the final middlewares array into a single function
    const chain = middlewares.reduceRight(
      (next: MiddlewareNext, middleware: Middleware) => {
        // Compose and return the new chain step, that takes the context and
        // invokes the current middleware with the context and the further chain
        // as the next argument
        return ((context) => {
          if (typeof middleware === 'function') {
            return middleware(context, next);
          }
          return (middleware as MiddlewareClass).invoke(context, next);
        }) as MiddlewareNext;
      },
      // Initialize reduceRight the accumulator with `fetchNext`
      fetchNext
    );

    // Invoke all the folded async middlewares and return
    return chain(initialContext);
  }

  private isFlowLoaded(): boolean {
    return $wnd.Vaadin.Flow?.clients?.TypeScript !== undefined;
  }

  private getCsrfTokenFromCookie(): string {
    const csrfCookieNamePrefix = 'csrfToken=';
    return (
      document.cookie
        .split(/;[ ]?/)
        .filter((cookie) => cookie.startsWith(csrfCookieNamePrefix))
        .map((cookie) => cookie.slice(csrfCookieNamePrefix.length))[0] || ''
    );
  }
}
