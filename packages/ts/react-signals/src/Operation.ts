/**
 * A return type for signal operations that exposes a `result` property of type
 * `Promise`, that resolves when the operation is confirmed by the server. It
 * allows defining callbacks to be run after the operation is completed, or
 * error handling when the operation is rejected.
 *
 * @example
 * ```ts
 * const sharedName = NameService.sharedName({ defaultValue: '' });
 * sharedName.set('John').result
 *    .then(() => console.log('Name updated successfully'))
 *    .catch((error) => console.error('Failed to update the name:', error));
 * ```
 */
export interface Operation {
  result: Promise<void>;
}

/**
 * An operation that creates a new child signal. The child signal is available
 * immediately, even though the server hasn't confirmed the operation yet.
 *
 * @typeParam S - The type of the created child signal.
 */
export interface InsertOperation<S> extends Operation {
  signal: S;
}
