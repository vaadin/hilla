import type SharedStorage from "@vaadin/hilla-generator-core/SharedStorage.js";
import Plugin from "@vaadin/hilla-generator-core/Plugin.js";
import process from "./SharedSignalProcessor";

export type PathSignalType = {
  path: string;
  signalType: string;
}

export default class SignalsPlugin extends Plugin {
  static readonly SIGNAL_CLASSES = [
    '#/components/schemas/com.vaadin.hilla.signals.NumberSignal'
  ]

  #extractEndpointMethodsWithSignalsAsReturnType(storage: SharedStorage): PathSignalType[] {
    const pathSignalTypes: PathSignalType[] = [];
    Object.entries(storage.api.paths).forEach(([path, pathObject]) => {
      const response200 = pathObject?.post?.responses['200'];
      if (response200 && !("$ref" in response200)) { // OpenAPIV3.ResponseObject
        const responseSchema = response200.content?.['application/json'].schema;
        if (responseSchema && ("anyOf" in responseSchema)) { // OpenAPIV3.SchemaObject
          responseSchema.anyOf?.some((c) => {
            const isSignal = ("$ref" in c) && c.$ref && SignalsPlugin.SIGNAL_CLASSES.includes(c.$ref);
            if (isSignal) {
              pathSignalTypes.push({ path, signalType: c.$ref });
            }
          });
        }
      }
    });
    return pathSignalTypes;
  }

  override async execute(sharedStorage: SharedStorage): Promise<void> {
    const methodsWithSignals = this.#extractEndpointMethodsWithSignalsAsReturnType(sharedStorage);
    process(methodsWithSignals, sharedStorage);
  }

  declare ['constructor']: typeof SignalsPlugin;

  override get path(): string {
    return import.meta.url;
  }
}
