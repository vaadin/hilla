import type Plugin from '@vaadin/hilla-generator-core/Plugin.js';
import ClientPlugin from '@vaadin/hilla-generator-plugin-client';
import createSourceFile from '@vaadin/hilla-generator-utils/createSourceFile.js';
import DependencyManager from '@vaadin/hilla-generator-utils/dependencies/DependencyManager.js';
import PathManager from '@vaadin/hilla-generator-utils/dependencies/PathManager.js';
import { OpenAPIV3 } from 'openapi-types';
import type { ReadonlyDeep } from 'type-fest';
import type { SourceFile, Statement } from 'typescript';
import EndpointMethodOperationProcessor, {
  HILLA_FRONTEND_NAME,
  INIT_TYPE_NAME,
} from './EndpointMethodOperationProcessor.js';

export default class EndpointProcessor {
  static async create(
    name: string,
    owner: Plugin,
    methods: Map<string, ReadonlyDeep<OpenAPIV3.PathItemObject>>,
    outputDir?: string,
  ): Promise<EndpointProcessor> {
    const endpoint = new EndpointProcessor(name, owner, methods, outputDir);
    endpoint.#dependencies.imports.default.add(
      endpoint.#dependencies.paths.createRelativePath(await ClientPlugin.getClientFileName(outputDir)),
      'client',
    );
    endpoint.#dependencies.imports.named.add(
      endpoint.#dependencies.paths.createBareModulePath(HILLA_FRONTEND_NAME),
      INIT_TYPE_NAME,
    );
    return endpoint;
  }

  readonly #createdFilePaths = new PathManager({ extension: 'ts' });
  readonly #dependencies = new DependencyManager(new PathManager({ extension: '.js' }));
  readonly #methods: Map<string, ReadonlyDeep<OpenAPIV3.PathItemObject>>;
  readonly #name: string;
  readonly #outputDir: string | undefined;
  readonly #owner: Plugin;

  private constructor(
    name: string,
    owner: Plugin,
    methods: Map<string, ReadonlyDeep<OpenAPIV3.PathItemObject>>,
    outputDir?: string,
  ) {
    this.#name = name;
    this.#owner = owner;
    this.#methods = methods;
    this.#outputDir = outputDir;
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

  async #processMethod(
    method: string,
    pathItem: ReadonlyDeep<OpenAPIV3.PathItemObject>,
  ): Promise<readonly Statement[]> {
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
              this.#owner,
            )?.process(this.#outputDir),
          ),
      )
    ).filter(Boolean) as readonly Statement[];
  }
}
