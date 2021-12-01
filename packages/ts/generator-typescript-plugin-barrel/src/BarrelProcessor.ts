import createSourceFile from '@vaadin/generator-typescript-utils/createSourceFile.js';
import DependencyManager from '@vaadin/generator-typescript-utils/DependencyManager.js';
import { basename, dirname } from 'path';
import type Pino from 'pino';
import type { SourceFile } from 'typescript';

export default class BarrelProcessor {
  public static readonly BARREL_FILE_NAME = 'endpoints.ts';

  readonly #endpoints: readonly SourceFile[];
  readonly #logger: Pino.Logger;

  public declare ['constructor']: typeof BarrelProcessor;

  public constructor(endpoints: readonly SourceFile[], logger: Pino.Logger) {
    this.#endpoints = endpoints;
    this.#logger = logger;
  }

  public process(): SourceFile {
    this.#logger.info(`Generating '${this.constructor.BARREL_FILE_NAME}' file`);

    const { exports, imports } = this.#endpoints.reduce((acc, { fileName }) => {
      const specifier = basename(fileName, '.ts');
      const path = `${dirname(fileName)}/${specifier}.js`;

      const id = acc.imports.namespace.add(path, specifier);
      acc.exports.named.add(specifier, false, id);

      return acc;
    }, new DependencyManager());

    return createSourceFile([...imports.toCode(), ...exports.toCode()], 'endpoints');
  }
}
