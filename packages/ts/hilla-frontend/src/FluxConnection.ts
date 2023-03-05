import { atmosphere, type AtmosphereRequest, type AtmosphereResponse } from 'a-atmosphere-javascript';
import type { ReactiveElement } from 'lit';
import type { Subscription } from './Connect.js';
import { getCsrfTokenHeadersForEndpointRequest } from './CsrfUtils.js';
import {
  isClientMessage,
  type ServerCloseMessage,
  type ServerConnectMessage,
  type ServerMessage,
} from './FluxMessages.js';

export enum State {
  ACTIVE = 'active',
  INACTIVE = 'inactive',
}

type ActiveEvent = CustomEvent<{ active: boolean }>;
interface EventMap {
  'state-changed': ActiveEvent;
}

type ListenerType<T extends keyof EventMap> =
  | ((this: FluxConnection, ev: EventMap[T]) => any)
  | {
      handleEvent(ev: EventMap[T]): void;
    }
  | null;

/**
 * A representation of the underlying persistent network connection used for subscribing to Flux type endpoint methods.
 */
export class FluxConnection extends EventTarget {
  state: State = State.INACTIVE;
  readonly #endpointInfos = new Map<string, string>();
  #nextId = 0;
  readonly #onCompleteCallbacks = new Map<string, () => void>();
  readonly #onErrorCallbacks = new Map<string, () => void>();
  readonly #onNextCallbacks = new Map<string, (value: any) => void>();
  #pendingMessages: ServerMessage[] = [];
  #socket: AtmosphereRequest;

  constructor() {
    super();

    const extraHeaders = getCsrfTokenHeadersForEndpointRequest(document);
    const callback = {
      onClose: () => {
        // https://socket.io/docs/v4/client-api/#event-disconnect
        if (this.state === State.ACTIVE) {
          this.state = State.INACTIVE;
          this.dispatchEvent(new CustomEvent('state-changed', { detail: { active: false } }));
        }
      },
      onError: (response?: AtmosphereResponse) => {
        // eslint-disable-next-line no-console
        console.error('error in push communication', response);
      },
      onMessage: (response: AtmosphereResponse) => {
        if (response.responseBody) {
          this.#handleMessage(JSON.parse(response.responseBody));
        }
      },
      onOpen: () => {
        if (this.state === State.INACTIVE) {
          this.state = State.ACTIVE;
          this.dispatchEvent(new CustomEvent('state-changed', { detail: { active: true } }));
          this.#sendPendingMessages();
        }
      },
      onReopen: () => {
        if (this.state === State.INACTIVE) {
          this.state = State.ACTIVE;
          this.dispatchEvent(new CustomEvent('state-changed', { detail: { active: true } }));
          this.#sendPendingMessages();
        }
      },
    };

    this.#socket = atmosphere.subscribe!({
      contentType: 'application/json; charset=UTF-8',
      enableProtocol: true,
      fallbackTransport: 'long-polling',
      headers: extraHeaders,
      maxReconnectOnClose: 10000000,
      reconnectInterval: 5000,
      timeout: -1,
      trackMessageLength: true,
      transport: 'websocket',
      url: '/HILLA/push',
      ...callback,
    });
  }

  /**
   * Subscribes to the flux returned by the given endpoint name + method name using the given parameters.
   *
   * @param endpointName the endpoint to connect to
   * @param methodName the method in the endpoint to connect to
   * @param parameters the parameters to use
   * @returns a subscription
   */
  subscribe(endpointName: string, methodName: string, parameters?: any[]): Subscription<any> {
    const id: string = this.#nextId.toString();
    this.#nextId += 1;
    const params = parameters ?? [];

    const msg: ServerConnectMessage = { '@type': 'subscribe', endpointName, id, methodName, params };
    const endpointInfo = `${endpointName}.${methodName}(${JSON.stringify(params)})`;
    this.#send(msg);
    this.#endpointInfos.set(id, endpointInfo);
    const hillaSubscription: Subscription<any> = {
      cancel: () => {
        if (!this.#endpointInfos.has(id)) {
          // Subscription already closed or canceled
          return;
        }

        const closeMessage: ServerCloseMessage = { '@type': 'unsubscribe', id };
        this.#send(closeMessage);
        this.#removeSubscription(id);
      },
      context: (context: ReactiveElement): Subscription<any> => {
        context.addController({
          hostDisconnected: () => {
            hillaSubscription.cancel();
          },
        });
        return hillaSubscription;
      },
      onComplete: (callback: () => void): Subscription<any> => {
        this.#onCompleteCallbacks.set(id, callback);
        return hillaSubscription;
      },
      onError: (callback: () => void): Subscription<any> => {
        this.#onErrorCallbacks.set(id, callback);
        return hillaSubscription;
      },
      onNext: (callback: (value: any) => void): Subscription<any> => {
        this.#onNextCallbacks.set(id, callback);
        return hillaSubscription;
      },
    };

    return hillaSubscription;
  }

  #handleMessage(message: object) {
    if (isClientMessage(message)) {
      const { id } = message;
      const endpointInfo = this.#endpointInfos.get(id);

      if (message['@type'] === 'update') {
        const callback = this.#onNextCallbacks.get(id);
        if (callback) {
          callback(message.item);
        }
      } else if (message['@type'] === 'complete') {
        const callback = this.#onCompleteCallbacks.get(id);
        if (callback) {
          callback();
        }

        this.#removeSubscription(id);
        // eslint-disable-next-line @typescript-eslint/no-unnecessary-condition
      } else if (message['@type'] === 'error') {
        const callback = this.#onErrorCallbacks.get(id);
        if (callback) {
          callback();
        }
        this.#removeSubscription(id);
        if (!callback) {
          throw new Error(`Error in ${endpointInfo ?? 'unknown'}: ${message.message}`);
        }
      }
    }

    throw new Error(`Unknown message from server:\n\n${JSON.stringify(message, null, 2)}`);
  }

  #removeSubscription(id: string) {
    this.#onNextCallbacks.delete(id);
    this.#onCompleteCallbacks.delete(id);
    this.#onErrorCallbacks.delete(id);
    this.#endpointInfos.delete(id);
  }

  #send(message: ServerMessage) {
    if (this.state === State.INACTIVE) {
      this.#pendingMessages.push(message);
    } else {
      this.#socket.push?.(JSON.stringify(message));
    }
  }

  #sendPendingMessages() {
    this.#pendingMessages.forEach((msg) => this.#send(msg));
    this.#pendingMessages = [];
  }
}

export interface FluxConnection {
  addEventListener<T extends keyof EventMap>(type: T, listener: ListenerType<T>): void;
  removeEventListener<T extends keyof EventMap>(type: T, listener: ListenerType<T>): void;
}
