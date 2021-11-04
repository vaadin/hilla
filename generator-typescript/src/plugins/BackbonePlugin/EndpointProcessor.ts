import { OpenAPIV3 } from 'openapi-types';
import type { ReadonlyDeep } from 'type-fest';
import type { SourceFile, Statement } from 'typescript';
import ts from 'typescript';
import EndpointMethodOperationProcessor, { EndpointMethodOperation } from './EndpointMethodOperationProcessor.js';
import {
  createSourceBag,
  ExportList,
  ImportList,
  SourceBag,
  StatementBag,
  updateSourceBagMutating,
} from './SourceBag.js';
import { BackbonePluginContext, createSourceFile } from './utils.js';

const collator = new Intl.Collator('en', { sensitivity: 'case' });

export default class EndpointProcessor {
  readonly #bags: StatementBag[] = [];
  readonly #context: BackbonePluginContext;
  readonly #methods = new Map<string, ReadonlyDeep<OpenAPIV3.PathItemObject>>();
  readonly #name: string;
  readonly #libraryIdentifier = ts.factory.createUniqueName('client');

  public constructor(name: string, context: BackbonePluginContext) {
    this.#context = context;
    this.#name = name;
  }

  public add(method: string, pathItem: ReadonlyDeep<OpenAPIV3.PathItemObject>): void {
    this.#methods.set(method, pathItem);
  }

  public process(): SourceFile {
    for (const [method, pathItem] of this.#methods) {
      this.#processMethod(method, pathItem);
    }

    return this.#createSourceFile();
  }

  #createSourceFile(): SourceFile {
    this.#context.logger.info(`Creating TS source file for endpoint: ${this.#name}`);

    const { imports, exports, code } = this.#mergeBags();

    const importStatements = this.#prepareImportStatements(imports);
    const exportStatement = this.#prepareExportStatement(exports);

    return createSourceFile(
      [this.#prepareLibraryImport(), ...importStatements, ...code, ...(exportStatement ? [exportStatement] : [])],
      this.#name,
    );
  }

  #mergeBags(): StatementBag {
    return this.#bags.reduce<SourceBag<Statement>>(
      (acc, { imports, exports, code }) => updateSourceBagMutating(acc, code, imports, exports),
      createSourceBag(),
    );
  }

  #prepareExportStatement(exports: ExportList): Statement | undefined {
    if (!exports) {
      return undefined;
    }

    const exportKeys = Object.keys(exports);
    exportKeys.sort(collator.compare);

    return ts.factory.createExportDeclaration(
      undefined,
      undefined,
      false,
      ts.factory.createNamedExports(
        exportKeys.map((key) =>
          ts.factory.createExportSpecifier(false, exports[key], ts.factory.createIdentifier(key)),
        ),
      ),
      undefined,
    );
  }

  #prepareImportStatements(imports: ImportList): readonly Statement[] {
    if (!imports) {
      return [];
    }

    const importKeys = Object.keys(imports);
    importKeys.sort(collator.compare);

    return importKeys.map((key) => {
      const path = imports[key];

      return ts.factory.createImportDeclaration(
        undefined,
        undefined,
        ts.factory.createImportClause(true, ts.factory.createIdentifier(key), undefined),
        ts.factory.createStringLiteral(path),
      );
    });
  }

  #prepareLibraryImport() {
    return ts.factory.createImportDeclaration(
      undefined,
      undefined,
      ts.factory.createImportClause(false, this.#libraryIdentifier, undefined),
      ts.factory.createStringLiteral('./connect-client.default.js'),
    );
  }

  #processMethod(method: string, pathItem: ReadonlyDeep<OpenAPIV3.PathItemObject>): void {
    this.#context.logger.info(`Start processing endpoint method: ${method}`);

    for (const httpMethod of Object.values(OpenAPIV3.HttpMethods)) {
      if (pathItem[httpMethod]) {
        const bag = new EndpointMethodOperationProcessor(
          httpMethod,
          pathItem[httpMethod] as EndpointMethodOperation,
          { libraryIdentifier: this.#libraryIdentifier },
          this.#context,
        ).process(this.#name, method);

        this.#bags.push(bag);
      }
    }
  }
}
