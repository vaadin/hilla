import type { Statement } from 'typescript';
import type SharedStorage from '../../core/SharedStorage';

export type EndpointMethod = Readonly<{
  endpoint: string;
  name: string;
}>;

export type EndpointMethodContext = Readonly<{
  endpoint: string;
  endpointMethod: string;
  source: Statement[];
  storage: SharedStorage;
}>;
