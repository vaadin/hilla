import type { ReactiveElement } from 'lit';
import { atmosphere } from 'a-atmosphere-javascript';
import type { Subscription } from './Connect.js';
import { getCsrfTokenHeadersForEndpointRequest } from './CsrfUtils.js';
import type { ClientMessage, ServerCloseMessage, ServerConnectMessage, ServerMessage } from './FluxMessages.js';

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
  private nextId = 0;
  private endpointInfos = new Map<string, string>();
  private onNextCallbacks = new Map<string, (value: any) => void>();
  private onCompleteCallbacks = new Map<string, () => void>();
  private onErrorCallbacks = new Map<string, () => void>();

  private socket: any;
  public state: State = State.INACTIVE;
  private pendingMessages: ServerMessage[] = [];

  constructor(connectPrefix: string) {
    super();
    this.connectWebsocket(connectPrefix.replace('/connect', '').replace(/^connect/, ''));
  }

  private connectWebsocket(prefix: string) {
    const extraHeaders = getCsrfTokenHeadersForEndpointRequest(document);
    const callback = {
      onMessage: (response: any) => {
        this.handleMessage(JSON.parse(response.responseBody));
      },
      onOpen: (_response: any) => {
        if (this.state === State.INACTIVE) {
          this.state = State.ACTIVE;
          this.dispatchEvent(new CustomEvent('state-changed', { detail: { active: true } }));
          this.sendPendingMessages();
        }
      },
      onReopen: (_response: any) => {
        if (this.state === State.INACTIVE) {
          this.state = State.ACTIVE;
          this.dispatchEvent(new CustomEvent('state-changed', { detail: { active: true } }));
          this.sendPendingMessages();
        }
      },
      onClose: (_response: any) => {
        // https://socket.io/docs/v4/client-api/#event-disconnect
        if (this.state === State.ACTIVE) {
          this.state = State.INACTIVE;
          this.dispatchEvent(new CustomEvent('state-changed', { detail: { active: false } }));
        }
      },
      onError: (response: any) => {
        // eslint-disable-next-line no-console
        console.error('error in push communication', response);
      },
    };
    this.socket = atmosphere.subscribe!({
      url: `${prefix}/HILLA/push`,
      transport: 'websocket',
      fallbackTransport: 'long-polling',
      contentType: 'application/json; charset=UTF-8',
      reconnectInterval: 5000,
      timeout: -1,
      maxReconnectOnClose: 10000000,
      trackMessageLength: true,
      enableProtocol: true,
      headers: extraHeaders,
      ...callback,
    });
  }

  private handleMessage(message: ClientMessage) {
    const { id } = message;
    const endpointInfo = this.endpointInfos.get(id);

    if (message['@type'] === 'update') {
      const callback = this.onNextCallbacks.get(id);
      if (callback) {
        callback(message.item);
      }
    } else if (message['@type'] === 'complete') {
      const callback = this.onCompleteCallbacks.get(id);
      if (callback) {
        callback();
      }

      this.removeSubscription(id);
    } else if (message['@type'] === 'error') {
      const callback = this.onErrorCallbacks.get(id);
      if (callback) {
        callback();
      }
      this.removeSubscription(id);
      if (!callback) {
        throw new Error(`Error in ${endpointInfo}: ${message.message}`);
      }
    } else {
      throw new Error(`Unknown message from server: ${message}`);
    }
  }

  private removeSubscription(id: string) {
    this.onNextCallbacks.delete(id);
    this.onCompleteCallbacks.delete(id);
    this.onErrorCallbacks.delete(id);
    this.endpointInfos.delete(id);
  }

  private sendPendingMessages() {
    this.pendingMessages.forEach((msg) => this.send(msg));
    this.pendingMessages = [];
  }
  private send(message: ServerMessage) {
    if (this.state === State.INACTIVE) {
      this.pendingMessages.push(message);
    } else {
      this.socket.push(JSON.stringify(message));
    }
  }

  /**
   * Subscribes to the flux returned by the given endpoint name + method name using the given parameters.
   *
   * @param endpointName the endpoint to connect to
   * @param methodName the method in the endpoint to connect to
   * @param parameters the parameters to use
   * @returns a subscription
   */
  subscribe(endpointName: string, methodName: string, parameters?: Array<any>): Subscription<any> {
    const id: string = this.nextId.toString();
    this.nextId += 1;
    const params = parameters || [];

    const msg: ServerConnectMessage = { '@type': 'subscribe', id, endpointName, methodName, params };
    const endpointInfo = `${endpointName}.${methodName}(${JSON.stringify(params)})`;
    this.send(msg);
    this.endpointInfos.set(id, endpointInfo);
    const hillaSubscription: Subscription<any> = {
      onNext: (callback: (value: any) => void): Subscription<any> => {
        this.onNextCallbacks.set(id, callback);
        return hillaSubscription;
      },
      onComplete: (callback: () => void): Subscription<any> => {
        this.onCompleteCallbacks.set(id, callback);
        return hillaSubscription;
      },
      onError: (callback: () => void): Subscription<any> => {
        this.onErrorCallbacks.set(id, callback);
        return hillaSubscription;
      },
      cancel: () => {
        if (!this.endpointInfos.has(id)) {
          // Subscription already closed or canceled
          return;
        }

        const closeMessage: ServerCloseMessage = { '@type': 'unsubscribe', id };
        this.send(closeMessage);
        this.removeSubscription(id);
      },
      context: (context: ReactiveElement): Subscription<any> => {
        context.addController({
          hostDisconnected: () => {
            hillaSubscription.cancel();
          },
        });
        return hillaSubscription;
      },
    };
    return hillaSubscription;
  }
}

export interface FluxConnection {
  addEventListener<T extends keyof EventMap>(type: T, listener: ListenerType<T>): void;
  removeEventListener<T extends keyof EventMap>(type: T, listener: ListenerType<T>): void;
}
