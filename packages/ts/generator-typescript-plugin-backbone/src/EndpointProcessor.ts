import { OpenAPIV3 } from 'openapi-types';
import type { ReadonlyDeep } from 'type-fest';
import type { SourceFile, Statement } from 'typescript';
import DependencyManager from './DependencyManager.js';
import EndpointMethodOperationProcessor, { EndpointMethodOperation } from './EndpointMethodOperationProcessor.js';
import type { BackbonePluginContext } from './utils.js';
import { clientLib, createSourceFile } from './utils.js';

export default class EndpointProcessor {
  readonly #context: BackbonePluginContext;
  readonly #dependencies = new DependencyManager();
  readonly #methods = new Map<string, ReadonlyDeep<OpenAPIV3.PathItemObject>>();
  readonly #name: string;

  public constructor(name: string, context: BackbonePluginContext) {
    this.#context = context;
    this.#name = name;
    this.#dependencies.imports.register(clientLib.specifier, clientLib.path);
  }

  public add(method: string, pathItem: ReadonlyDeep<OpenAPIV3.PathItemObject>): void {
    this.#methods.set(method, pathItem);
  }

  public process(): SourceFile {
    this.#context.logger.info(`Processing endpoint: ${this.#name}`);

    const statements = Array.from(this.#methods, ([method, pathItem]) => this.#processMethod(method, pathItem)).flatMap(
      (item) => item,
    );

    const { imports, exports } = this.#dependencies;

    const importStatements = imports.toTS();
    const exportStatement = exports.toTS();

    return createSourceFile(
      [...importStatements, ...statements, exportStatement].filter(Boolean) as readonly Statement[],
      this.#name,
    );
  }

  #processMethod(method: string, pathItem: ReadonlyDeep<OpenAPIV3.PathItemObject>): readonly Statement[] {
    this.#context.logger.info(`Processing endpoint method: ${method}`);

    return Object.values(OpenAPIV3.HttpMethods)
      .filter((httpMethod) => pathItem[httpMethod])
      .map((httpMethod) =>
        new EndpointMethodOperationProcessor(
          httpMethod,
          pathItem[httpMethod] as EndpointMethodOperation,
          this.#dependencies,
          this.#context,
        ).process(this.#name, method),
      )
      .filter(Boolean) as readonly Statement[];
  }
}
