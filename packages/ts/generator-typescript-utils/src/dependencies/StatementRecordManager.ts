import type { Statement } from 'typescript';
import type CodeConvertable from './CodeConvertable.js';

export type StatementRecord<T extends Statement> = readonly [path: string, declaration: T];

export default abstract class StatementRecordManager<T extends Statement> implements CodeConvertable<readonly T[]> {
  public static createComparator<T extends Statement>(
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

  public declare ['constructor']: typeof StatementRecordManager;
  readonly #collator: Intl.Collator;

  public constructor(collator: Intl.Collator) {
    this.#collator = collator;
  }

  public abstract statementRecords(): IterableIterator<StatementRecord<T>>;

  public toCode(): readonly T[] {
    const records = [...this.statementRecords()];
    records.sort(this.constructor.createComparator(this.#collator));

    return records.map(([, statement]) => statement);
  }

  public abstract clear(): void;
}
