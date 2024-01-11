import ts, { type Identifier, type ImportDeclaration, type Statement } from 'typescript';
import createFullyUniqueIdentifier from '../createFullyUniqueIdentifier.js';
import type CodeConvertable from './CodeConvertable.js';
import StatementRecordManager, { type StatementRecord } from './StatementRecordManager.js';
import { createDependencyRecord, type DependencyRecord } from './utils.js';

export class NamedImportManager extends StatementRecordManager<ImportDeclaration> {
  readonly #collator: Intl.Collator;
  readonly #map = new Map<string, Map<string, DependencyRecord>>();

  constructor(collator: Intl.Collator) {
    super(collator);
    this.#collator = collator;
  }

  add(path: string, specifier: string, isType?: boolean, uniqueId?: Identifier): Identifier {
    const record = createDependencyRecord(uniqueId ?? createFullyUniqueIdentifier(specifier), isType);

    if (this.#map.has(path)) {
      this.#map.get(path)!.set(specifier, record);
    } else {
      this.#map.set(path, new Map([[specifier, record]]));
    }

    return record.id;
  }

  override clear(): void {
    this.#map.clear();
  }

  getIdentifier(path: string, specifier: string): Identifier | undefined {
    return this.#map.get(path)?.get(specifier)?.id;
  }

  *identifiers(): IterableIterator<readonly [path: string, specifier: string, id: Identifier, isType: boolean]> {
    for (const [path, specifiers] of this.#map) {
      for (const [specifier, { id, isType }] of specifiers) {
        yield [path, specifier, id, isType];
      }
    }
  }

  isType(path: string, specifier: string): boolean | undefined {
    return this.#map.get(path)?.get(specifier)?.isType;
  }

  paths(): IterableIterator<string> {
    return this.#map.keys();
  }

  *specifiers(): IterableIterator<readonly [path: string, specifier: string]> {
    for (const [path, specifiers] of this.#map) {
      for (const specifier of specifiers.keys()) {
        yield [path, specifier];
      }
    }
  }

  override *statementRecords(): IterableIterator<StatementRecord<ImportDeclaration>> {
    for (const [path, specifiers] of this.#map) {
      const names = [...specifiers.keys()];
      // eslint-disable-next-line @typescript-eslint/unbound-method
      names.sort(this.#collator.compare);

      yield [
        path,
        ts.factory.createImportDeclaration(
          undefined,
          ts.factory.createImportClause(
            false,
            undefined,
            ts.factory.createNamedImports(
              names.map((name) => {
                const { id, isType } = specifiers.get(name)!;
                return ts.factory.createImportSpecifier(isType, ts.factory.createIdentifier(name), id);
              }),
            ),
          ),
          ts.factory.createStringLiteral(path),
        ),
      ];
    }
  }
}

export class NamespaceImportManager extends StatementRecordManager<ImportDeclaration> {
  readonly #map = new Map<string, Identifier>();

  add(path: string, name: string, uniqueId?: Identifier): Identifier {
    const id = uniqueId ?? createFullyUniqueIdentifier(name);
    this.#map.set(path, id);
    return id;
  }

  override clear(): void {
    this.#map.clear();
  }

  getIdentifier(path: string): Identifier | undefined {
    return this.#map.get(path);
  }

  *identifiers(): IterableIterator<Identifier> {
    for (const id of this.#map.values()) {
      yield id;
    }
  }

  paths(): IterableIterator<string> {
    return this.#map.keys();
  }

  override *statementRecords(): IterableIterator<StatementRecord<ImportDeclaration>> {
    for (const [path, id] of this.#map) {
      yield [
        path,
        ts.factory.createImportDeclaration(
          undefined,
          ts.factory.createImportClause(false, undefined, ts.factory.createNamespaceImport(id)),
          ts.factory.createStringLiteral(path),
        ),
      ];
    }
  }
}

export class DefaultImportManager extends StatementRecordManager<ImportDeclaration> {
  readonly #map = new Map<string, DependencyRecord>();

  add(path: string, name: string, isType?: boolean, uniqueId?: Identifier): Identifier {
    const id = uniqueId ?? createFullyUniqueIdentifier(name);
    this.#map.set(path, createDependencyRecord(id, isType));
    return id;
  }

  getIdentifier(path: string): Identifier | undefined {
    return this.#map.get(path)?.id;
  }

  override clear(): void {
    this.#map.clear();
  }

  *identifiers(): IterableIterator<readonly [id: Identifier, isType: boolean]> {
    for (const { id, isType } of this.#map.values()) {
      yield [id, isType];
    }
  }

  isType(path: string): boolean | undefined {
    return this.#map.get(path)?.isType;
  }

  paths(): IterableIterator<string> {
    return this.#map.keys();
  }

  override *statementRecords(): IterableIterator<StatementRecord<ImportDeclaration>> {
    for (const [path, { id, isType }] of this.#map) {
      yield [
        path,
        ts.factory.createImportDeclaration(
          undefined,
          ts.factory.createImportClause(isType, id, undefined),
          ts.factory.createStringLiteral(path),
        ),
      ];
    }
  }
}

export default class ImportManager implements CodeConvertable<readonly Statement[]> {
  readonly default: DefaultImportManager;
  readonly named: NamedImportManager;
  readonly namespace: NamespaceImportManager;

  readonly #collator: Intl.Collator;

  constructor(collator: Intl.Collator) {
    this.default = new DefaultImportManager(collator);
    this.named = new NamedImportManager(collator);
    this.namespace = new NamespaceImportManager(collator);
    this.#collator = collator;
  }

  toCode(): readonly Statement[] {
    const records = [
      ...this.default.statementRecords(),
      ...this.named.statementRecords(),
      ...this.namespace.statementRecords(),
    ];
    records.sort(StatementRecordManager.createComparator(this.#collator));

    return records.map(([, statement]) => statement);
  }

  fromCode(source: ts.SourceFile): void {
    this.default.clear();
    this.named.clear();
    this.namespace.clear();

    const imports = source.statements.filter((statement): statement is ImportDeclaration =>
      ts.isImportDeclaration(statement),
    );

    for (const { importClause, moduleSpecifier } of imports) {
      if (!importClause) {
        // eslint-disable-next-line no-continue
        continue;
      }

      const { name, namedBindings } = importClause;
      const path = (moduleSpecifier as ts.StringLiteral).text;

      if (namedBindings) {
        if (ts.isNamespaceImport(namedBindings)) {
          this.namespace.add(path, namedBindings.name.text, namedBindings.name);
        } else {
          for (const { isTypeOnly, name: specifier } of namedBindings.elements) {
            this.named.add(path, specifier.text, isTypeOnly, specifier);
          }
        }
      } else if (name) {
        this.default.add(path, name.text, importClause.isTypeOnly, name);
      }
    }
  }
}
