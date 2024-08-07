import type { ConnectClient } from '@vaadin/hilla-frontend';
import type { ValueSignal } from './Signals.js';
import { type StateEvent, StateEventType } from './types.js';

const ENDPOINT = 'SignalsHandler';

/**
 * A signal channel can be used to communicate with a server-side signal
 * instance.
 *
 * The signal channel is responsible for subscribing to the server-side signal
 * and updating the local signal based on the received events.
 *
 * @typeParam T - The type of the signal value.
 * @typeParam S - The type of the signal instance.
 */
export class SignalChannel<T, S extends ValueSignal<T>> {
  readonly #id = crypto.randomUUID();
  readonly #client: ConnectClient;
  readonly #signalProviderEndpointMethod: string;
  readonly #internalSignal: S;
  #paused = false;

  constructor(createSignal: () => S, signalProviderServiceMethod: string, client: ConnectClient) {
    this.#client = client;
    this.#signalProviderEndpointMethod = signalProviderServiceMethod;
    this.#internalSignal = createSignal();
    this.#internalSignal.subscribe((value) => {
      if (!this.#paused) {
        this.publish({ id: crypto.randomUUID(), type: StateEventType.SET, value }).catch((error) => {
          throw error;
        });
      }
    });

    this.#connect();
  }

  #connect() {
    this.#client
      .subscribe(ENDPOINT, 'subscribe', {
        signalProviderEndpointMethod: this.#signalProviderEndpointMethod,
        clientSignalId: this.#id,
      })
      .onNext((stateEvent) => {
        // Update signals based on the new value from the event:
        this.#updateSignals(stateEvent);
      });
  }

  #updateSignals(stateEvent: StateEvent): void {
    if (stateEvent.type === StateEventType.SNAPSHOT) {
      this.#paused = true;
      this.#internalSignal.value = stateEvent.value;
      this.#paused = false;
    }
  }

  async publish(event: StateEvent): Promise<boolean> {
    await this.#client.call(ENDPOINT, 'update', {
      clientSignalId: this.#id,
      event,
    });
    return true;
  }

  /**
   * Returns the signal instance to be used in components.
   */
  get signal(): S {
    return this.#internalSignal;
  }

  /**
   * Returns the id of the signal channel.
   */
  get id(): string {
    return this.#id;
  }
}
