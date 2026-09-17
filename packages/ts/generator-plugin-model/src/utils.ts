import ts, {
  type ArrowFunction,
  type Expression,
  type Identifier,
  type Node,
  type PropertyDeclaration,
  type SourceFile,
  type Statement,
  type TransformerFactory,
  type VariableStatement,
} from '@typescript/typescript6';
import type Plugin from '@vaadin/hilla-generator-core/Plugin.js';
import { template } from '@vaadin/hilla-generator-utils/ast.js';
import type DependencyManager from '@vaadin/hilla-generator-utils/dependencies/DependencyManager.js';

export type Context = Readonly<{
  owner: Plugin;
}>;

export const defaultMediaType = 'application/json';

function initializerOf<T extends Expression>([statement]: readonly Statement[]): T {
  return (statement as VariableStatement).declarationList.declarations[0].initializer as T;
}

/**
 * Unquotes every object key, at any depth. The shared `transform` helper stops
 * descending into a node it has replaced, which would leave the keys of a
 * nested object quoted while the ones around it are not.
 */
const unquoteKeys: TransformerFactory<SourceFile> = (context) => (root) => {
  function visit(node: Node): Node {
    const visited = ts.visitEachChild(node, visit, context);

    return ts.isPropertyAssignment(visited) && ts.isStringLiteral(visited.name)
      ? ts.factory.createPropertyAssignment(visited.name.text, visited.initializer)
      : visited;
  }

  return ts.visitEachChild(root, visit, context);
};

/**
 * Turns a value read from the OpenAPI document into the expression that
 * produces it. Parsing the JSON rather than building the nodes by hand keeps
 * the cases the factory has no literal for, such as a negative number.
 */
export function createExpressionFromValue(value: unknown): Expression {
  return template(`const a=${JSON.stringify(value)}`, initializerOf, [unquoteKeys]);
}

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
