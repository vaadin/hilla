import ts, { type Expression, type Identifier } from '@typescript/typescript6';
import type Plugin from '@vaadin/hilla-generator-core/Plugin.js';
import type DependencyManager from '@vaadin/hilla-generator-utils/dependencies/DependencyManager.js';
import type { Cycles } from './cycles.js';

export type Context = Readonly<{
  owner: Plugin;
  cycles: Cycles;
}>;

export const defaultMediaType = 'application/json';

const MODELS_MODULE = '@vaadin/hilla-models';

/**
 * Imports a named export of the model library, such as `StringModel`.
 */
export function importModel(specifier: string, { imports, paths }: DependencyManager): Identifier {
  const path = paths.createBareModulePath(MODELS_MODULE, false);
  return imports.named.getIdentifier(path, specifier) ?? imports.named.add(path, specifier);
}

/**
 * Imports the `m` namespace of the model library, which holds the builders and
 * the converters.
 */
export function importM({ imports, paths }: DependencyManager): Identifier {
  const path = paths.createBareModulePath(MODELS_MODULE, false);
  return imports.default.getIdentifier(path) ?? imports.default.add(path, 'm');
}

/**
 * Builds a call of a converter of the `m` namespace, e.g. `m.optional(model)`.
 */
export function createModelCall(
  dependencies: DependencyManager,
  name: string,
  args: readonly Expression[],
): ts.CallExpression {
  return ts.factory.createCallExpression(
    ts.factory.createPropertyAccessExpression(importM(dependencies), name),
    undefined,
    args,
  );
}

/**
 * Builds `() => Model`, which defers reading the model until the property is
 * accessed and so survives a circular import.
 */
export function createModelProvider(model: Expression): ts.ArrowFunction {
  return ts.factory.createArrowFunction(
    undefined,
    undefined,
    [],
    undefined,
    ts.factory.createToken(ts.SyntaxKind.EqualsGreaterThanToken),
    model,
  );
}
