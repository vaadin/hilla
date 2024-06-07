
export type EntryId = string;

export interface EventCondition {
  id: EntryId;
  value?: any;
}

export interface StateEvent {
  id: string;
  conditions?: EventCondition[];
}

export interface SetEvent extends StateEvent {
  set: 'id';
  value: any;
}

export interface SnapshotEvent extends StateEvent {
  value: any;
}
