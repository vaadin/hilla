import { createClearCommand, createPutCommand, createRemoveByKeyCommand, ZERO } from './commands.js';
import type { ServerConnectionConfig } from './Connection.js';
import { FullStackSignal, type Operation } from './FullStackSignal.js';
import { getMapChildren, type NodeTree } from './NodeTree.js';
import { ValueSignal } from './ValueSignal.js';

/**
 * A signal containing a map of string keys to values. Supports atomic updates
 * to the map structure. Each value in the map is accessed as a separate
 * ValueSignal instance.
 */
export class MapSignal<T> extends FullStackSignal<Map<string, ValueSignal<T>>> {
  constructor(config: ServerConnectionConfig, id?: string) {
    super(new Map(), config, id);
  }

  protected deriveValue(tree: NodeTree): Map<string, ValueSignal<T>> {
    const mapChildren = getMapChildren(tree, '');
    const result = new Map<string, ValueSignal<T>>();
    for (const [key, childId] of mapChildren) {
      const childNode = tree.get(childId);
      if (childNode && 'value' in childNode) {
        result.set(key, new ValueSignal<T>(childNode.value as T, this.getConfig(), childId, this));
      }
    }
    return result;
  }

  override get value(): Map<string, ValueSignal<T>> {
    return super.value;
  }

  /**
   * @readonly
   */
  override set value(_: never) {
    throw new Error('Value of the collection signals cannot be set.');
  }

  /**
   * Sets the value for the given key. If a value already exists for the key,
   * it is replaced. The change is applied optimistically.
   * @param key - The map key
   * @param value - The value to set
   * @returns An operation containing the eventual result
   */
  put(key: string, value: T): Operation {
    return this.sendCommand(createPutCommand<T>(ZERO, key, value));
  }

  /**
   * Removes the entry with the given key.
   * @param key - The key to remove
   * @returns An operation containing the eventual result
   */
  removeKey(key: string): Operation {
    return this.sendCommand(createRemoveByKeyCommand(ZERO, key));
  }

  /**
   * Removes all entries from this map.
   * @returns An operation containing the eventual result
   */
  clear(): Operation {
    return this.sendCommand(createClearCommand(ZERO));
  }
}
