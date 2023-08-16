export interface AbstractMessage {
  '@type': string;
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

export type ClientMessage = ClientCompleteMessage | ClientErrorMessage | ClientUpdateMessage;

export function isClientMessage(value: unknown): value is ClientMessage {
  return value != null && typeof value === 'object' && '@type' in value;
}

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

export type ServerMessage = ServerCloseMessage | ServerConnectMessage;
