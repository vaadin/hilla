import type { ActionOnLostSubscription } from '@vaadin/hilla-frontend';
import { isSnapshotCommand, type SignalCommand } from './commands.js';
import { Connection, type ServerConnectionConfig } from './Connection.js';
import { computed, signal, type ReadonlySignal, type Signal } from './core.js';
import { applyCommand, emptyTree, type NodeTree } from './NodeTree.js';
import { randomId } from './utils.js';

/**
 * A return type for signal operations that exposes a `result` property of type
 * `Promise`, that resolves when the operation is completed.
 */
export interface Operation {
  result: Promise<void>;
}

export { type ServerConnectionConfig } from './Connection.js';

/**
 * A signal that holds a shared value. Each change to the value is propagated to
 * the server-side signal provider. At the same time, each change received from
 * the server-side signal provider is propagated to the local signal and its
 * subscribers.
 *
 * Uses an immutable NodeTree with a confirmed + unconfirmed queue architecture.
 * The displayed value is computed from: confirmed tree + all unconfirmed commands.
 * Rejection removes from the queue and the computed re-derives automatically.
 *
 * @internal
 */
export abstract class FullStackSignal<T> {
  /**
   * The unique identifier of the signal necessary to communicate with the
   * server.
   */
  readonly id: string;

  /**
   * The server connection manager.
   */
  readonly connection: Connection;

  /**
   * Defines whether the signal is currently awaiting a server-side response.
   */
  readonly pending: ReadonlySignal<boolean>;

  /**
   * Defines whether the signal has an error.
   */
  readonly error: ReadonlySignal<Error | undefined>;

  /**
   * Optional parent signal for command routing.
   */
  protected readonly parent?: FullStackSignal<any>;

  readonly #confirmed: Signal<NodeTree>;
  readonly #unconfirmed: Signal<SignalCommand[]>;
  readonly #derived: ReadonlySignal<T>;
  readonly #pending: Signal<boolean>;
  readonly #error: Signal<Error | undefined>;

  // Stores the promise handlers associated to operations
  readonly #operationPromises = new Map<
    string,
    {
      resolve(value: PromiseLike<void> | void): void;
      reject(reason?: any): void;
    }
  >();

  constructor(defaultValue: T | undefined, config: ServerConnectionConfig, id?: string, parent?: FullStackSignal<any>) {
    this.id = id ?? randomId();
    this.connection = new Connection(this.id, config);
    this.parent = parent;

    this.#confirmed = signal(emptyTree(defaultValue));
    this.#unconfirmed = signal<SignalCommand[]>([]);
    this.#pending = signal(false);
    this.#error = signal<Error | undefined>(undefined);

    this.pending = computed(() => this.#pending.value);
    this.error = computed(() => this.#error.value);

    this.#derived = computed(
      () => {
        let tree: NodeTree | null = this.#confirmed.value;
        for (const cmd of this.#unconfirmed.value) {
          tree = applyCommand(tree, cmd);
          if (!tree) {
            // If a command fails to apply, fall back to confirmed tree
            tree = this.#confirmed.value;
            break;
          }
        }
        return this.deriveValue(tree);
      },
      !parent
        ? {
            watched: () => this.#connect(),
            unwatched: () => this.#disconnect(),
          }
        : undefined,
    );
  }

  /**
   * Gets the current value derived from the confirmed tree + unconfirmed commands.
   */
  get value(): T {
    return this.#derived.value;
  }

  /**
   * Peeks at the value without subscribing to changes.
   */
  peek(): T {
    return this.#derived.peek();
  }

  /**
   * Subscribes to value changes.
   */
  subscribe(fn: (value: T) => void): () => void {
    return this.#derived.subscribe(fn);
  }

  /**
   * Returns the primitive value.
   */
  valueOf(): T {
    return this.#derived.valueOf();
  }

  /**
   * Returns the string representation.
   */
  toString(): string {
    return this.#derived.toString();
  }

  /**
   * Returns the JSON representation.
   */
  toJSON(): T {
    return this.#derived.toJSON();
  }

  /**
   * The signal brand for React integration.
   */
  get brand(): unknown {
    // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
    return (this.#derived as any).brand;
  }

  /**
   * Derives the public-facing value from the given tree.
   * Each signal subclass implements this to extract its specific value type.
   */
  protected abstract deriveValue(tree: NodeTree): T;

  /**
   * Returns the current confirmed tree. Used by subclasses to create child signals.
   */
  protected getConfirmedTree(): NodeTree {
    return this.#confirmed.value;
  }

  /**
   * Returns the connection config. Used by subclasses to pass to child signals.
   */
  protected getConfig(): ServerConnectionConfig {
    return this.connection.config;
  }

  /**
   * Sends a command to the server. If this signal has a parent, routes the
   * command through the parent with the targetNodeId set to this signal's id.
   */
  protected sendCommand(command: SignalCommand): Operation {
    if (this.parent) {
      const routedCommand = { ...command, targetNodeId: this.id };
      return this.parent.sendCommand(routedCommand);
    }

    // Add to unconfirmed queue
    this.#unconfirmed.value = [...this.#unconfirmed.value, command];
    this.#pending.value = true;
    this.#error.value = undefined;

    // Create the operation promise — resolve/reject are plain functions, not bound methods
    // eslint-disable-next-line @typescript-eslint/unbound-method
    const { promise, resolve, reject } = Promise.withResolvers<void>();
    this.#operationPromises.set(command.commandId, { resolve, reject });

    // Send to server
    this.connection
      .send(command)
      .catch((error: unknown) => {
        this.#error.value = error instanceof Error ? error : new Error(String(error));
      })
      .finally(() => {
        this.#pending.value = false;
      });

    return { result: promise };
  }

  /**
   * Creates an Operation with a pre-resolved promise (used for no-op cases).
   */
  // eslint-disable-next-line @typescript-eslint/class-methods-use-this
  protected createResolvedOperation(): Operation {
    return { result: Promise.resolve() };
  }

  #handleServerCommand(command: SignalCommand): void {
    if (command.accepted === false) {
      // Rejection: remove from unconfirmed queue → computed re-derives automatically
      this.#unconfirmed.value = this.#unconfirmed.value.filter((c) => c.commandId !== command.commandId);
      const operationPromise = this.#operationPromises.get(command.commandId);
      if (operationPromise) {
        this.#operationPromises.delete(command.commandId);
        operationPromise.reject(command.reason ?? 'Command rejected by server');
      }
    } else {
      // Acceptance: apply to confirmed tree, remove from unconfirmed queue
      const newTree = applyCommand(this.#confirmed.value, command);
      if (newTree) {
        this.#confirmed.value = newTree;
      }

      if (isSnapshotCommand(command)) {
        // Snapshot replaces the entire tree — clear all unconfirmed commands
        this.#unconfirmed.value = [];
      } else {
        this.#unconfirmed.value = this.#unconfirmed.value.filter((c) => c.commandId !== command.commandId);
      }

      const operationPromise = this.#operationPromises.get(command.commandId);
      if (operationPromise) {
        this.#operationPromises.delete(command.commandId);
        operationPromise.resolve();
      }
    }
  }

  #connect(): void {
    this.connection
      .connect()
      .onSubscriptionLost(() => 'resubscribe' as ActionOnLostSubscription)
      .onNext((command: SignalCommand) => {
        this.#handleServerCommand(command);
      });
  }

  #disconnect(): void {
    if (this.connection.subscription === undefined) {
      return;
    }
    this.connection.disconnect();
  }
}
