import { CollectionSignal } from './CollectionSignal.js';
import {
  createClearCommand,
  createPutCommand,
  createRemoveByKeyCommand,
  isClearCommand,
  isPutCommand,
  isRemoveByKeyCommand,
  isSnapshotCommand,
  ZERO,
  type ClearCommand,
  type PutCommand,
  type RemoveByKeyCommand,
  type SignalCommand,
  type SnapshotCommand,
} from './commands.js';
import {
  $createOperation,
  $handleRejection,
  $processServerResponse,
  $resolveOperation,
  $setValueQuietly,
  $update,
  type Operation,
  type ServerConnectionConfig,
} from './FullStackSignal.js';
import { ValueSignal } from './ValueSignal.js';

/**
 * A signal containing a map of string keys to values. Supports atomic updates
 * to the map structure. Each value in the map is accessed as a separate
 * ValueSignal instance.
 */
export class MapSignal<T> extends CollectionSignal<Map<string, ValueSignal<T>>> {
  /** Pending put operations applied optimistically. */
  readonly #pendingPuts = new Set<string>();

  /** Pending remove-by-key operations: commandId to key and child for potential revert. */
  readonly #pendingRemoves = new Map<string, { key: string; child: ValueSignal<T> }>();

  constructor(config: ServerConnectionConfig, id?: string) {
    super(new Map(), config, id);
  }

  /**
   * Sets the value for the given key. If a value already exists for the key,
   * it is replaced. The change is applied optimistically.
   * @param key - The map key
   * @param value - The value to set
   * @returns An operation containing the eventual result
   */
  put(key: string, value: T): Operation {
    const command = createPutCommand<T>(ZERO, key, value);
    const promise = this[$update](command);
    // Optimistic put
    const valueSignal = new ValueSignal<T>(value, this.server.config, command.commandId, this);
    const newMap = new Map(this.value);
    newMap.set(key, valueSignal);
    this[$setValueQuietly](newMap);
    this.#pendingPuts.add(command.commandId);
    return this[$createOperation]({ id: command.commandId, promise });
  }

  /**
   * Removes the entry with the given key. The removal is applied
   * optimistically.
   * @param key - The key to remove
   * @returns An operation containing the eventual result
   */
  removeKey(key: string): Operation {
    const command = createRemoveByKeyCommand(ZERO, key);
    const promise = this[$update](command);
    // Optimistic remove
    const existing = this.value.get(key);
    if (existing) {
      this.#pendingRemoves.set(command.commandId, { key, child: existing });
      const newMap = new Map(this.value);
      newMap.delete(key);
      this[$setValueQuietly](newMap);
    }
    return this[$createOperation]({ id: command.commandId, promise });
  }

  /**
   * Removes all entries from this map.
   * @returns An operation containing the eventual result
   */
  clear(): Operation {
    const command = createClearCommand(ZERO);
    const promise = this[$update](command);
    this[$setValueQuietly](new Map());
    this.#pendingPuts.clear();
    this.#pendingRemoves.clear();
    return this[$createOperation]({ id: command.commandId, promise });
  }

  protected override [$processServerResponse](
    command: PutCommand<T> | RemoveByKeyCommand | ClearCommand | SnapshotCommand,
  ): void {
    if (isPutCommand<T>(command)) {
      if (this.#pendingPuts.has(command.commandId)) {
        // Our own put — already applied optimistically
        this.#pendingPuts.delete(command.commandId);
      } else {
        // Remote put
        const valueSignal = new ValueSignal<T>(command.value, this.server.config, command.commandId, this);
        const newMap = new Map(this.value);
        newMap.set(command.key, valueSignal);
        this[$setValueQuietly](newMap);
      }
      this[$resolveOperation](command.commandId, undefined);
    } else if (isRemoveByKeyCommand(command)) {
      if (this.#pendingRemoves.has(command.commandId)) {
        // Our own remove — already applied optimistically
        this.#pendingRemoves.delete(command.commandId);
      } else {
        // Remote remove
        const newMap = new Map(this.value);
        newMap.delete(command.key);
        this[$setValueQuietly](newMap);
      }
      this[$resolveOperation](command.commandId, undefined);
    } else if (isClearCommand(command)) {
      this[$setValueQuietly](new Map());
      this.#pendingPuts.clear();
      this.#pendingRemoves.clear();
      this[$resolveOperation](command.commandId, undefined);
    } else if (isSnapshotCommand(command)) {
      const { nodes } = command;
      const mapNode = nodes[''];
      const newMap = new Map<string, ValueSignal<T>>();

      if ('mapChildren' in mapNode) {
        for (const [key, childId] of Object.entries(mapNode.mapChildren)) {
          const childNode = nodes[childId];
          if ('value' in childNode) {
            newMap.set(key, new ValueSignal<T>(childNode.value as T, this.server.config, childId, this));
          }
        }
      }

      this[$setValueQuietly](newMap);
      this.#pendingPuts.clear();
      this.#pendingRemoves.clear();
      this[$resolveOperation](command.commandId, undefined);
    }
  }

  protected override [$handleRejection](command: SignalCommand): void {
    if (isPutCommand(command) && this.#pendingPuts.has(command.commandId)) {
      // Revert optimistic put — find and remove by commandId
      const newMap = new Map(this.value);
      for (const [key, signal] of newMap) {
        if (signal.id === command.commandId) {
          newMap.delete(key);
          break;
        }
      }
      this[$setValueQuietly](newMap);
      this.#pendingPuts.delete(command.commandId);
    } else if (isRemoveByKeyCommand(command)) {
      const removed = this.#pendingRemoves.get(command.commandId);
      if (removed) {
        // Revert optimistic remove — re-add the entry
        const newMap = new Map(this.value);
        newMap.set(removed.key, removed.child);
        this[$setValueQuietly](newMap);
        this.#pendingRemoves.delete(command.commandId);
      }
    }
    super[$handleRejection](command);
  }
}
