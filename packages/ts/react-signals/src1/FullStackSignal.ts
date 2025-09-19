/* eslint-disable import/no-mutable-exports, no-param-reassign */
import { computed, signal, type ReadonlySignal, type Signal } from '@preact/signals-core';
import { ActionOnLostSubscription } from '@vaadin/hilla-frontend';
import type { SignalCommand } from './commands.js';
import { Connection, type ConnectionConfig } from './Connection.js';
import { apply, type NodeTree, type Node } from './NodeTree.js';

type UnconfirmedOperation = Pick<ReturnType<typeof Promise.withResolvers>, 'resolve' | 'reject'> &
  Readonly<{
    command: SignalCommand;
  }>;

export type FullStackSignalOptions = Readonly<
  ConnectionConfig & {
    parent?: FullStackSignal | null;
    id?: bigint;
    watched?(this: FullStackSignal): void;
    unwatched?(this: FullStackSignal): void;
    connection?: Connection;
  }
>;

export let getNode: (signal: FullStackSignal) => Node;

export abstract class FullStackSignal {
  static {
    getNode = (s) => s.#public.value.get(s.#id)!;
  }

  readonly #id: number;
  readonly #confirmed: Signal<NodeTree>;
  readonly #public: ReadonlySignal<NodeTree>;
  readonly #unconfirmed = signal(new Map<bigint, UnconfirmedOperation>());
  readonly #connection: Connection;
  readonly #options: FullStackSignalOptions;

  constructor(signal: FullStackSignal);
  constructor(tree: NodeTree, id: number, options: FullStackSignalOptions);
  constructor(treeOrSignal: NodeTree | FullStackSignal, id?: number, options?: FullStackSignalOptions) {
    let tree: NodeTree;
    if (treeOrSignal instanceof FullStackSignal) {
      tree = treeOrSignal.#public.value;
      id = treeOrSignal.#id;
      options = treeOrSignal.#options;
    } else {
      tree = treeOrSignal;
    }

    this.#id = id!;
    this.#options = options!;
    this.#connection = options?.connection ?? new Connection(id!, options!);
    this.#confirmed = signal(tree);
    this.#public = computed(
      () => {
        let result: NodeTree | null = this.#confirmed.value;

        for (const { command } of this.#unconfirmed.value.values()) {
          result = apply(result, this.#id, command);

          if (!result) {
            break;
          }
        }

        return result ?? this.#confirmed.value;
      },
      {
        ...options,
        watched: () => {
          this.#connect();
          options!.watched?.call(this);
        },
        unwatched: () => {
          this.#disconnect();
          options!.unwatched?.call(this);
        },
      },
    );
  }

  #connect() {
    this.#connection.establish().onSubscriptionLost(() => ActionOnLostSubscription.RESUBSCRIBE);
  }

  #disconnect() {
    this.#connection.terminate();
  }
}
