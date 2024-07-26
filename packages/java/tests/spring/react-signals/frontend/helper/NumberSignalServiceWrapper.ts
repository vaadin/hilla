import { NumberSignalChannel } from '@vaadin/hilla-react-signals';
import client_1 from 'Frontend/generated/connect-client.default.js';

export class NumberSignalServiceWrapper {
  static sharedValue() {
    const signalChannel = new NumberSignalChannel('NumberSignalProviderService.sharedValue', client_1);
    return signalChannel.signal;
  }

  static counter() {
    const signalChannel = new NumberSignalChannel('NumberSignalProviderService.counter', client_1);
    return signalChannel.signal;
  }
}
