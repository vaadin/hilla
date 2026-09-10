import ts, { type SourceFile, type Statement } from '@typescript/typescript6';
import type Plugin from '@vaadin/hilla-generator-core/Plugin.js';
import type { SharedStorage, TransferTypes } from '@vaadin/hilla-generator-core/SharedStorage.js';
import ClientPlugin from '@vaadin/hilla-generator-plugin-client';
import createSourceFile from '@vaadin/hilla-generator-utils/createSourceFile.js';
import DependencyManager from '@vaadin/hilla-generator-utils/dependencies/DependencyManager.js';
import PathManager from '@vaadin/hilla-generator-utils/dependencies/PathManager.js';
import { OpenAPIV3 } from 'openapi-types';
import EndpointMethodOperationProcessor from './EndpointMethodOperationProcessor.js';
import { defaultMediaType } from './utils.js';

export default class EndpointProcessor {
  static async create(
    name: string,
    methods: Map<string, OpenAPIV3.PathItemObject>,
    storage: SharedStorage,
    owner: Plugin,
  ): Promise<EndpointProcessor> {
    const endpoint = new EndpointProcessor(name, methods, storage, owner);
    const { exports, imports, names, paths } = endpoint.#dependencies;

    // Claim declared names and parameters before imports. This ensures that
    // imports are suffixed on collisions and that method identifiers are
    // preserved as-is.
    for (const [method, pathItem] of methods) {
      exports.named.add(method, false, ts.factory.createIdentifier(method));

      // only the names are of interest here, so the schemas are left alone
      const { requestBody } = pathItem[OpenAPIV3.HttpMethods.POST]!;
      const schema = requestBody ? owner.resolver.resolve(requestBody).content[defaultMediaType].schema : undefined;
      const properties = schema ? owner.resolver.resolve(schema).properties : undefined;

      for (const parameter of Object.keys(properties ?? {})) {
        names.claim(parameter);
      }
    }

    imports.default.add(paths.createRelativePath(await ClientPlugin.getClientFileName(storage.outputDir)), 'client');

    return endpoint;
  }

  readonly #createdFilePaths = new PathManager({ extension: 'ts' });
  readonly #dependencies = new DependencyManager(new PathManager({ extension: '.js' }));
  readonly #methods: Map<string, OpenAPIV3.PathItemObject>;
  readonly #name: string;
  readonly #outputDir: string | undefined;
  readonly #transferTypes: TransferTypes;
  readonly #owner: Plugin;

  private constructor(
    name: string,
    methods: Map<string, OpenAPIV3.PathItemObject>,
    storage: SharedStorage,
    owner: Plugin,
  ) {
    this.#name = name;
    this.#owner = owner;
    this.#methods = methods;
    this.#outputDir = storage.outputDir;
    this.#transferTypes = storage.transferTypes;
  }

  async process(): Promise<SourceFile> {
    this.#owner.logger.debug(`Processing endpoint: ${this.#name}`);

    const statements = (
      await Promise.all(Array.from(this.#methods, async ([method, pathItem]) => this.#processMethod(method, pathItem)))
    ).flatMap((item) => item);

    const { exports, imports } = this.#dependencies;

    return createSourceFile(
      [...imports.toCode(), ...statements, ...exports.toCode()],
      this.#createdFilePaths.createRelativePath(this.#name),
    );
  }

  async #processMethod(method: string, pathItem: OpenAPIV3.PathItemObject): Promise<readonly Statement[]> {
    this.#owner.logger.debug(`Processing endpoint method: ${this.#name}.${method}`);

    return (
      await Promise.all(
        Object.values(OpenAPIV3.HttpMethods)
          .filter((httpMethod) => pathItem[httpMethod])
          .map(async (httpMethod) =>
            EndpointMethodOperationProcessor.createProcessor(
              httpMethod,
              this.#name,
              method,
              pathItem[httpMethod]!,
              this.#dependencies,
              this.#transferTypes,
              this.#owner,
            )?.process(this.#outputDir),
          ),
      )
    ).filter(Boolean) as readonly Statement[];
  }
}
