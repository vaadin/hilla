import type Pino from 'pino';
import ReferenceResolver from '../../core/ReferenceResolver';

export type BackbonePluginContext = Readonly<{
  imports: Map<string, string>;
  logger: Pino.Logger;
  resolver: ReferenceResolver;
}>;

export type EndpointMethodData = Readonly<{
  endpoint: string;
  method: string;
}>;
