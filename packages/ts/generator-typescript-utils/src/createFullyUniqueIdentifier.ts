import type { GeneratedIdentifierFlags } from 'typescript';
import ts from 'typescript';

export default function createFullyUniqueIdentifier(name: string, flags?: GeneratedIdentifierFlags) {
  return ts.factory.createUniqueName(
    name,
    flags ?? ts.GeneratedIdentifierFlags.Optimistic & ts.GeneratedIdentifierFlags.FileLevel,
  );
}
