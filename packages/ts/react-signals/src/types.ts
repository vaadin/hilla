export enum EntryType {
  VALUE,
  NUMBER,
}

export type EntryId = string;

export interface ModifiableEntry<T = any> {
  value: T;
  type: EntryType;
}

export type Entry<T = any> = Readonly<ModifiableEntry<T>>;

export type Entries = Map<EntryId, Entry | undefined>;

export interface FullSignalOptions {
  initialValue: unknown;
  latencyCompensation: boolean;
  delay: number | undefined;
}

export const defaultOptions : FullSignalOptions = {
  initialValue: null,
  latencyCompensation: true,
  delay: undefined,
}

export type SignalOptions = Partial<FullSignalOptions>;

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
  entries: {
    id: EntryId;
    value: any;
  }[];
}
