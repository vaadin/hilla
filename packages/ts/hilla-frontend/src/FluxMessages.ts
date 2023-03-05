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

export type ClientMessage = ClientCompleteMessage | ClientErrorMessage | ClientUpdateMessage;

const clientMessageTypes = ['complete', 'error', 'update'];
export function isClientMessage(obj: object): obj is ClientMessage {
  return '@type' in obj && typeof obj['@type'] === 'string' && clientMessageTypes.includes(obj['@type']);
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

export type ServerMessage = ServerConnectMessage | ServerCloseMessage;
