/* eslint-disable @typescript-eslint/ban-types,import/unambiguous */
declare module 'a-atmosphere-javascript' {
  export interface AtmosphereSubscribe {
    (request: AtmosphereRequest, callback?: Function): AtmosphereRequest;
    (url: URL, callback: Function | undefined, request: AtmosphereRequest): AtmosphereRequest;
  }

  export interface Atmosphere {
    /**
     * The atmosphere API is a little bit special here: the first parameter can either be
     * a URL string or a Request object. If it is a URL string, then the additional parameters are expected.
     */
    subscribe?: AtmosphereSubscribe | undefined;
    unsubscribe?: (() => void) | undefined;

    AtmosphereRequest?: AtmosphereRequest | undefined;
  }

  export interface AtmosphereRequest {
    timeout?: number | undefined;
    method?: string | undefined;
    headers?: any;
    contentType?: string | undefined;
    callback?: Function | undefined;
    url?: string | undefined;
    data?: string | undefined;
    suspend?: boolean | undefined;
    maxRequest?: number | undefined;
    reconnect?: boolean | undefined;
    maxStreamingLength?: number | undefined;
    lastIndex?: number | undefined;
    logLevel?: string | undefined;
    requestCount?: number | undefined;
    fallbackMethod?: string | undefined;
    fallbackTransport?: string | undefined;
    transport?: string | undefined;
    webSocketImpl?: any;
    webSocketBinaryType?: any;
    dispatchUrl?: string | undefined;
    webSocketPathDelimiter?: string | undefined;
    enableXDR?: boolean | undefined;
    rewriteURL?: boolean | undefined;
    attachHeadersAsQueryString?: boolean | undefined;
    executeCallbackBeforeReconnect?: boolean | undefined;
    readyState?: number | undefined;
    lastTimestamp?: number | undefined;
    withCredentials?: boolean | undefined;
    trackMessageLength?: boolean | undefined;
    messageDelimiter?: string | undefined;
    connectTimeout?: number | undefined;
    reconnectInterval?: number | undefined;
    dropHeaders?: boolean | undefined;
    uuid?: string | undefined;
    async?: boolean | undefined;
    shared?: boolean | undefined;
    readResponsesHeaders?: boolean | undefined;
    maxReconnectOnClose?: number | undefined;
    enableProtocol?: boolean | undefined;
    pollingInterval?: number | undefined;
    webSocketUrl?: string | undefined;
    disableDisconnect?: boolean | undefined;

    onError?: ((response?: AtmosphereResponse) => void) | undefined;
    onClose?: ((response?: AtmosphereResponse) => void) | undefined;
    onOpen?: ((response?: AtmosphereResponse) => void) | undefined;
    onMessage?: ((response: AtmosphereResponse) => void) | undefined;
    onReopen?: ((request?: AtmosphereRequest, response?: AtmosphereResponse) => void) | undefined;
    onReconnect?: ((request?: AtmosphereRequest, response?: AtmosphereResponse) => void) | undefined;
    onMessagePublished?: ((response?: AtmosphereResponse) => void) | undefined;
    onTransportFailure?: ((reason?: string, response?: AtmosphereResponse) => void) | undefined;
    onLocalMessage?: ((request?: AtmosphereRequest) => void) | undefined;
    onFailureToReconnect?: ((request?: AtmosphereRequest, response?: AtmosphereResponse) => void) | undefined;
    onClientTimeout?: ((request?: AtmosphereRequest) => void) | undefined;

    subscribe?: ((options: AtmosphereRequest) => void) | undefined;
    execute?: (() => void) | undefined;
    close?: (() => void) | undefined;
    disconnect?: (() => void) | undefined;
    getUrl?: (() => string) | undefined;
    push?: ((message: string, dispatchUrl?: string) => void) | undefined;
    getUUID?: (() => void) | undefined;
    pushLocal?: ((message: string) => void) | undefined;
  }

  // needed to fit JavaScript "new atmosphere.AtmosphereRequest()"
  // and compile with --noImplicitAny
  export const AtmosphereRequest: {
    prototype: AtmosphereRequest;
    new (): AtmosphereRequest;
  };

  export interface AtmosphereResponse {
    status?: number | undefined;
    reasonPhrase?: string | undefined;
    responseBody?: string | undefined;
    messages?: string[] | undefined;
    headers?: string[] | undefined;
    state?: string | undefined;
    transport?: string | undefined;
    error?: string | undefined;
    request?: AtmosphereRequest | undefined;
    partialMessage?: string | undefined;
    errorHandled?: boolean | undefined;
    closedByClientTimeout?: boolean | undefined;
  }

  export const atmosphere: Atmosphere;
}
