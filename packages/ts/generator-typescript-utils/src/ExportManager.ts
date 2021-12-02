import ts, { ExportAssignment, ExportDeclaration, Identifier, Statement } from 'typescript';
import type PathProcessor from './PathProcessor.js';
import type { DependencyRecord, PathRecord } from './utils.js';
import { createDependencyRecord, createPathRecordComparator } from './utils.js';

export class NamedExportManager {
  readonly #collator: Intl.Collator;
  readonly #map = new Map<string, DependencyRecord>();

  public constructor(collator: Intl.Collator) {
    this.#collator = collator;
  }

  public add(name: string, isType?: boolean, uniqueId?: Identifier): Identifier {
    const id = uniqueId ?? ts.factory.createUniqueName(name);
    this.#map.set(name, createDependencyRecord(id, isType));
    return id;
  }

  public getIdentifier(name: string): Identifier | undefined {
    return this.#map.get(name)?.id;
  }

  public *identifiers(): IterableIterator<readonly [id: Identifier, isType: boolean]> {
    for (const { id, isType } of this.#map.values()) {
      yield [id, isType];
    }
  }

  public isType(name: string): boolean | undefined {
    return this.#map.get(name)?.isType;
  }

  public names(): IterableIterator<string> {
    return this.#map.keys();
  }

  public toCode(): ExportDeclaration | undefined {
    if (this.#map.size === 0) {
      return undefined;
    }

    const names = [...this.#map.keys()];
    names.sort(this.#collator.compare);

    return ts.factory.createExportDeclaration(
      undefined,
      undefined,
      false,
      ts.factory.createNamedExports(
        names.map((name) => {
          const { id, isType } = this.#map.get(name)!;
          return ts.factory.createExportSpecifier(isType, id, ts.factory.createIdentifier(name));
        }),
      ),
      undefined,
    );
  }
}

export class NamespaceExportManager {
  readonly #collator: Intl.Collator;
  readonly #map = new Map<string, Identifier | null>();
  readonly #path: PathProcessor;

  public constructor(path: PathProcessor, collator: Intl.Collator) {
    this.#collator = collator;
    this.#path = path;
  }

  public addCombined(path: string, name: string, uniqueId?: Identifier): Identifier {
    const id = uniqueId ?? ts.factory.createUniqueName(name);
    this.#map.set(this.#path.process(path), id);
    return id;
  }

  public addSpread(path: string) {
    this.#map.set(this.#path.process(path), null);
  }

  public *codeRecords(): IterableIterator<PathRecord<ExportDeclaration>> {
    for (const [path, id] of this.#map) {
      yield [
        path,
        ts.factory.createExportDeclaration(
          undefined,
          undefined,
          false,
          id !== null ? ts.factory.createNamespaceExport(id) : undefined,
          ts.factory.createStringLiteral(path),
        ),
      ];
    }
  }

  public getIdentifier(path: string): Identifier | null | undefined {
    return this.#map.get(this.#path.process(path));
  }

  public identifiers(): IterableIterator<Identifier | null> {
    return this.#map.values();
  }

  public isCombined(path: string): boolean | undefined {
    const processedPath = this.#path.process(path);
    return this.#map.has(processedPath) ? this.#map.get(processedPath) !== null : undefined;
  }

  public isSpread(path: string): boolean | undefined {
    const processedPath = this.#path.process(path);
    return this.#map.has(processedPath) ? this.#map.get(processedPath) === null : undefined;
  }

  public paths(): IterableIterator<string> {
    return this.#map.keys();
  }

  public toCode(): readonly ExportDeclaration[] {
    const records = [...this.codeRecords()];
    records.sort(createPathRecordComparator(this.#collator));

    return records.map(([, statement]) => statement);
  }
}

export class DefaultExportManager {
  #id?: Identifier;

  public set(id: Identifier) {
    this.#id = id;
  }

  public toCode(): ExportAssignment | undefined {
    return this.#id ? ts.factory.createExportAssignment(undefined, undefined, undefined, this.#id) : undefined;
  }
}

export default class ExportManager {
  public readonly default = new DefaultExportManager();
  public readonly named: NamedExportManager;
  public readonly namespace: NamespaceExportManager;

  public constructor(path: PathProcessor, collator: Intl.Collator) {
    this.named = new NamedExportManager(collator);
    this.namespace = new NamespaceExportManager(path, collator);
  }

  public toCode(): readonly Statement[] {
    const defaultStatement = this.default.toCode();
    const namedStatement = this.named.toCode();
    const namespaceStatements = this.namespace.toCode();

    const result: Statement[] = [];

    if (namedStatement) {
      result.push(namedStatement);
    }

    if (namespaceStatements) {
      result.push(...namespaceStatements);
    }

    if (defaultStatement) {
      result.push(defaultStatement);
    }

    return result;
  }
}
