import type SwaggerParser from '@apidevtools/swagger-parser';
import type { OpenAPIV3 } from 'openapi-types';
import type { ReadonlyDeep } from 'type-fest';

export default class ReferenceResolver {
  readonly #parser: SwaggerParser;

  constructor(parser: SwaggerParser) {
    this.#parser = parser;
  }

  // eslint-disable-next-line @typescript-eslint/ban-types
  resolve<T extends ReadonlyDeep<object>>(obj: ReadonlyDeep<OpenAPIV3.ReferenceObject> | T): T {
    return '$ref' in obj ? this.#parser.$refs.get(obj.$ref) : obj;
  }
}
