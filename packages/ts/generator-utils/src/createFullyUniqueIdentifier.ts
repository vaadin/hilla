import ts, { type GeneratedIdentifierFlags, type Identifier } from '@typescript/typescript6';

export default function createFullyUniqueIdentifier(name: string, flags?: GeneratedIdentifierFlags): Identifier {
  return ts.factory.createUniqueName(
    name,
    // eslint-disable-next-line no-bitwise
    flags ?? ts.GeneratedIdentifierFlags.Optimistic & ts.GeneratedIdentifierFlags.FileLevel,
  );
}
