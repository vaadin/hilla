import ts, {
  type Expression,
  type Identifier,
  type Node,
  type SourceFile,
  type Statement,
  type TransformerFactory,
  type VariableStatement,
} from '@typescript/typescript6';
import type Plugin from '@vaadin/hilla-generator-core/Plugin.js';
import { template } from '@vaadin/hilla-generator-utils/ast.js';
import type DependencyManager from '@vaadin/hilla-generator-utils/dependencies/DependencyManager.js';
import type { Cycles } from './cycles.js';

export type Context = Readonly<{
  owner: Plugin;
  cycles: Cycles;
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
