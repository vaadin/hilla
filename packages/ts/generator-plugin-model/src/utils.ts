import type Plugin from '@vaadin/hilla-generator-core/Plugin.js';
import type DependencyManager from '@vaadin/hilla-generator-utils/dependencies/DependencyManager.js';
import ts, { type ArrowFunction, type Expression, type Identifier, type PropertyDeclaration } from 'typescript';

export type Context = Readonly<{
  owner: Plugin;
}>;

export const defaultMediaType = 'application/json';

export function importBuiltInFormModel(specifier: string, { imports, paths }: DependencyManager): Identifier {
  const modelPath = paths.createBareModulePath('@vaadin/hilla-lit-form', false);
  return imports.named.getIdentifier(modelPath, specifier) ?? imports.named.add(modelPath, specifier);
}

export function createModelBuildingCallback(name: Identifier, args: readonly Expression[]): ArrowFunction {
  const defaults = [ts.factory.createIdentifier('parent'), ts.factory.createIdentifier('key')];

  return ts.factory.createArrowFunction(
    undefined,
    undefined,
    defaults.map((arg) => ts.factory.createParameterDeclaration(undefined, undefined, arg)),
    undefined,
    ts.factory.createToken(ts.SyntaxKind.EqualsGreaterThanToken),
    ts.factory.createNewExpression(name, undefined, [...defaults, ...args]),
  );
}

export function createEmptyValueMaker(maker: Identifier, model: Identifier): PropertyDeclaration {
  return ts.factory.createPropertyDeclaration(
    [ts.factory.createModifier(ts.SyntaxKind.StaticKeyword), ts.factory.createModifier(ts.SyntaxKind.OverrideKeyword)],
    'createEmptyValue',
    undefined,
    undefined,
    ts.factory.createCallExpression(maker, undefined, [model]),
  );
}
