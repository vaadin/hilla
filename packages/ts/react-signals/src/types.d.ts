
export enum EntryType {
  VALUE,
  NUMBER,
}

type EntryId = string;

interface ModifiableEntry<T = any> {
  value: T;
  next: EntryId | null;
  prev: EntryId | null;
  type: EntryType;
}

type Entry<T = any> = Readonly<ModifiableEntry<T>>;

type Entries = Map<EntryId, Entry | undefined>;

interface FullSignalOptions {
  delay: boolean;
  initialValue: any;
}

const defaultOptions : FullSignalOptions = {
  delay: false,
  initialValue: null,
}

type SignalOptions = Partial<FullSignalOptions>;

interface EventCondition {
  id: EntryId;
  value?: any;
  // TODO add conditions for prev / next pointers
}

interface StateEvent {
  id: string;
  conditions?: EventCondition[];
}

interface SetEvent extends StateEvent {
  set: EntryId;
  value: any;
}

interface SnapshotEvent extends StateEvent {
  entries: {
    id: EntryId;
    next: EntryId | null;
    prev: EntryId | null;
    value: any;
  }[];
}

// Entry value for the root of a list
interface ListRoot {
  head: string | null,
  tail: string | null
}
