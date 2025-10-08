/* eslint-disable @typescript-eslint/no-use-before-define, no-param-reassign */
import { computed, signal, type ReadonlySignal, type Signal } from '@preact/signals-core';
import { ActionOnLostSubscription } from '@vaadin/hilla-frontend';
import type { CommandResult } from './CommandResult.js';
import type { SignalCommand } from './commands.js';
import { Connection, type ConnectionConfig } from './Connection.js';
import { apply, type NodeTree, getListValue, getMapValue } from './NodeTree.js';

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

let getId: (signal: FullStackSignal) => number;
let getPublic: (signal: FullStackSignal) => NodeTree;

export abstract class FullStackSignal {
  static {
    getId = (s) => s.#id;
    getPublic = (s) => s.#public.value;
  }

  /**
   * The unique identifier for the node represented by this signal. This ID is
   * used to track and manage the node within the tree structure in
   * communication with the server.
   */
  readonly #id: number;
  readonly #confirmed: Signal<NodeTree>;
  readonly #public: ReadonlySignal<NodeTree>;
  // readonly #state: ReadonlySignal<>
  readonly #unconfirmed = signal(new Map<number, UnconfirmedOperation>());
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

  async run(command: SignalCommand): Promise<CommandResult> {
    const result = await this.#connection.send(command);

    if (result.accepted) {
      const newTree = apply(this.#confirmed.value, this.#id, command);

      if (newTree) {
        this.#confirmed.value = newTree;
      }
    } else {
      this.#unconfirmed.value.delete(command.id);
    }

    return result;
  }

  #connect() {
    this.#connection.establish().onSubscriptionLost(() => ActionOnLostSubscription.RESUBSCRIBE);
  }

  #disconnect() {
    this.#connection.terminate();
  }
}

export class NodeSignal extends FullStackSignal {
  asValue<T>(): ValueSignal<T> {
    return new ValueSignal<T>(this);
  }
  asNumber(): NumberSignal {
    return new NumberSignal(this);
  }
  asList<T>(): ListSignal<T> {
    return new ListSignal<T>(this);
  }
  asMap<T>(): MapSignal<T> {
    return new MapSignal<T>(this);
  }
}

export class ListSignal<T> extends FullStackSignal {
  readonly #value = computed(() => {
    const id = getId(this);
    const tree = getPublic(this);
    return getListValue(tree, tree.get(id)!.listChildren) as readonly T[];
  });

  get value(): readonly T[] {
    return this.#value.value;
  }

  asNode(): NodeSignal {
    return new NodeSignal(this);
  }
}

export class MapSignal<T> extends FullStackSignal {
  readonly #value = computed(() => {
    const id = getId(this);
    const tree = getPublic(this);
    return getMapValue(tree, tree.get(id)!.mapChildren) as Readonly<Record<string, T>>;
  });

  get value(): Readonly<Record<string, T>> {
    return this.#value.value;
  }

  asNode(): NodeSignal {
    return new NodeSignal(this);
  }
}

export class ValueSignal<T> extends FullStackSignal {
  readonly #value = computed(() => {
    const id = getId(this);
    const tree = getPublic(this);
    return tree.get(id)!.value as T;
  });

  value(): T {
    return this.#value.value;
  }
  asNode(): NodeSignal {
    return new NodeSignal(this);
  }
}

export class NumberSignal<T extends bigint | number = number> extends ValueSignal<T> {}
