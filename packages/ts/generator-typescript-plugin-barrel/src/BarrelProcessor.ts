import { basename, dirname } from 'path';
import type Plugin from '@hilla/generator-typescript-core/Plugin.js';
import createSourceFile from '@hilla/generator-typescript-utils/createSourceFile.js';
import DependencyManager from '@hilla/generator-typescript-utils/dependencies/DependencyManager.js';
import PathManager from '@hilla/generator-typescript-utils/dependencies/PathManager.js';
import type { SourceFile } from 'typescript';

export default class BarrelProcessor {
  static readonly BARREL_FILE_NAME = 'endpoints.ts';
  declare ['constructor']: typeof BarrelProcessor;
  readonly #endpoints: readonly SourceFile[];
  readonly #outputPathManager = new PathManager({ extension: 'ts' });
  readonly #owner: Plugin;

  constructor(endpoints: readonly SourceFile[], owner: Plugin) {
    this.#endpoints = endpoints;
    this.#owner = owner;
  }

  process(): SourceFile {
    this.#owner.logger.debug(`Generating '${this.constructor.BARREL_FILE_NAME}' file`);

    const { exports, imports } = this.#endpoints.reduce(
      (acc, { fileName }) => {
        const specifier = basename(fileName, '.ts');
        const path = `${dirname(fileName)}/${specifier}`;

        const id = acc.imports.namespace.add(acc.paths.createRelativePath(path), specifier);
        acc.exports.named.add(specifier, false, id);

        return acc;
      },
      new DependencyManager(new PathManager({ extension: '.js' })),
    );

    return createSourceFile(
      [...imports.toCode(), ...exports.toCode()],
      this.#outputPathManager.createRelativePath('endpoints'),
    );
  }
}
