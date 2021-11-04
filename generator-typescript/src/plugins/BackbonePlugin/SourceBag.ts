import type { Mutable } from 'type-fest';
import type { Identifier, Statement, TypeNode } from 'typescript';

export type ExportList = Readonly<Record<string, Identifier>>;
export type ImportList = Readonly<Record<string, string>>;

export type SourceBagBase = Readonly<{
  exports: ExportList;
  imports: ImportList;
}>;

export type SourceBag<T = unknown> = SourceBagBase &
  Readonly<{
    code: readonly T[];
  }>;

export type TypeNodesBag = SourceBag<TypeNode>;
export type StatementBag = SourceBag<Statement>;

export function createSourceBag<T = unknown>(
  code: readonly T[] = [],
  imports: ImportList = {},
  exports: ExportList = {},
): SourceBag<T> {
  return {
    code,
    exports,
    imports,
  };
}

export function updateSourceBagMutating<T = unknown>(
  bag: Mutable<SourceBag<T>>,
  code?: readonly T[],
  imports?: ImportList,
  exports?: ExportList,
): SourceBag<T> {
  if (code) {
    (bag.code as Array<T>).push(...code);
  }

  if (imports) {
    Object.assign(bag.imports, imports);
  }

  if (exports) {
    Object.assign(bag.exports, exports);
  }

  return bag;
}
