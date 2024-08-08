import type { ConnectClient, Subscription } from '@vaadin/hilla-frontend';
import { nanoid } from 'nanoid';
import type { ValueSignal } from './Signals.js';

const ENDPOINT = 'SignalsHandler';

/**
 * Types of changes that can be produced or processed by a signal.
 */
export enum StateChangeType {
  SET = 'set',
  SNAPSHOT = 'snapshot',
}

/**
 * An object that describes the change of the signal state.
 */
export type StateChange<T> = Readonly<{
  id: string;
  type: StateChangeType;
  value: T;
}>;

export type Value<S extends ValueSignal<unknown>> = S extends ValueSignal<infer T> ? T : never;

export type SignalChannel<S extends ValueSignal<unknown>> = Readonly<{
  id: string;
  subscription: Subscription<StateChange<Value<S>>>;
}>;

/**
 * Creates a signal channel that can be used to communicate with a server-side.
 *
 * The signal channel is responsible for subscribing to the server-side signal
 * and updating the local signal based on the received events.
 *
 * @typeParam S - The type of the signal instance.
 * @param signal - The local signal instance to be updated.
 * @param signalProviderEndpointMethod - The method name of the signal provider
 * service.
 * @param client - The client instance to be used for communication.
 * @returns The id of the created signal channel.
 */
export function createSignalChannel<S extends ValueSignal<unknown>>(
  signal: S,
  signalProviderEndpointMethod: string,
  client: ConnectClient,
): SignalChannel<S> {
  const id = nanoid();

  let paused = false;
  signal.subscribe((value) => {
    if (!paused && value) {
      client
        .call(ENDPOINT, 'update', {
          clientSignalId: id,
          change: { id: nanoid(), type: StateChangeType.SET, value },
        })
        .catch((error) => {
          throw error;
        });
    }
  });

  const subscription: Subscription<StateChange<Value<S>>> = client
    .subscribe(ENDPOINT, 'subscribe', {
      signalProviderEndpointMethod,
      clientSignalId: id,
    })
    .onNext((change: StateChange<Value<S>>) => {
      if (change.type === StateChangeType.SNAPSHOT) {
        paused = true;
        signal.value = change.value;
        paused = false;
      }
    });

  return { id, subscription };
}
