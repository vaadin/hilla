import type SwaggerParser from '@apidevtools/swagger-parser';
import type { OpenAPIV3 } from 'openapi-types';
import type { ReadonlyDeep } from 'type-fest';

export default class ReferenceResolver {
  readonly #parser: SwaggerParser;

  public constructor(parser: SwaggerParser) {
    this.#parser = parser;
  }

  // eslint-disable-next-line @typescript-eslint/ban-types
  public resolve<T extends ReadonlyDeep<object>>(obj: T | ReadonlyDeep<OpenAPIV3.ReferenceObject>): T {
    return '$ref' in obj ? this.#parser.$refs.get(obj.$ref) : obj;
  }
}
