import type SharedStorage from '@vaadin/hilla-generator-core/SharedStorage.js';
import Plugin from '@vaadin/hilla-generator-core/Plugin.js';
import SignalProcessor, { type MethodInfo } from './SignalProcessor.js';

export type PathSignalType = Readonly<{
  path: string;
  signalType: string;
}>;

function extractEndpointMethodsWithSignalsAsReturnType(storage: SharedStorage): PathSignalType[] {
  const pathSignalTypes: PathSignalType[] = [];
  Object.entries(storage.api.paths).forEach(([path, pathObject]) => {
    const response200 = pathObject?.post?.responses['200'];
    if (response200 && !('$ref' in response200)) {
      // OpenAPIV3.ResponseObject
      const responseSchema = response200.content?.['application/json'].schema;
      if (responseSchema && 'anyOf' in responseSchema) {
        // OpenAPIV3.SchemaObject
        responseSchema.anyOf?.some((c) => {
          const isSignal = '$ref' in c && c.$ref && SignalsPlugin.SIGNAL_CLASSES.includes(c.$ref);
          if (isSignal) {
            pathSignalTypes.push({ path, signalType: c.$ref });
          }
        });
      }
    }
  });
  return pathSignalTypes;
}

function groupByService(signals: PathSignalType[]): Map<string, MethodInfo[]> {
  const serviceMap = new Map<string, MethodInfo[]>();

  signals.forEach((signal) => {
    const [_, service, method] = signal.path.split('/');

    const serviceMethods = serviceMap.get(service) ?? [];

    serviceMethods.push({
      name: method,
      signalType: signal.signalType,
    });

    serviceMap.set(service, serviceMethods);
  });

  return serviceMap;
}

export default class SignalsPlugin extends Plugin {
  static readonly SIGNAL_CLASSES = ['#/components/schemas/com.vaadin.hilla.signals.NumberSignal'];

  override async execute(sharedStorage: SharedStorage): Promise<void> {
    const methodsWithSignals = extractEndpointMethodsWithSignalsAsReturnType(sharedStorage);
    const services = groupByService(methodsWithSignals);
    services.forEach((methods, service) => {
      let index = sharedStorage.sources.findIndex((source) => source.fileName === `${service}.ts`);
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
