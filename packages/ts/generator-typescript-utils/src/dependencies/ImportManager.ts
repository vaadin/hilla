import type { Identifier, ImportDeclaration, Statement } from 'typescript';
import ts, { NamedImports } from 'typescript';
import createFullyUniqueIdentifier from '../createFullyUniqueIdentifier.js';
import type CodeConvertable from './CodeConvertable.js';
import StatementRecordManager, { StatementRecord } from './StatementRecordManager.js';
import type { DependencyRecord } from './utils.js';
import { createDependencyRecord } from './utils.js';

export class NamedImportManager extends StatementRecordManager<ImportDeclaration> {
  readonly #collator: Intl.Collator;
  readonly #map = new Map<string, Map<string, DependencyRecord>>();

  public constructor(collator: Intl.Collator) {
    super(collator);
    this.#collator = collator;
  }

  public add(path: string, specifier: string, isType?: boolean, uniqueId?: Identifier): Identifier {
    const record = createDependencyRecord(uniqueId ?? createFullyUniqueIdentifier(specifier), isType);

    if (this.#map.has(path)) {
      this.#map.get(path)!.set(specifier, record);
    } else {
      this.#map.set(path, new Map([[specifier, record]]));
    }

    return record.id;
  }

  public override clear() {
    this.#map.clear();
  }

  public getIdentifier(path: string, specifier: string): Identifier | undefined {
    return this.#map.get(path)?.get(specifier)?.id;
  }

  public *identifiers(): IterableIterator<readonly [path: string, specifier: string, id: Identifier, isType: boolean]> {
    for (const [path, specifiers] of this.#map) {
      for (const [specifier, { id, isType }] of specifiers) {
        yield [path, specifier, id, isType];
      }
    }
  }

  public isType(path: string, specifier: string): boolean | undefined {
    return this.#map.get(path)?.get(specifier)?.isType;
  }

  public paths(): IterableIterator<string> {
    return this.#map.keys();
  }

  public *specifiers(): IterableIterator<readonly [path: string, specifier: string]> {
    for (const [path, specifiers] of this.#map) {
      for (const specifier of specifiers.keys()) {
        yield [path, specifier];
      }
    }
  }

  public override *statementRecords(): IterableIterator<StatementRecord<ImportDeclaration>> {
    for (const [path, specifiers] of this.#map) {
      const names = [...specifiers.keys()];
      names.sort(this.#collator.compare);

      yield [
        path,
        ts.factory.createImportDeclaration(
          undefined,
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

  public add(path: string, name: string, uniqueId?: Identifier): Identifier {
    const id = uniqueId ?? createFullyUniqueIdentifier(name);
    this.#map.set(path, id);
    return id;
  }

  public override clear() {
    this.#map.clear();
  }

  public getIdentifier(path: string): Identifier | undefined {
    return this.#map.get(path);
  }

  public *identifiers(): IterableIterator<Identifier> {
    for (const id of this.#map.values()) {
      yield id;
    }
  }

  public paths(): IterableIterator<string> {
    return this.#map.keys();
  }

  public override *statementRecords(): IterableIterator<StatementRecord<ImportDeclaration>> {
    for (const [path, id] of this.#map) {
      yield [
        path,
        ts.factory.createImportDeclaration(
          undefined,
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

  public add(path: string, name: string, isType?: boolean, uniqueId?: Identifier): Identifier {
    const id = uniqueId ?? createFullyUniqueIdentifier(name);
    this.#map.set(path, createDependencyRecord(id, isType));
    return id;
  }

  public getIdentifier(path: string): Identifier | undefined {
    return this.#map.get(path)?.id;
  }

  public override clear() {
    this.#map.clear();
  }

  public *identifiers(): IterableIterator<readonly [id: Identifier, isType: boolean]> {
    for (const { id, isType } of this.#map.values()) {
      yield [id, isType];
    }
  }

  public isType(path: string): boolean | undefined {
    return this.#map.get(path)?.isType;
  }

  public paths(): IterableIterator<string> {
    return this.#map.keys();
  }

  public override *statementRecords(): IterableIterator<StatementRecord<ImportDeclaration>> {
    for (const [path, { id, isType }] of this.#map) {
      yield [
        path,
        ts.factory.createImportDeclaration(
          undefined,
          undefined,
          ts.factory.createImportClause(isType, id, undefined),
          ts.factory.createStringLiteral(path),
        ),
      ];
    }
  }
}

export default class ImportManager implements CodeConvertable<readonly Statement[]> {
  public readonly default: DefaultImportManager;
  public readonly named: NamedImportManager;
  public readonly namespace: NamespaceImportManager;

  readonly #collator: Intl.Collator;

  public constructor(collator: Intl.Collator) {
    this.default = new DefaultImportManager(collator);
    this.named = new NamedImportManager(collator);
    this.namespace = new NamespaceImportManager(collator);
    this.#collator = collator;
  }

  public toCode(): readonly Statement[] {
    const records = [
      ...this.default.statementRecords(),
      ...this.named.statementRecords(),
      ...this.namespace.statementRecords(),
    ];
    records.sort(StatementRecordManager.createComparator(this.#collator));

    return records.map(([, statement]) => statement);
  }

  public fromCode(source: ts.SourceFile) {
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
          for (const { name: specifier, isTypeOnly } of (namedBindings as NamedImports).elements) {
            this.named.add(path, specifier.text, isTypeOnly, specifier);
          }
        }
      } else if (name) {
        this.default.add(path, name.text, importClause.isTypeOnly, name);
      }
    }
  }
}
