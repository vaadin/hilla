import Plugin from '@vaadin/hilla-generator-core/Plugin.js';
import type { SharedStorage } from '@vaadin/hilla-generator-core/SharedStorage.js';
import type { OpenAPIV3 } from 'openapi-types';
import SignalProcessor from './SignalProcessor.js';
import { SIGNALS } from './utils.js';

// Polyfill for iterator helpers (Stage 3 proposal)
if (!('Iterator' in globalThis)) {
  const { installIntoGlobal } = await import('iterator-helpers-polyfill');
  installIntoGlobal();
}

const SIGNAL_CLASSES = SIGNALS.map((signal) => `#/components/schemas/com.vaadin.hilla.runtime.transfertypes.${signal}`);

function mapPathToSignalType(storage: SharedStorage): Map<string, string> {
  return new Map(
    Object.entries(storage.api.paths)
      .filter(([_, pathObject]) => {
        const response200 = pathObject?.post?.responses['200'];
        return response200 && !('$ref' in response200);
      })
      .flatMap(([path, pathObject]) => {
        const response200 = pathObject?.post?.responses['200'];
        const responseSchema = (response200 as OpenAPIV3.ResponseObject).content?.['application/json']?.schema;

        return responseSchema && 'anyOf' in responseSchema
          ? responseSchema.anyOf
              ?.filter(
                (c): c is OpenAPIV3.ReferenceObject => '$ref' in c && !!c.$ref && SIGNAL_CLASSES.includes(c.$ref),
              )
              .map((c) => [path, c.$ref] as const)
          : null;
      })
      .filter((signalArray) => signalArray != null),
  );
}

function groupByService(signalMap: Map<string, string>): Map<string, Map<string, string>> {
  return signalMap.entries().reduce((serviceMap, [path, type]) => {
    const [_, service, method] = path.split('/');
    const serviceMethods = serviceMap.get(service) ?? new Map<string, string>();
    serviceMethods.set(method, type);
    serviceMap.set(service, serviceMethods);
    return serviceMap;
  }, new Map<string, Map<string, string>>());
}

export default class SignalsPlugin extends Plugin {
  override execute(sharedStorage: SharedStorage): void {
    const methodsWithSignals = mapPathToSignalType(sharedStorage);
    const services = groupByService(methodsWithSignals);
    services.forEach((methods, service) => {
      const index = sharedStorage.sources.findIndex((source) => source.fileName === `${service}.ts`);
      if (index >= 0) {
        sharedStorage.sources[index] = new SignalProcessor(
          service,
          methods,
          sharedStorage.sources[index],
          this,
        ).process();
      }
    });
  }

  declare ['constructor']: typeof SignalsPlugin;

  override get path(): string {
    return import.meta.url;
  }
}
