import type { Identifier, Statement } from 'typescript';

export type DependencyRecord = Readonly<{
  id: Identifier;
  isType: boolean;
}>;

export type PathRecord<T extends Statement> = readonly [path: string, declaration: T];

export function createDependencyRecord(id: Identifier, isType = false): DependencyRecord {
  return {
    id,
    isType,
  };
}

export function createPathRecordComparator<T extends Statement>(
  collator: Intl.Collator,
): (recordA: PathRecord<T>, recordB: PathRecord<T>) => number {
  return ([pathA], [pathB]) => {
    if (pathA.startsWith('.') && !pathB.startsWith('.')) {
      return 1;
    }

    if (!pathA.startsWith('.') && pathB.startsWith('.')) {
      return -1;
    }

    return collator.compare(pathA, pathB);
  };
}
