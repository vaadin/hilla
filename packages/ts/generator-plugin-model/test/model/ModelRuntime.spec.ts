/* eslint-disable import/no-extraneous-dependencies */
import { mkdir, readFile, rm, writeFile } from 'node:fs/promises';
import { dirname, join } from 'node:path';
import { pathToFileURL } from 'node:url';
import Generator from '@vaadin/hilla-generator-core/Generator.js';
import BackbonePlugin from '@vaadin/hilla-generator-plugin-backbone/index.js';
import LoggerFactory from '@vaadin/hilla-generator-utils/LoggerFactory.js';
import { typeCheck } from '@vaadin/hilla-generator-utils/testing/typeCheck.js';
import { beforeAll, describe, expect, it } from 'vitest';
import ModelPlugin from '../../src/index.js';

/**
 * The snapshot tests only compare text, so generated code that throws still
 * passes them. Importing a model module only runs its class body; every
 * property expression — nested `ArrayModel` factories, validator construction,
 * optional flags — sits in a getter body. `createEmptyValue()` walks all of
 * them and recurses into child models, so it covers what the import alone does
 * not.
 *
 * The sources are written into the package so that their imports resolve the
 * way an application's would, and are transformed by Vite on import. Vite
 * strips the types rather than checking them, so type-incorrect output still
 * evaluates; `typeCheck()` compiles the same files to cover that.
 *
 * `Model.json` deliberately has no unbroken cycle of non-optional object
 * references, because `makeObjectEmptyValueCreator` recurses into every one of
 * them without a cycle guard and overflows the stack. `@NotNull` does not
 * produce that shape — the parser emits such a property as optional — but the
 * `@Nonnull` family does, so the generator can still emit one.
 */
const outputDir = join(import.meta.dirname, '.generated');

function moduleUrl(name: string): string {
  return pathToFileURL(join(outputDir, name)).href;
}

async function generate(): Promise<readonly File[]> {
  const generator = new Generator([BackbonePlugin, ModelPlugin], {
    logger: new LoggerFactory({ name: 'model-plugin-runtime-test', verbose: true }),
  });

  const input = await readFile(new URL('./Model.json', import.meta.url), 'utf8');
  const files = await generator.process(input);

  await rm(outputDir, { force: true, recursive: true });

  await Promise.all(
    files.map(async (file) => {
      const path = join(outputDir, file.name);
      await mkdir(dirname(path), { recursive: true });
      await writeFile(path, await file.text());
    }),
  );

  return files;
}

describe('ModelPlugin', () => {
  let generated: readonly File[];

  beforeAll(async () => {
    generated = await generate();
  }, 30000);

  it('generates models that can be evaluated', async () => {
    const modelFiles = generated.map((file) => file.name).filter((name) => name.endsWith('Model.ts'));
    expect(modelFiles.length, 'generated model files').to.be.greaterThan(0);

    // Every module is loaded before any getter runs. Vite's SSR runner resolves
    // an import as soon as that one module's body has run, even when a module it
    // is in a cycle with has not reached its `export default` yet — unlike
    // native ESM, which evaluates the whole cycle first.
    const models = await Promise.all(
      modelFiles.map(async (name) => {
        const module = (await import(moduleUrl(name))) as { default?: { createEmptyValue(): unknown } };
        return [name, module.default] as const;
      }),
    );

    const failures = models
      .map(([name, model]) => {
        if (model === undefined) {
          return `${name}: no default export`;
        }

        try {
          model.createEmptyValue();
          return undefined;
        } catch (e: unknown) {
          return `${name}: ${String(e)}`;
        }
      })
      .filter(Boolean);

    expect(failures).to.deep.equal([]);
  }, 30000);

  it('generates code that type-checks', async () => {
    expect(await typeCheck(outputDir, generated)).to.deep.equal([]);
  }, 30000); // compiling the whole model hierarchy is slow on CI
});
