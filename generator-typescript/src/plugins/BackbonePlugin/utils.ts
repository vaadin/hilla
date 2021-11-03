import type Pino from 'pino';
import type { Statement, TypeNode } from 'typescript';
import type ReferenceResolver from '../../core/ReferenceResolver.js';

export type MutableArray<T extends readonly unknown[]> = Array<T[number]>;

export type BackbonePluginContext = Readonly<{
  logger: Pino.Logger;
  resolver: ReferenceResolver;
}>;

export type SourceBagBase = Readonly<{
  exports?: Readonly<Record<string, string>>;
  imports?: Readonly<Record<string, string>>;
}>;

export type SourceBag<T = unknown> = SourceBagBase &
  Readonly<{
    code: readonly T[];
  }>;

export type TypeNodesBag = SourceBag<TypeNode>;

export type StatementBag = SourceBag<Statement>;

export const defaultMediaType = 'application/json';

export const emptySourceBag: SourceBag = {
  code: [],
};
