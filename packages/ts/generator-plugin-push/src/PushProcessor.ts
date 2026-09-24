import ts from '@typescript/typescript6';
import createSourceFile from '@vaadin/hilla-generator-utils/createSourceFile.js';
import DependencyManager from '@vaadin/hilla-generator-utils/dependencies/DependencyManager.js';
import PathManager from '@vaadin/hilla-generator-utils/dependencies/PathManager.js';
import memoize from '@vaadin/hilla-generator-utils/memoize.js';

const initParameterTypeName = 'EndpointRequestInit';

export type EndpointOperations = {
  methodsToPatch: string[];
  removeInitImport: boolean;
};

export class PushProcessor {
  readonly #declaredNames: ReadonlySet<string>;
  readonly #dependencies = new DependencyManager(new PathManager({ extension: '.js' }));
  readonly #initParameterTypeId: ts.Identifier | undefined;
  readonly #operations: EndpointOperations;
  readonly #source: ts.SourceFile;
  readonly #subscriptionId: () => ts.Identifier;

  constructor(source: ts.SourceFile, operations: EndpointOperations) {
    this.#operations = operations;
    this.#source = source;

    const { exports, imports, paths } = this.#dependencies;

    this.#dependencies.imports.fromCode(source);
    this.#dependencies.exports.fromCode(source);
    // a method whose name is a reserved word is declared under a suffixed one
    this.#declaredNames = new Set(
      operations.methodsToPatch.map((method) => exports.named.getIdentifier(method)?.text ?? method),
    );
    // the init type is imported under a suffixed name when something else in
    // the file took the bare one
    this.#initParameterTypeId = imports.named.getIdentifier(
      paths.createBareModulePath('@vaadin/hilla-frontend', false),
      initParameterTypeName,
    );
    this.#subscriptionId = memoize(() =>
      imports.named.add(paths.createBareModulePath('@vaadin/hilla-frontend', false), 'Subscription'),
    );
  }

  process(): ts.SourceFile {
    const otherStatements = this.#source.statements
      .filter((statement) => !ts.isImportDeclaration(statement))
      .map((statement) => {
        if (ts.isFunctionDeclaration(statement)) {
          const functionName = statement.name?.text;

          // Checks if the method is in the list of methods to patch
          if (functionName && this.#declaredNames.has(functionName)) {
            return this.#updateFunction(statement);
          }
        }

        return statement;
      });

    let importStatements = this.#dependencies.imports.toCode();

    if (this.#operations.removeInitImport) {
      const importHillaFrontend = importStatements.find(
        (statement) =>
          ts.isImportDeclaration(statement) &&
          (statement.moduleSpecifier as ts.StringLiteral).text === '@vaadin/hilla-frontend',
      );

      if (importHillaFrontend) {
        const updatedImportStatement = PushProcessor.#removeInitImport(importHillaFrontend as ts.ImportDeclaration);

        if (updatedImportStatement) {
          importStatements = importStatements.map((statement) => {
            if (statement === importHillaFrontend) {
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

  #doesInitParameterExist(parameters: ts.NodeArray<ts.ParameterDeclaration>): boolean {
    const last = parameters[parameters.length - 1];
    const lastType = last.type as ts.TypeReferenceNode;

    return lastType.typeName === this.#initParameterTypeId;
  }

  static #removeInitImport(importStatement: ts.ImportDeclaration): ts.Statement | undefined {
    const namedImports = importStatement.importClause?.namedBindings;
    if (namedImports && ts.isNamedImports(namedImports)) {
      const updatedElements = namedImports.elements.filter(
        // the local binding may be aliased, so the specifier is what to match
        (element) => (element.propertyName ?? element.name).text !== initParameterTypeName,
      );

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
    const [promiseType] = (declaration.type as ts.TypeReferenceNode).typeArguments!;
    const promiseArray = (ts.isUnionTypeNode(promiseType) ? promiseType.types[0] : promiseType) as ts.TypeReferenceNode;

    return ts.factory.createTypeReferenceNode(this.#subscriptionId(), promiseArray.typeArguments);
  }

  #updateFunction(declaration: ts.FunctionDeclaration): ts.FunctionDeclaration {
    const { parameters } = declaration;
    const doesInitParameterExist = this.#doesInitParameterExist(parameters);

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
