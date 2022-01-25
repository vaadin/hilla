import type Plugin from '@hilla/generator-typescript-core/Plugin.js';
import createFullyUniqueIdentifier from '@hilla/generator-typescript-utils/createFullyUniqueIdentifier.js';
import createSourceFile from '@hilla/generator-typescript-utils/createSourceFile.js';
import DependencyManager from '@hilla/generator-typescript-utils/dependencies/DependencyManager.js';
import PathManager from '@hilla/generator-typescript-utils/dependencies/PathManager.js';
import type { SourceFile } from 'typescript';
import ts from 'typescript';

export default class ClientProcessor {
  readonly #filePath: string;
  readonly #owner: Plugin;

  public constructor(fileName: string, owner: Plugin) {
    this.#filePath = new PathManager({ extension: 'ts' }).createRelativePath(fileName);
    this.#owner = owner;
  }

  public process(): SourceFile {
    this.#owner.logger.debug(`Generating ${this.#filePath}`);

    const { exports, imports, paths } = new DependencyManager(new PathManager());
    const clientClassId = imports.named.add(paths.createBareModulePath('@hilla/frontend', false), 'ConnectClient');

    const clientVarId = createFullyUniqueIdentifier('client');
    exports.default.set(clientVarId);

    const declaration = ts.factory.createVariableStatement(
      undefined,
      ts.factory.createVariableDeclarationList(
        [
          ts.factory.createVariableDeclaration(
            clientVarId,
            undefined,
            undefined,
            ts.factory.createNewExpression(clientClassId, undefined, [
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
