import type { GeneratedIdentifierFlags, Identifier } from 'typescript';
import ts from 'typescript';

export default function createFullyUniqueIdentifier(name: string, flags?: GeneratedIdentifierFlags): Identifier {
  return ts.factory.createUniqueName(
    name,
    // eslint-disable-next-line no-bitwise
    flags ?? ts.GeneratedIdentifierFlags.Optimistic & ts.GeneratedIdentifierFlags.FileLevel,
  );
}
