import Plugin from '@hilla/generator-typescript-core/Plugin.js';
import type SharedStorage from '@hilla/generator-typescript-core/SharedStorage.js';
import type { OpenAPIV3 } from 'openapi-types';
import type { ReadonlyObjectDeep } from 'type-fest/source/readonly-deep';
import PushProcessor from './PushProcessor.js';

type ExtendedMediaTypeSchema = ReadonlyObjectDeep<OpenAPIV3.ReferenceObject | OpenAPIV3.SchemaObject> &
  Readonly<{ 'x-class-name': string }>;

const classesToReplace: readonly string[] = [
  'dev.hilla.runtime.transfertypes.Flux',
  'dev.hilla.runtime.transfertypes.EndpointSubscription',
];

export default class PushPlugin extends Plugin {
  /**
   * Collects methods that must be patched by checking their `x-class-name` value
   */
  static #collectPatchableMethods(paths: ReadonlyObjectDeep<OpenAPIV3.PathsObject>) {
    return Object.entries(paths).reduce((acc, [key, path]) => {
      const response = path?.post?.responses[200] as ReadonlyObjectDeep<OpenAPIV3.ResponseObject> | undefined;
      const schema = response?.content?.['application/json']?.schema as ExtendedMediaTypeSchema | undefined;
      const className = schema?.['x-class-name'];

      if (className && classesToReplace.includes(className)) {
        const [, endpoint, method] = key.split('/');

        if (acc.has(endpoint)) {
          acc.get(endpoint)!.push(method);
        } else {
          acc.set(endpoint, [method]);
        }
      }

      return acc;
    }, new Map<string, string[]>());
  }

  declare ['constructor']: typeof PushPlugin;

  override get path(): string {
    return import.meta.url;
  }

  public override async execute(storage: SharedStorage): Promise<void> {
    const { api, sources } = storage;
    const endpointMethodMap = this.constructor.#collectPatchableMethods(api.paths);

    for (let i = 0; i < sources.length; i++) {
      const { fileName } = sources[i];
      const endpoint = fileName.substring(0, fileName.indexOf('.ts'));

      if (endpointMethodMap.has(endpoint)) {
        sources[i] = new PushProcessor(sources[i], endpointMethodMap.get(endpoint)!).process();
      }
    }
  }
}
