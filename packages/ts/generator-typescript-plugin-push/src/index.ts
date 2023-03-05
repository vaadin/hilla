import Plugin from '@hilla/generator-typescript-core/Plugin.js';
import type SharedStorage from '@hilla/generator-typescript-core/SharedStorage.js';
import type { OpenAPIV3 } from 'openapi-types';
import type { ReadonlyDeep } from 'type-fest';
import PushProcessor from './PushProcessor.js';

type ExtendedMediaTypeSchema = Readonly<{ 'x-class-name': string }> &
  ReadonlyDeep<OpenAPIV3.ReferenceObject | OpenAPIV3.SchemaObject>;

const classesToReplace: readonly string[] = [
  'dev.hilla.runtime.transfertypes.Flux',
  'dev.hilla.runtime.transfertypes.EndpointSubscription',
];

/**
 * Collects methods that must be patched by checking their `x-class-name` value
 */
function collectPatchableMethods(paths: ReadonlyDeep<OpenAPIV3.PathsObject>) {
  return Object.entries(paths).reduce((acc, [key, path]) => {
    const response = path?.post?.responses[200] as ReadonlyDeep<OpenAPIV3.ResponseObject> | undefined;
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

export default class PushPlugin extends Plugin {
  declare ['constructor']: typeof PushPlugin;

  // eslint-disable-next-line class-methods-use-this
  override get path(): string {
    return import.meta.url;
  }

  // eslint-disable-next-line class-methods-use-this
  override async execute(storage: SharedStorage): Promise<void> {
    const { api, sources } = storage;
    const endpointMethodMap = collectPatchableMethods(api.paths);

    for (let i = 0; i < sources.length; i++) {
      const { fileName } = sources[i];
      const endpoint = fileName.substring(0, fileName.indexOf('.ts'));

      if (endpointMethodMap.has(endpoint)) {
        sources[i] = new PushProcessor(sources[i], endpointMethodMap.get(endpoint)!).process();
      }
    }
  }
}
