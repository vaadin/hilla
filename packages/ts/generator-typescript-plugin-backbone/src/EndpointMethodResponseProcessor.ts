import type Plugin from '@hilla/generator-typescript-core/Plugin.js';
import type DependencyManager from '@hilla/generator-typescript-utils/dependencies/DependencyManager.js';
import type { OpenAPIV3 } from 'openapi-types';
import type { ReadonlyDeep } from 'type-fest';
import type { TypeNode } from 'typescript';
import TypeSchemaProcessor from './TypeSchemaProcessor.js';
import { defaultMediaType } from './utils.js';

export type EndpointMethodResponses = ReadonlyDeep<OpenAPIV3.ResponsesObject>;
export type EndpointMethodResponse = ReadonlyDeep<OpenAPIV3.ResponseObject>;

export default class EndpointMethodResponseProcessor {
  readonly #code: string;
  readonly #dependencies: DependencyManager;
  readonly #owner: Plugin;
  readonly #response: EndpointMethodResponse;

  constructor(
    code: string,
    response: EndpointMethodResponses[string],
    dependencyManager: DependencyManager,
    owner: Plugin,
  ) {
    this.#code = code;
    this.#owner = owner;
    this.#dependencies = dependencyManager;
    this.#response = owner.resolver.resolve(response);
  }

  process(): readonly TypeNode[] {
    switch (this.#code) {
      case '200':
        return this.#processOk();
      default:
        this.#owner.logger.warn(`Response code '${this.#code} is not supported'`);
        return [];
    }
  }

  #processOk(): readonly TypeNode[] {
    const rawSchema = this.#response.content?.[defaultMediaType]?.schema;

    return rawSchema ? new TypeSchemaProcessor(rawSchema, this.#dependencies).process() : [];
  }
}
