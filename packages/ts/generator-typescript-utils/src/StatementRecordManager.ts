import type { Statement } from 'typescript';

export type StatementRecord<T extends Statement> = readonly [path: string, declaration: T];

export default abstract class StatementRecordManager<T extends Statement> {
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

  readonly #collator: Intl.Collator;

  public declare ['constructor']: typeof StatementRecordManager;

  public constructor(collator: Intl.Collator) {
    this.#collator = collator;
  }

  public abstract statementRecords(): IterableIterator<StatementRecord<T>>;

  public toCode(): readonly T[] {
    const records = [...this.statementRecords()];
    records.sort(this.constructor.createComparator(this.#collator));

    return records.map(([, statement]) => statement);
  }
}
