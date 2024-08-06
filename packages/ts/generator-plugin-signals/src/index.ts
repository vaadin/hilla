import Plugin from '@vaadin/hilla-generator-core/Plugin.js';
import type SharedStorage from '@vaadin/hilla-generator-core/SharedStorage.js';
import type { OpenAPIV3 } from 'openapi-types';
import SignalProcessor, { type MethodInfo } from './SignalProcessor.js';

export type PathSignalType = Readonly<{
  path: string;
  signalType: string;
}>;

const SIGNAL_CLASSES = ['#/components/schemas/com.vaadin.hilla.signals.NumberSignal'];

function extractEndpointMethodsWithSignalsAsReturnType(storage: SharedStorage): PathSignalType[] {
  return Object.entries(storage.api.paths)
    .filter(([_, pathObject]) => {
      const response200 = pathObject?.post?.responses['200'];
      return response200 && !('$ref' in response200);
    })
    .map(([path, pathObject]) => {
      const response200 = pathObject?.post?.responses['200'];
      const responseSchema = (response200 as OpenAPIV3.ResponseObject).content?.['application/json']?.schema;

      return responseSchema && 'anyOf' in responseSchema
        ? responseSchema.anyOf
            ?.filter((c) => '$ref' in c && c.$ref && SIGNAL_CLASSES.includes(c.$ref))
            .map((c: OpenAPIV3.ReferenceObject | OpenAPIV3.SchemaObject) => ({
              path,
              signalType: '$ref' in c ? c.$ref : '',
            }))
        : [];
    })
    .filter((signalArray) => signalArray !== undefined)
    .reduce<PathSignalType[]>((acc, current) => acc.concat(current), []);
}

function groupByService(signals: readonly PathSignalType[]): Map<string, MethodInfo[]> {
  return signals.reduce((serviceMap, signal) => {
    const [_, service, method] = signal.path.split('/');
    const serviceMethods = serviceMap.get(service) ?? [];
    serviceMethods.push({
      name: method,
      signalType: signal.signalType,
    });
    serviceMap.set(service, serviceMethods);
    return serviceMap;
  }, new Map<string, MethodInfo[]>());
}

export default class SignalsPlugin extends Plugin {
  // eslint-disable-next-line @typescript-eslint/require-await
  override async execute(sharedStorage: SharedStorage): Promise<void> {
    const methodsWithSignals = extractEndpointMethodsWithSignalsAsReturnType(sharedStorage);
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
