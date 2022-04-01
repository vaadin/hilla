import { io, Socket } from 'socket.io-client';
import { getCsrfTokenHeadersForEndpointRequest } from './CsrfUtils';
import type { DefaultEventsMap } from '@socket.io/component-emitter';
import type { Subscription } from './Connect';
import type { ClientMessage, ServerCloseMessage, ServerConnectMessage, ServerMessage } from './FluxMessages';

export class FluxConnection {
  private nextId = 0;
  private endpointInfos = new Map<string, string>();
  private onNextCallbacks = new Map<string, (value: any) => void>();
  private onCompleteCallbacks = new Map<string, () => void>();
  private onErrorCallbacks = new Map<string, () => void>();

  private socket!: Socket<DefaultEventsMap, DefaultEventsMap>;

  constructor() {
    this.connectWebsocket();
  }
  private connectWebsocket() {
    const extraHeaders = getCsrfTokenHeadersForEndpointRequest(document);
    this.socket = io('/hilla', { path: '/VAADIN/hillapush/', extraHeaders });
    this.socket.on('message', (message) => {
      this.handleMessage(JSON.parse(message));
    });
  }

  private handleMessage(message: ClientMessage) {
    const id = message.id;
    const endpointInfo = this.endpointInfos.get(id);

    if (message['@type'] === 'update') {
      console.debug(`Got value ${JSON.stringify(message.item)} for ${endpointInfo}`);
      const callback = this.onNextCallbacks.get(id);
      if (!callback) {
        console.log('No callback for stream id ' + id);
        return;
      }
      callback(message.item);
    } else if (message['@type'] === 'complete') {
      console.debug(`Server completed ${endpointInfo}`);
      const callback = this.onCompleteCallbacks.get(id);
      if (callback) {
        callback();
      }

      this.onNextCallbacks.delete(id);
      this.onCompleteCallbacks.delete(id);
      this.onErrorCallbacks.delete(id);
      this.endpointInfos.delete(id);
    } else if (message['@type'] === 'error') {
      console.error(`Error in ${endpointInfo}: ${message.message}`);
      const callback = this.onErrorCallbacks.get(id);
      if (callback) {
        callback();
      }
      this.onNextCallbacks.delete(id);
      this.onCompleteCallbacks.delete(id);
      this.onErrorCallbacks.delete(id);
    } else {
      console.error('Unknown message from server: ' + message);
    }
  }

  private send(message: ServerMessage) {
    this.socket.send(message);
  }

  subscribe(endpointName: string, methodName: string, params?: Array<any>): Subscription<any> {
    const id: string = '' + this.nextId++;

    const msg: ServerConnectMessage = { '@type': 'subscribe', id, endpointName, methodName, params };
    const endpointInfo = `${endpointName}.${methodName}(${JSON.stringify(params)})`;
    console.debug(`Subscribing to ${endpointInfo}`);
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
        console.debug(`Ending subscription to ${endpointInfo}`);

        const closeMessage: ServerCloseMessage = { '@type': 'unsubscribe', id };
        this.send(closeMessage);
      },
    };
    return hillaSubscription;
  }
}
