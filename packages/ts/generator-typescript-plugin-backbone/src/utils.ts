import type Pino from 'pino';
import type ReferenceResolver from '@vaadin/generator-typescript-core/ReferenceResolver.js';

export type BackbonePluginContext = Readonly<{
  logger: Pino.Logger;
  resolver: ReferenceResolver;
}>;

export const defaultMediaType = 'application/json';
