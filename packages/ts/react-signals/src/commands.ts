import { randomId } from './utils';

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
    commandId: randomId(),
    targetNodeId,
    type: 'ValueCondition',
    expectedValue,
  };
}

/**
 * A signal command that sets a value.
 */
export type SetCommand<V> = CreateCommandType<'SetCommand', { value: V }>;

export function createSetCommand<V>(targetNodeId: Id, value: V): SetCommand<V> {
  return {
    commandId: randomId(),
    targetNodeId,
    type: 'SetCommand',
    value,
  };
}

/**
 * A signal command that increments a numeric value.
 */
export type IncrementCommand = CreateCommandType<'IncrementCommand', { value: number }>;

export function createIncrementCommand(targetNodeId: Id, value: number): IncrementCommand {
  return {
    commandId: randomId(),
    targetNodeId,
    type: 'IncrementCommand',
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
    commandId: randomId(),
    targetNodeId: '',
    type: 'TransactionCommand',
    commands,
  };
}

/**
 * A signal command that inserts a value into a list at a given position.
 */
export type InsertCommand<V> = CreateCommandType<
  'InsertCommand',
  {
    value: V;
    position: ListPosition;
  }
>;

// ZERO constant to represent the default id (like Id.ZERO in Java)
export const ZERO: Id = '';

export function createInsertCommand<V>(targetNodeId: Id, value: V, position: ListPosition): InsertCommand<V> {
  return {
    commandId: randomId(),
    targetNodeId,
    type: 'InsertCommand',
    value,
    position,
  };
}

export type ListPosition = {
  after?: Id | null;
  before?: Id | null;
};

// EDGE constant to represent the edge of the list (like Id.EDGE in Java)
export const EDGE: Id = '';

function idOf(signal: { id: Id } | null | undefined): Id {
  return signal?.id ?? EDGE;
}

// ListPosition helpers to match Java API.
export const ListPosition = {
  /**
   * Gets the insertion position that corresponds to the beginning of the list.
   * After edge.
   */
  first(): ListPosition {
    return { after: EDGE, before: null };
  },
  /**
   * Gets the insertion position that corresponds to the end of the list.
   * Before edge.
   */
  last(): ListPosition {
    return { after: null, before: EDGE };
  },
  /**
   * Gets the insertion position immediately after the given signal.
   * Inserting after null is interpreted as after the start of the list (first).
   */
  after(signal: { id: Id } | null): ListPosition {
    return { after: idOf(signal), before: null };
  },
  /**
   * Gets the insertion position immediately before the given signal.
   * Inserting before null is interpreted as before the end of the list (last).
   */
  before(signal: { id: Id } | null): ListPosition {
    return { after: null, before: idOf(signal) };
  },
  /**
   * Gets the insertion position between the given signals.
   * Inserting after null is after the start (first), before null is before the end (last).
   */
  between(after: { id: Id } | null, before: { id: Id } | null): ListPosition {
    return { after: idOf(after), before: idOf(before) };
  },
};

/**
 * A signal command that moves a child to a new position in a list.
 */
export type AdoptAtCommand = CreateCommandType<
  'AdoptAtCommand',
  {
    childId: Id;
    position: ListPosition;
  }
>;

export function createAdoptAtCommand(targetNodeId: Id, childId: Id, position: ListPosition): AdoptAtCommand {
  return {
    commandId: randomId(),
    targetNodeId,
    type: 'AdoptAtCommand',
    childId,
    position,
  };
}

/**
 * A signal command that checks a child's position in a list.
 */
export type PositionCondition = CreateCommandType<
  'PositionCondition',
  {
    childId: Id;
    expectedPosition: ListPosition;
  }
>;

export function createPositionCondition(
  targetNodeId: Id,
  childId: Id,
  expectedPosition: ListPosition,
): PositionCondition {
  return {
    commandId: randomId(),
    targetNodeId,
    type: 'PositionCondition',
    childId,
    expectedPosition,
  };
}

/**
 * A signal command that removes a child from a list.
 */
export type RemoveCommand = CreateCommandType<
  'RemoveCommand',
  {
    childId: Id;
  }
>;

export function createRemoveCommand(targetNodeId: Id, childId: Id): RemoveCommand {
  return {
    commandId: randomId(),
    targetNodeId,
    type: 'RemoveCommand',
    childId,
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
  return isSignalCommand(command) && command.type === 'SetCommand';
}

export function isValueCondition<V>(command: unknown): command is ValueCondition<V> {
  return isSignalCommand(command) && command.type === 'ValueCondition';
}

export function isIncrementCommand(command: unknown): command is IncrementCommand {
  return isSignalCommand(command) && command.type === 'IncrementCommand';
}

export function isTransactionCommand(command: unknown): command is TransactionCommand {
  return (
    isSignalCommand(command) &&
    command.type === 'TransactionCommand' &&
    command.targetNodeId === '' &&
    Array.isArray((command as TransactionCommand).commands)
  );
}

export function isInsertCommand<V>(command: unknown): command is InsertCommand<V> {
  return isSignalCommand(command) && command.type === 'InsertCommand';
}

export function isAdoptAtCommand(command: unknown): command is AdoptAtCommand {
  return isSignalCommand(command) && command.type === 'AdoptAtCommand';
}

export function isPositionCondition(command: unknown): command is PositionCondition {
  return isSignalCommand(command) && command.type === 'PositionCondition';
}

export function isRemoveCommand(command: unknown): command is RemoveCommand {
  return isSignalCommand(command) && command.type === 'RemoveCommand';
}
