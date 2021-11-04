import type { Mutable } from 'type-fest';
import type { Statement, TypeNode } from 'typescript';

export type SourceBagBase = Readonly<{
  exports: Readonly<Record<string, string>>;
  imports: Readonly<Record<string, string>>;
}>;

export type SourceBag<T = unknown> = SourceBagBase &
  Readonly<{
    code: readonly T[];
  }>;

export type TypeNodesBag = SourceBag<TypeNode>;
export type StatementBag = SourceBag<Statement>;

export function createSourceBag<T = unknown>(
  code: readonly T[] = [],
  imports: Readonly<Record<string, string>> = {},
  exports: Readonly<Record<string, string>> = {},
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
  imports?: Readonly<Record<string, string>>,
  exports?: Readonly<Record<string, string>>,
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
