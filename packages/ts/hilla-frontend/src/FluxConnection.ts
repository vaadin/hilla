import type { DefaultEventsMap } from '@socket.io/component-emitter';
import { io, Socket } from 'socket.io-client';
import type { Subscription } from './Connect';
import { getCsrfTokenHeadersForEndpointRequest } from './CsrfUtils';
import type { ClientMessage, ServerCloseMessage, ServerConnectMessage, ServerMessage } from './FluxMessages';

export class FluxConnection {
  private nextId = 0;
  private endpointInfos = new Map<string, string>();
  private onNextCallbacks = new Map<string, (value: any) => void>();
  private onCompleteCallbacks = new Map<string, () => void>();
  private onErrorCallbacks = new Map<string, () => void>();
  private closed = new Set<string>();

  private socket!: Socket<DefaultEventsMap, DefaultEventsMap>;

  constructor() {
    if (!(window as any).Vaadin?.featureFlags?.hillaPush) {
      // Remove when removing feature flag
      throw new Error(
        `Push support in Hilla is not enabled. Enable it in the debug window or by adding com.vaadin.experimental.hillaPush=true to vaadin-featureflags.properties`,
      );
    }
    this.connectWebsocket();
  }

  private connectWebsocket() {
    const extraHeaders = getCsrfTokenHeadersForEndpointRequest(document);
    this.socket = io('/hilla', { path: '/HILLA/push', extraHeaders });
    this.socket.on('message', (message) => {
      this.handleMessage(JSON.parse(message));
    });
  }

  private handleMessage(message: ClientMessage) {
    const { id } = message;
    const endpointInfo = this.endpointInfos.get(id);

    if (message['@type'] === 'update') {
      const callback = this.onNextCallbacks.get(id);
      const closed = this.closed.has(id);
      if (callback && !closed) {
        callback(message.item);
      } else if (!callback) {
        throw new Error(`No callback for stream id ${id}`);
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
    this.closed.delete(id);
  }

  private send(message: ServerMessage) {
    this.socket.send(message);
  }

  subscribe(endpointName: string, methodName: string, maybeParams?: Array<any>): Subscription<any> {
    const id: string = this.nextId.toString();
    this.nextId += 1;
    const params = maybeParams || [];

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
        const closeMessage: ServerCloseMessage = { '@type': 'unsubscribe', id };
        this.send(closeMessage);
        this.closed.add(id);
      },
    };
    return hillaSubscription;
  }
}
