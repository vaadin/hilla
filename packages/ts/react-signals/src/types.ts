/**
 * Event that describes the state of a signal.
 */
export type StateEvent = {
  id: string;
  type: 'set' | 'snapshot';
};

/**
 * Event that describes setting the value
 * of a full-stack signal.
 */
export type SetEvent = StateEvent & {
  value: any;
};

/**
 * Event that describes a received updated
 * value of a full-stack signal.
 */
export type SnapshotEvent = StateEvent & {
  value: any;
};
