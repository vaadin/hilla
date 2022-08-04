import Plugin from '@hilla/generator-typescript-core/Plugin.js';
import type SharedStorage from '@hilla/generator-typescript-core/SharedStorage.js';
import createSourceFile from '@hilla/generator-typescript-utils/createSourceFile.js';
import type { OpenAPIV3 } from 'openapi-types';
import type { ReadonlyObjectDeep } from 'type-fest/source/readonly-deep';
import ts from 'typescript';

export default class PushPlugin extends Plugin {
  get path(): string {
    return import.meta.url;
  }

  public async execute(storage: SharedStorage): Promise<void> {
    const endpointMethods: Map<string, string[]> = new Map();

    Object.entries(storage.api.paths).forEach(([key, path]) => {
      const post = path?.post;

      if (post) {
        const { content } = post!.responses[200] as ReadonlyObjectDeep<OpenAPIV3.ResponseObject>;

        if (content) {
          const schema = content!['application/json']?.schema;

          if (schema) {
            // @ts-ignore
            const className = schema!['x-class-name'];

            if (className && ['reactor.core.publisher.Flux', 'dev.hilla.EndpointSubscription'].includes(className)) {
              const [endpoint, method] = key.split('/').slice(1);
              const methodNames = endpointMethods.get(endpoint) || [];
              methodNames.push(method);
              endpointMethods.set(endpoint, methodNames);
            }
          }
        }
      }
    });

    endpointMethods.forEach((methodNames, endpoint) => {
      const subscriptionIdentifier = ts.factory.createUniqueName('Subscription');
      const endpointFilename = `${endpoint}.ts`;
      const source = storage.sources.find((s) => s.fileName === endpointFilename)!;
      const updatedStatements: ts.Statement[] = [];

      for (let i = 0; i < source.statements.length; i++) {
        const statement = source.statements[i];
        let modifiedStatement: ts.Statement | undefined;

        if (ts.isImportDeclaration(statement)) {
          const importClause = statement.importClause!;

          if (importClause.namedBindings) {
            const namedImports = importClause.namedBindings as ts.NamedImports;
            modifiedStatement = ts.factory.createImportDeclaration(
              statement.decorators,
              statement.modifiers,
              ts.factory.createImportClause(
                importClause.isTypeOnly,
                importClause.name,
                ts.factory.createNamedImports([
                  ...namedImports.elements,
                  ts.factory.createImportSpecifier(
                    false,
                    ts.factory.createIdentifier('Subscription'),
                    subscriptionIdentifier,
                  ),
                ]),
              ),
              statement.moduleSpecifier,
              statement.assertClause,
            );
          }
        } else if (ts.isFunctionDeclaration(statement)) {
          const statementName = statement.name?.escapedText;

          if (statementName && methodNames.includes(statementName)) {
            const { parameters } = statement;
            const [lastParam] = parameters.slice(-1);
            const paramType = lastParam.type as ts.TypeReferenceNode;

            const initParamFound = (paramType.typeName as ts.Identifier).escapedText === 'EndpointRequestInit';
            const returnStatement = statement.body!.statements[0] as ts.ReturnStatement;
            const returnClient = returnStatement.expression! as ts.CallExpression;
            const call = returnClient.expression! as ts.PropertyAccessExpression;
            const unionType = (statement.type as ts.TypeReferenceNode).typeArguments![0] as ts.UnionTypeNode;
            const referenceType = unionType.types[0] as ts.TypeReferenceNode;

            modifiedStatement = ts.factory.createFunctionDeclaration(
              statement.decorators,
              undefined, // no async
              statement.asteriskToken,
              statement.name,
              statement.typeParameters,
              initParamFound ? parameters.slice(0, -1) : parameters,
              ts.factory.createUnionTypeNode([
                ts.factory.createTypeReferenceNode(subscriptionIdentifier, referenceType.typeArguments),
                unionType.types[1],
              ]),
              ts.factory.createBlock([
                ts.factory.createReturnStatement(
                  ts.factory.createCallExpression(
                    ts.factory.createPropertyAccessExpression(
                      call.expression,
                      ts.factory.createIdentifier('subscribe'),
                    ),
                    returnClient.typeArguments,
                    initParamFound ? returnClient.arguments.slice(0, -1) : returnClient.arguments,
                  ),
                ),
              ]),
            );
          }
        }

        updatedStatements.push(modifiedStatement || statement);
      }

      for (let i = 0; i < storage.sources.length; i++) {
        if (storage.sources[i].fileName === endpointFilename) {
          storage.sources[i] = createSourceFile(updatedStatements, endpointFilename);
        }
      }
    });
  }
}
