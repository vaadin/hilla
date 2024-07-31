import { NumberSignalChannel } from '@vaadin/hilla-react-signals';
import client_1 from 'Frontend/generated/connect-client.default.js';

export function sharedValue() {
  const signalChannel = new NumberSignalChannel('NumberSignalProviderService.sharedValue', client_1);
  return signalChannel.signal;
}

export function counter() {
  const signalChannel = new NumberSignalChannel('NumberSignalProviderService.counter', client_1);
  return signalChannel.signal;
}
