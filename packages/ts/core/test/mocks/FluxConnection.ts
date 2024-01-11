/* eslint-disable import/export */
import sinon from 'sinon';
import { FluxConnection as _FluxConnection } from '../../src/FluxConnection.js';

export * from '../../src/FluxConnection.js';

export type FluxConnectionSubscribeStub = sinon.SinonStub<
  Parameters<_FluxConnection['subscribe']>,
  ReturnType<_FluxConnection['subscribe']>
>;

export const fluxConnectionSubscriptionStubs: FluxConnectionSubscribeStub[] = [];

export class FluxConnection extends _FluxConnection {
  constructor(...args: ConstructorParameters<typeof _FluxConnection>) {
    super(...args);
    const stub = sinon.stub(this as _FluxConnection, 'subscribe');
    fluxConnectionSubscriptionStubs.push(stub);
  }
}
