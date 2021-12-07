import createSourceFile from '@vaadin/generator-typescript-utils/createSourceFile.js';
import DependencyManager from '@vaadin/generator-typescript-utils/DependencyManager.js';
import PathManager from '@vaadin/generator-typescript-utils/PathManager.js';
import type Pino from 'pino';
import type { SourceFile } from 'typescript';
import ts from 'typescript';

export default class ClientProcessor {
  readonly #filePath: string;
  readonly #logger: Pino.Logger;

  public constructor(fileName: string, logger: Pino.Logger) {
    this.#filePath = new PathManager('ts').createRelativePath(fileName);
    this.#logger = logger;
  }

  public process(): SourceFile {
    this.#logger.debug(`Generating ${this.#filePath}`);

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

    return createSourceFile([...imports.toCode(), declaration, ...exports.toCode()], this.#filePath);
  }
}
