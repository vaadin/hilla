export interface AbstractMessage {
  id: string;
}

export interface ClientErrorMessage extends AbstractMessage {
  '@type': 'error';
  message: string;
}
export interface ClientCompleteMessage extends AbstractMessage {
  '@type': 'complete';
}
export interface ClientUpdateMessage extends AbstractMessage {
  '@type': 'update';
  item: any;
}

export type ClientMessage = ClientUpdateMessage | ClientCompleteMessage | ClientErrorMessage;

export interface ServerConnectMessage extends AbstractMessage {
  id: string;
  '@type': 'subscribe';
  endpointName: string;
  methodName: string;
  params?: any;
}
export interface ServerCloseMessage extends AbstractMessage {
  id: string;
  '@type': 'unsubscribe';
}

export type ServerMessage = ServerConnectMessage | ServerCloseMessage;
