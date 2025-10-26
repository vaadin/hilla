import type { Statement } from 'typescript';
import type CodeConvertable from './CodeConvertable.js';

export type StatementRecord<T extends Statement> = readonly [path: string, declaration: T];

export function createComparator<T extends Statement>(
  collator: Intl.Collator,
): (recordA: StatementRecord<T>, recordB: StatementRecord<T>) => number {
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

export default abstract class StatementRecordManager<T extends Statement> implements CodeConvertable<readonly T[]> {
  readonly collator: Intl.Collator;

  constructor(collator: Intl.Collator) {
    this.collator = collator;
  }

  abstract statementRecords(): IterableIterator<StatementRecord<T>>;

  toCode(): readonly T[] {
    const records = [...this.statementRecords()];
    records.sort(createComparator(this.collator));

    return records.map(([, statement]) => statement);
  }

  abstract clear(): void;
}
