import type { Identifier } from '@typescript/typescript6';

export type DependencyRecord = Readonly<{
  id: Identifier;
  isType: boolean;
}>;

export function createDependencyRecord(id: Identifier, isType = false): DependencyRecord {
  return {
    id,
    isType,
  };
}
