export type EntryId = string;

export type EventCondition = {
  id: EntryId;
  value?: any;
}

export type StateEvent = {
  id: string;
  type: 'set' | 'snapshot';
  conditions?: EventCondition[];
}

export type SetEvent = StateEvent & {
  value: any;
}

export type SnapshotEvent = StateEvent & {
  value: any;
}
