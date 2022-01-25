import type Plugin from '@vaadin/generator-typescript-core/Plugin.js';
import createSourceFile from '@vaadin/generator-typescript-utils/createSourceFile.js';
import DependencyManager from '@vaadin/generator-typescript-utils/dependencies/DependencyManager.js';
import PathManager from '@vaadin/generator-typescript-utils/dependencies/PathManager.js';
import { basename, dirname } from 'path';
import type { SourceFile } from 'typescript';

export default class BarrelProcessor {
  public static readonly BARREL_FILE_NAME = 'endpoints.ts';
  public declare ['constructor']: typeof BarrelProcessor;
  readonly #endpoints: readonly SourceFile[];
  readonly #owner: Plugin;
  readonly #sourcePaths = new PathManager({ extension: 'ts' });

  public constructor(endpoints: readonly SourceFile[], owner: Plugin) {
    this.#endpoints = endpoints;
    this.#owner = owner;
  }

  public process(): SourceFile {
    this.#owner.logger.info(`Generating '${this.constructor.BARREL_FILE_NAME}' file`);

    const { exports, imports } = this.#endpoints.reduce((acc, { fileName }) => {
      const specifier = basename(fileName, '.ts');
      const path = `${dirname(fileName)}/${specifier}`;

      const id = acc.imports.namespace.add(acc.paths.createRelativePath(path), specifier);
      acc.exports.named.add(specifier, false, id);

      return acc;
    }, new DependencyManager(new PathManager()));

    return createSourceFile(
      [...imports.toCode(), ...exports.toCode()],
      this.#sourcePaths.createRelativePath('endpoints'),
    );
  }
}
