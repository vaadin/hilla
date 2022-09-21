import type Plugin from '@hilla/generator-typescript-core/Plugin.js';
import type DependencyManager from '@hilla/generator-typescript-utils/dependencies/DependencyManager.js';
import type { Identifier } from 'typescript';

export type Context = Readonly<{
  owner: Plugin;
}>;

export const defaultMediaType = 'application/json';

export function importBuiltInFormModel(specifier: string, { imports, paths }: DependencyManager): Identifier {
  const modelPath = paths.createBareModulePath('@hilla/form', false);
  return imports.named.getIdentifier(modelPath, specifier) ?? imports.named.add(modelPath, specifier);
}
