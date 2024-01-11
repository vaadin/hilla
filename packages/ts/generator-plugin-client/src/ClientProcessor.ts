import type Plugin from '@vaadin/hilla-generator-core/Plugin.js';
import createFullyUniqueIdentifier from '@vaadin/hilla-generator-utils/createFullyUniqueIdentifier.js';
import createSourceFile from '@vaadin/hilla-generator-utils/createSourceFile.js';
import DependencyManager from '@vaadin/hilla-generator-utils/dependencies/DependencyManager.js';
import PathManager from '@vaadin/hilla-generator-utils/dependencies/PathManager.js';
import ts, { type SourceFile } from 'typescript';

export default class ClientProcessor {
  readonly #owner: Plugin;
  readonly #outputPath: string;

  constructor(fileName: string, owner: Plugin) {
    this.#outputPath = new PathManager({ extension: 'ts' }).createRelativePath(fileName);
    this.#owner = owner;
  }

  process(): SourceFile {
    this.#owner.logger.debug(`Generating ${this.#outputPath}`);

    const { exports, imports, paths } = new DependencyManager(new PathManager({ extension: '.js' }));
    const clientClassId = imports.named.add(paths.createBareModulePath('@vaadin/hilla-core', false), 'ConnectClient');

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

    return createSourceFile([...imports.toCode(), declaration, ...exports.toCode()], this.#outputPath);
  }
}
