import { nanoid } from 'nanoid';

/**
 * A base64 string value emitted by Jackson's JsonValue annotated properties.
 */
export type Id = string;

/**
 * A command triggered from a signal.
 */
export type SignalCommand = Readonly<{
  commandId: Id;
  targetNodeId: Id;
  type: string;
}>;

/**
 * Creates a new state event type.
 */
type CreateCommandType<T extends string, E extends Record<string, unknown> = Record<never, never>> = Readonly<{
  type: T;
}> &
  Readonly<E> &
  SignalCommand;

/**
 * A signal command that doesn't apply any change but only performs a test
 * whether the given node has the expected value, based on JSON equality.
 */
export type ValueCondition<V> = CreateCommandType<'ValueCondition', { expectedValue: V }>;

export function createValueCondition<V>(targetNodeId: Id, expectedValue: V): ValueCondition<V> {
  return {
    commandId: nanoid(),
    targetNodeId,
    type: 'ValueCondition',
    expectedValue,
  };
}

/**
 * A signal command that sets a value.
 */
export type SetCommand<V> = CreateCommandType<'set', { value: V }>;

export function createSetCommand<V>(targetNodeId: Id, value: V): SetCommand<V> {
  return {
    commandId: nanoid(),
    targetNodeId,
    type: 'set',
    value,
  };
}

/**
 * A sequence of commands that should be applied atomically and only if all
 * commands are individually accepted.
 */
export type TransactionCommand = CreateCommandType<
  'TransactionCommand',
  {
    commands: SignalCommand[];
  }
>;

export function createTransactionCommand(commands: SignalCommand[]): TransactionCommand {
  return {
    commandId: nanoid(),
    targetNodeId: '',
    type: 'TransactionCommand',
    commands,
  };
}

// TypeGuard functions:

function isSignalCommand(command: unknown): command is SignalCommand {
  return (
    typeof command === 'object' &&
    command !== null &&
    typeof (command as { commandId?: unknown }).commandId === 'string' &&
    typeof (command as { targetNodeId?: unknown }).targetNodeId === 'string' &&
    typeof (command as { type?: unknown }).type === 'string'
  );
}

export function isSetCommand<V>(command: unknown): command is SetCommand<V> {
  return isSignalCommand(command) && command.type === 'set';
}

export function isValueCondition<V>(command: unknown): command is ValueCondition<V> {
  return isSignalCommand(command) && command.type === 'ValueCondition';
}

export function isTransactionCommand(command: unknown): command is TransactionCommand {
  return (
    isSignalCommand(command) &&
    command.type === 'TransactionCommand' &&
    command.targetNodeId === '' &&
    Array.isArray((command as TransactionCommand).commands)
  );
}
