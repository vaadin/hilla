import type { ActionOnLostSubscription } from '@vaadin/hilla-frontend';
import { isSnapshotCommand, type Id, type SignalCommand } from './commands.js';
import { Connection, type ServerConnectionConfig } from './Connection.js';
import { computed, signal, type ReadonlySignal, type Signal } from './core.js';
import { applyCommand, emptyTree, type NodeTree } from './NodeTree.js';
import type { Operation } from './Operation.js';

type OperationHandlers = Readonly<{
  resolve(): void;
  reject(reason: unknown): void;
}>;

/**
 * The shared state behind one or more full-stack signals.
 * <p>
 * The tree keeps a confirmed state, i.e. the last state that the server has
 * acknowledged, together with a queue of commands that have been submitted but
 * not yet confirmed. The visible state is the confirmed state with all
 * unconfirmed commands replayed on top of it, which gives latency compensation:
 * a local change is visible immediately, and it disappears again if the server
 * rejects it. This mirrors how `AsynchronousSignalTree` works on the server.
 * <p>
 * All signals that belong to the same server-side signal share one tree
 * instance, so a child signal stays up to date without needing a connection of
 * its own.
 *
 * @internal
 */
export class SignalTree {
  /**
   * The connection used to communicate with the server.
   */
  readonly connection: Connection;

  /**
   * The visible state of the tree: the confirmed state with all unconfirmed
   * commands applied on top of it.
   */
  readonly nodes: ReadonlySignal<NodeTree>;

  /**
   * Defines whether there are commands that are waiting for a server response.
   */
  readonly pending: ReadonlySignal<boolean>;

  /**
   * The error of the latest failed command, if any.
   */
  readonly error: ReadonlySignal<Error | undefined>;

  readonly #confirmed: Signal<NodeTree>;
  readonly #unconfirmed: Signal<readonly SignalCommand[]> = signal<readonly SignalCommand[]>([]);
  readonly #error: Signal<Error | undefined> = signal<Error | undefined>(undefined);
  readonly #operations = new Map<Id, OperationHandlers>();

  constructor(id: string, config: ServerConnectionConfig, defaultValue?: unknown) {
    this.connection = new Connection(id, config);
    // The default value is used until the server has sent the actual value, so
    // that commands are applied on top of something meaningful right away
    this.#confirmed = signal(emptyTree(defaultValue));

    this.nodes = computed(
      () => {
        let tree = this.#confirmed.value;

        for (const command of this.#unconfirmed.value) {
          const next = applyCommand(tree, command);
          // A command that is no longer applicable is skipped rather than
          // discarding the whole queue: the remaining commands are still the
          // best guess of what the state will be
          if (next) {
            tree = next;
          }
        }

        return tree;
      },
      {
        watched: () => this.#connect(),
        unwatched: () => this.#disconnect(),
      },
    );

    this.pending = computed(() => this.#unconfirmed.value.length > 0);
    this.error = computed(() => this.#error.value);
  }

  /**
   * Submits a command to the server and applies it optimistically until the
   * server either confirms or rejects it.
   *
   * @param command - The command to submit
   * @returns An operation whose result is resolved when the command is
   * confirmed and rejected when the server rejects it
   */
  submit(command: SignalCommand): Operation {
    // eslint-disable-next-line @typescript-eslint/unbound-method
    const { promise, resolve, reject } = Promise.withResolvers<void>();
    this.#operations.set(command.commandId, { resolve, reject });

    // Reacting to a rejected command is optional, so the promise is always
    // handled here to avoid unhandled rejections
    promise.catch(() => {});

    this.#unconfirmed.value = [...this.#unconfirmed.value, command];
    this.#error.value = undefined;

    // Without a subscription there is no channel for the server to report the
    // outcome back, so the command is confirmed based on the call itself
    const confirmOnCall = !this.connection.isConnected();

    this.connection.send(command).then(
      () => {
        if (!confirmOnCall) {
          return;
        }

        if (this.connection.isConnected()) {
          // Something subscribed while the command was in flight. The snapshot
          // that comes with the subscription already contains the command, so
          // it must not be applied a second time.
          this.#dequeue(command.commandId);
          this.#settle(command.commandId);
        } else {
          this.#confirm(command);
        }
      },
      (error: unknown) => {
        const failure = error instanceof Error ? error : new Error(String(error));
        this.#error.value = failure;
        this.#dequeue(command.commandId);
        this.#settle(command.commandId, failure);
      },
    );

    return { result: promise };
  }

  /**
   * Handles a command that has been processed by the server.
   */
  handleCommand(command: SignalCommand): void {
    if (command.accepted === false) {
      // The command was rejected, so dropping it from the queue reverts the
      // optimistically applied change
      this.#dequeue(command.commandId);
      this.#settle(command.commandId, new Error(command.reason ?? 'The command was rejected by the server'));
      return;
    }

    this.#confirm(command);
  }

  #confirm(command: SignalCommand): void {
    const next = applyCommand(this.#confirmed.value, command);
    if (next) {
      this.#confirmed.value = next;
    }

    // A snapshot describes the whole confirmed state. Any commands that are
    // still unconfirmed are kept in the queue and replayed on top of it, the
    // same way as the server does when confirming commands.
    if (!isSnapshotCommand(command)) {
      this.#dequeue(command.commandId);
    }

    this.#settle(command.commandId);
  }

  #dequeue(commandId: Id): void {
    const remaining = this.#unconfirmed.value.filter((command) => command.commandId !== commandId);

    if (remaining.length !== this.#unconfirmed.value.length) {
      this.#unconfirmed.value = remaining;
    }
  }

  #settle(commandId: Id, reason?: Error): void {
    const operation = this.#operations.get(commandId);
    if (!operation) {
      return;
    }

    this.#operations.delete(commandId);

    if (reason) {
      operation.reject(reason);
    } else {
      operation.resolve();
    }
  }

  #connect(): void {
    this.connection
      .connect()
      .onSubscriptionLost(() => 'resubscribe' as ActionOnLostSubscription)
      .onNext((command: SignalCommand) => this.handleCommand(command));
  }

  #disconnect(): void {
    if (this.connection.isConnected()) {
      this.connection.disconnect();
    }
  }
}
