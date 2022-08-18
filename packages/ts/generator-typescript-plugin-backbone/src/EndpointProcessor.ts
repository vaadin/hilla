import type Plugin from '@hilla/generator-typescript-core/Plugin.js';
import ClientPlugin from '@hilla/generator-typescript-plugin-client';
import createSourceFile from '@hilla/generator-typescript-utils/createSourceFile.js';
import DependencyManager from '@hilla/generator-typescript-utils/dependencies/DependencyManager.js';
import PathManager from '@hilla/generator-typescript-utils/dependencies/PathManager.js';
import { OpenAPIV3 } from 'openapi-types';
import type { ReadonlyDeep } from 'type-fest';
import type { SourceFile, Statement } from 'typescript';
import EndpointMethodOperationProcessor, {
  EndpointMethodOperation,
  HILLA_FRONTEND_NAME,
  INIT_TYPE_NAME,
} from './EndpointMethodOperationProcessor.js';

export default class EndpointProcessor {
  readonly #dependencies = new DependencyManager(new PathManager());
  readonly #methods: Map<string, ReadonlyDeep<OpenAPIV3.PathItemObject>>;
  readonly #name: string;
  readonly #owner: Plugin;
  readonly #sourcePaths = new PathManager({ extension: 'ts' });
  readonly #outputDir: string | undefined;

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

  public static async create(
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

  public async process(): Promise<SourceFile> {
    this.#owner.logger.debug(`Processing endpoint: ${this.#name}`);

    const statements = (
      await Promise.all(Array.from(this.#methods, async ([method, pathItem]) => this.#processMethod(method, pathItem)))
    ).flatMap((item) => item);

    const { imports, exports } = this.#dependencies;

    return createSourceFile(
      [...imports.toCode(), ...statements, ...exports.toCode()],
      this.#sourcePaths.createRelativePath(this.#name),
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
          .map((httpMethod) =>
            EndpointMethodOperationProcessor.createProcessor(
              httpMethod,
              this.#name,
              method,
              pathItem[httpMethod] as EndpointMethodOperation,
              this.#dependencies,
              this.#owner,
            )?.process(this.#outputDir),
          ),
      )
    ).filter(Boolean) as readonly Statement[];
  }
}
