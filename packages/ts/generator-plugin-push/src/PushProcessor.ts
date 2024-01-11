import createSourceFile from '@vaadin/hilla-generator-utils/createSourceFile.js';
import DependencyManager from '@vaadin/hilla-generator-utils/dependencies/DependencyManager.js';
import PathManager from '@vaadin/hilla-generator-utils/dependencies/PathManager.js';
import memoize from '@vaadin/hilla-generator-utils/memoize.js';
import ts from 'typescript';

const initParameterTypeName = 'EndpointRequestInit';

export type EndpointOperations = {
  methodsToPatch: string[];
  removeInitImport: boolean;
};

export class PushProcessor {
  readonly #dependencies = new DependencyManager(new PathManager({ extension: '.js' }));
  readonly #operations: EndpointOperations;
  readonly #source: ts.SourceFile;
  readonly #subscriptionId: () => ts.Identifier;

  constructor(source: ts.SourceFile, operations: EndpointOperations) {
    this.#operations = operations;
    this.#source = source;

    const { imports, paths } = this.#dependencies;

    this.#dependencies.imports.fromCode(source);
    this.#subscriptionId = memoize(() =>
      imports.named.add(paths.createBareModulePath('@vaadin/hilla-core', false), 'Subscription'),
    );
  }

  process(): ts.SourceFile {
    const otherStatements = this.#source.statements
      .filter((statement) => !ts.isImportDeclaration(statement))
      .map((statement) => {
        if (ts.isFunctionDeclaration(statement)) {
          const functionName = statement.name?.text;

          // Checks if the method is in the list of methods to patch
          if (functionName && this.#operations.methodsToPatch.includes(functionName)) {
            return this.#updateFunction(statement);
          }
        }

        return statement;
      });

    let importStatements = this.#dependencies.imports.toCode();

    if (this.#operations.removeInitImport) {
      const importHillaCore = importStatements.find(
        (statement) =>
          ts.isImportDeclaration(statement) &&
          (statement.moduleSpecifier as ts.StringLiteral).text === '@vaadin/hilla-core',
      );

      if (importHillaCore) {
        const updatedImportStatement = PushProcessor.#removeInitImport(importHillaCore as ts.ImportDeclaration);

        if (updatedImportStatement) {
          importStatements = importStatements.map((statement) => {
            if (statement === importHillaCore) {
              return updatedImportStatement;
            }

            return statement;
          });
        }
      }
    }

    const updatedStatements: readonly ts.Statement[] = [...importStatements, ...otherStatements];

    return createSourceFile(updatedStatements, this.#source.fileName);
  }

  static #doesInitParameterExist(parameters: ts.NodeArray<ts.ParameterDeclaration>): boolean {
    const last = parameters[parameters.length - 1];
    const lastType = last.type as ts.TypeReferenceNode;
    const lastTypeName = lastType.typeName as ts.Identifier;

    return lastTypeName.text === initParameterTypeName;
  }

  static #removeInitImport(importStatement: ts.ImportDeclaration): ts.Statement | undefined {
    const namedImports = importStatement.importClause?.namedBindings;
    if (namedImports && ts.isNamedImports(namedImports)) {
      const updatedElements = namedImports.elements.filter((element) => element.name.text !== 'EndpointRequestInit');

      const updatedImportClause = ts.factory.updateImportClause(
        importStatement.importClause,
        false, // FIXME: could be true, but it is false for regular endpoint calls, so sticking to that for now
        undefined,
        ts.factory.createNamedImports(updatedElements),
      );

      return ts.factory.updateImportDeclaration(
        importStatement,
        undefined,
        updatedImportClause,
        importStatement.moduleSpecifier,
        undefined,
      );
    }

    return undefined;
  }

  /**
   * Replace returned `Promise<Array<T>>` by the `Subscription<T>` type
   * @param declaration -
   */
  #replacePromiseType(declaration: ts.FunctionDeclaration) {
    const promiseType = (declaration.type as ts.TypeReferenceNode).typeArguments![0];
    const promiseArray = (ts.isUnionTypeNode(promiseType) ? promiseType.types[0] : promiseType) as ts.TypeReferenceNode;

    return ts.factory.createTypeReferenceNode(this.#subscriptionId(), promiseArray.typeArguments);
  }

  #updateFunction(declaration: ts.FunctionDeclaration): ts.FunctionDeclaration {
    const { parameters } = declaration;
    const doesInitParameterExist = PushProcessor.#doesInitParameterExist(parameters);

    return ts.factory.createFunctionDeclaration(
      undefined, // no async
      declaration.asteriskToken,
      declaration.name,
      declaration.typeParameters,
      // Remove the `init` parameter
      doesInitParameterExist ? parameters.slice(0, -1) : parameters,
      this.#replacePromiseType(declaration),
      PushProcessor.#updateFunctionBody(declaration, doesInitParameterExist),
    );
  }

  static #updateFunctionBody(declaration: ts.FunctionDeclaration, doesInitParameterExist: boolean): ts.Block {
    const returnStatement = declaration.body!.statements[0] as ts.ReturnStatement;
    const { arguments: args, expression, typeArguments } = returnStatement.expression! as ts.CallExpression;
    const call = expression as ts.PropertyAccessExpression;

    return ts.factory.createBlock([
      ts.factory.createReturnStatement(
        ts.factory.createCallExpression(
          ts.factory.createPropertyAccessExpression(
            call.expression,
            // `subscribe` instead of `call`
            ts.factory.createIdentifier('subscribe'),
          ),
          typeArguments,
          // remove the `init` parameter
          doesInitParameterExist ? args.slice(0, -1) : args,
        ),
      ),
    ]);
  }
}
