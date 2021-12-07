import type { Identifier, ImportDeclaration, Statement } from 'typescript';
import ts from 'typescript';
import type { DependencyRecord } from './utils.js';
import { createDependencyRecord, PathRecord, convertPathRecordsToCode, createPathRecordComparator } from './utils.js';

export class NamedImportManager {
  readonly #collator: Intl.Collator;
  readonly #map = new Map<string, Map<string, DependencyRecord>>();

  public constructor(collator: Intl.Collator) {
    this.#collator = collator;
  }

  public add(path: string, specifier: string, isType?: boolean, uniqueId?: Identifier): Identifier {
    const record = createDependencyRecord(uniqueId ?? ts.factory.createUniqueName(specifier), isType);

    if (this.#map.has(path)) {
      this.#map.get(path)!.set(specifier, record);
    } else {
      this.#map.set(path, new Map([[specifier, record]]));
    }

    return record.id;
  }

  public *codeRecords(): IterableIterator<PathRecord<ImportDeclaration>> {
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

  public toCode(): readonly ImportDeclaration[] {
    return convertPathRecordsToCode(this.codeRecords(), this.#collator);
  }
}

export class NamespaceImportManager {
  readonly #collator: Intl.Collator;
  readonly #map = new Map<string, Identifier>();

  public constructor(collator: Intl.Collator) {
    this.#collator = collator;
  }

  public add(path: string, name: string, uniqueId?: Identifier): Identifier {
    const id = uniqueId ?? ts.factory.createUniqueName(name);
    this.#map.set(path, id);
    return id;
  }

  public *codeRecords(): IterableIterator<PathRecord<ImportDeclaration>> {
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

  public toCode(): readonly ImportDeclaration[] {
    return convertPathRecordsToCode(this.codeRecords(), this.#collator);
  }
}

export class DefaultImportManager {
  readonly #collator: Intl.Collator;
  readonly #map = new Map<string, DependencyRecord>();

  public constructor(collator: Intl.Collator) {
    this.#collator = collator;
  }

  public add(path: string, name: string, isType?: boolean, uniqueId?: Identifier): Identifier {
    const id = uniqueId ?? ts.factory.createUniqueName(name);
    this.#map.set(path, createDependencyRecord(id, isType));
    return id;
  }

  public *codeRecords(): IterableIterator<PathRecord<ImportDeclaration>> {
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

  public getIdentifier(path: string): Identifier | undefined {
    return this.#map.get(path)?.id;
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

  public toCode(): readonly ImportDeclaration[] {
    return convertPathRecordsToCode(this.codeRecords(), this.#collator);
  }
}

export default class ImportManager {
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
    const records = [...this.default.codeRecords(), ...this.named.codeRecords(), ...this.namespace.codeRecords()];
    records.sort(createPathRecordComparator(this.#collator));

    return records.map(([, statement]) => statement);
  }
}
