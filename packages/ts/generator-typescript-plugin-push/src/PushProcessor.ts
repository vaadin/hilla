import createSourceFile from '@hilla/generator-typescript-utils/createSourceFile.js';
import DependencyManager from '@hilla/generator-typescript-utils/dependencies/DependencyManager.js';
import PathManager from '@hilla/generator-typescript-utils/dependencies/PathManager.js';
import ts from 'typescript';

const initParameterTypeName = 'EndpointRequestInit';

export default class PushProcessor {
  readonly #dependencies = new DependencyManager(new PathManager());
  readonly #methods: readonly string[];
  readonly #source: ts.SourceFile;
  readonly #subscriptionId: ts.Identifier;

  constructor(source: ts.SourceFile, methods: readonly string[]) {
    this.#methods = methods;
    this.#source = source;

    const { imports, paths } = this.#dependencies;

    this.#dependencies.imports.fromCode(source);
    this.#subscriptionId = imports.named.add(paths.createBareModulePath('@hilla/frontend', false), 'Subscription');
  }

  public process(): ts.SourceFile {
    const importStatements = this.#dependencies.imports.toCode();

    const updatedStatements: readonly ts.Statement[] = [
      ...importStatements,
      ...this.#source.statements
        .filter((statement) => !ts.isImportDeclaration(statement))
        .map((statement) => {
          if (ts.isFunctionDeclaration(statement)) {
            const functionName = statement.name?.text;

            // Checks if the method is in the list of methods to patch
            if (functionName && this.#methods.includes(functionName)) {
              return this.#updateFunction(statement);
            }
          }

          return statement;
        }),
    ];

    return createSourceFile(updatedStatements, this.#source.fileName);
  }

  #doesInitParameterExist(parameters: ts.NodeArray<ts.ParameterDeclaration>): boolean {
    const last = parameters[parameters.length - 1];
    const lastType = last.type as ts.TypeReferenceNode;
    const lastTypeName = lastType.typeName as ts.Identifier;

    return lastTypeName.text === initParameterTypeName;
  }

  /**
   * Replace returned `Promise<Array<T>>` by the `Subscription<T>` type
   * @param declaration
   * @private
   */
  #replacePromiseType(declaration: ts.FunctionDeclaration) {
    const promiseType = (declaration.type as ts.TypeReferenceNode).typeArguments![0];
    const promiseArray = (
      ts.isUnionTypeNode(promiseType) ? (promiseType as ts.UnionTypeNode).types[0] : promiseType
    ) as ts.TypeReferenceNode;

    return ts.factory.createTypeReferenceNode(this.#subscriptionId, promiseArray.typeArguments);
  }

  #updateFunction(declaration: ts.FunctionDeclaration): ts.FunctionDeclaration {
    const { parameters } = declaration;
    const doesInitParameterExist = this.#doesInitParameterExist(parameters);

    return ts.factory.createFunctionDeclaration(
      declaration.decorators,
      undefined, // no async
      declaration.asteriskToken,
      declaration.name,
      declaration.typeParameters,
      // Remove the `init` parameter
      doesInitParameterExist ? parameters.slice(0, -1) : parameters,
      this.#replacePromiseType(declaration),
      this.#updateFunctionBody(declaration, doesInitParameterExist),
    );
  }

  #updateFunctionBody(declaration: ts.FunctionDeclaration, doesInitParameterExist: boolean): ts.Block {
    const returnStatement = declaration.body!.statements[0] as ts.ReturnStatement;
    const { arguments: args, expression, typeArguments } = returnStatement.expression! as ts.CallExpression;
    const call = expression! as ts.PropertyAccessExpression;

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
