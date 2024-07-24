/**
 * Types of events that can be produced or processed by a signal.
 */
export enum StateEventType {
  SET = 'set',
  SNAPSHOT = 'snapshot',
}

/**
 * Event that describes the state of a signal.
 */
export type StateEvent = {
  id: string;
  type: StateEventType;
  value: any;
};
