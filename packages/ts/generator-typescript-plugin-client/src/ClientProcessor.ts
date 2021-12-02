import createSourceFile from '@vaadin/generator-typescript-utils/createSourceFile.js';
import DependencyManager from '@vaadin/generator-typescript-utils/DependencyManager.js';
import PathManager from '@vaadin/generator-typescript-utils/PathManager.js';
import type Pino from 'pino';
import type { SourceFile } from 'typescript';
import ts from 'typescript';

export default class ClientProcessor {
  public static readonly CLIENT_FILE_NAME = 'connect-client.default';

  readonly #logger: Pino.Logger;
  readonly #sourcePaths = new PathManager('ts');

  public declare ['constructor']: typeof ClientProcessor;

  public constructor(logger: Pino.Logger) {
    this.#logger = logger;
  }

  public process(): SourceFile {
    const { exports, imports, paths } = new DependencyManager(new PathManager());
    const clientImportId = imports.named.add(
      paths.createBareModulePath('@vaadin/flow-frontend/Connect', true),
      'ConnectClient',
    );

    const clientVarId = ts.factory.createUniqueName('client');
    exports.default.set(clientVarId);

    const declaration = ts.factory.createVariableStatement(
      undefined,
      ts.factory.createVariableDeclarationList(
        [
          ts.factory.createVariableDeclaration(
            clientVarId,
            undefined,
            undefined,
            ts.factory.createNewExpression(clientImportId, undefined, [
              ts.factory.createObjectLiteralExpression(
                [
                  ts.factory.createPropertyAssignment(
                    ts.factory.createIdentifier('prefix'),
                    ts.factory.createStringLiteral('connect'),
                  ),
                ],
                false,
              ),
            ]),
          ),
        ],
        ts.NodeFlags.Const,
      ),
    );

    return createSourceFile(
      [...imports.toCode(), declaration, ...exports.toCode()],
      this.#sourcePaths.createRelativePath(this.constructor.CLIENT_FILE_NAME),
    );
  }
}

/*
import {ConnectClient} from '@vaadin/flow-frontend/Connect';
const client = new ConnectClient({prefix: 'connect'});
export default client;
*/
