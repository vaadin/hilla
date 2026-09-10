/* eslint-disable import/no-extraneous-dependencies */
import { mkdir, readFile, rm, writeFile } from 'node:fs/promises';
import { dirname, join } from 'node:path';
import { pathToFileURL } from 'node:url';
import Generator from '@vaadin/hilla-generator-core/Generator.js';
import BackbonePlugin from '@vaadin/hilla-generator-plugin-backbone/index.js';
import LoggerFactory from '@vaadin/hilla-generator-utils/LoggerFactory.js';
import { beforeAll, describe, expect, it } from 'vitest';
import ModelPlugin from '../../src/index.js';

/**
 * The snapshot tests only compare text, so generated code that cannot be
 * evaluated still passes them. Mutually referencing models are the case that
 * matters: they import each other, and reading the other binding while the
 * module is still evaluating throws. `Model.json` has several such cycles, e.g.
 * `FormEntity` and `FormArrayTypes`.
 *
 * The sources are written into the package so that their imports resolve the
 * way an application's would, and are transformed by Vite on import.
 */
const outputDir = join(import.meta.dirname, '.generated');
const entityDir = 'com/example/application/endpoints/TsFormEndpoint';

function moduleUrl(name: string): string {
  return pathToFileURL(join(outputDir, name)).href;
}

async function generate(): Promise<readonly string[]> {
  const generator = new Generator([BackbonePlugin, ModelPlugin], {
    logger: new LoggerFactory({ name: 'model-plugin-runtime-test', verbose: true }),
  });

  const input = await readFile(new URL('./Model.json', import.meta.url), 'utf8');
  const files = await generator.process(input);

  await rm(outputDir, { force: true, recursive: true });

  return Promise.all(
    files.map(async (file) => {
      const path = join(outputDir, file.name);
      await mkdir(dirname(path), { recursive: true });
      await writeFile(path, await file.text());
      return file.name;
    }),
  );
}

describe('ModelPlugin', () => {
  let generated: readonly string[];

  beforeAll(async () => {
    generated = await generate();
  }, 30000);

  it('generates models that can be evaluated', async () => {
    const modelFiles = generated.filter((name) => name.endsWith('Model.ts'));
    expect(modelFiles.length, 'generated model files').to.be.greaterThan(0);

    const failures = (
      await Promise.all(
        modelFiles.map(async (name) => {
          try {
            const module = (await import(moduleUrl(name))) as { default?: unknown };
            return module.default === undefined ? `${name}: no default export` : undefined;
          } catch (e: unknown) {
            return `${name}: ${String(e)}`;
          }
        }),
      )
    ).filter(Boolean);

    expect(failures).to.deep.equal([]);
  }, 30000);

  it('resolves models that reference each other', async () => {
    // FormEntity has a FormArrayTypes property and vice versa, so whichever
    // module is evaluated first sees the other one uninitialised
    const { default: FormEntityModel } = (await import(moduleUrl(`${entityDir}/FormEntityModel.ts`))) as {
      default: unknown;
    };
    const { default: FormArrayTypesModel } = (await import(moduleUrl(`${entityDir}/FormArrayTypesModel.ts`))) as {
      default: unknown;
    };

    expect(FormEntityModel, 'FormEntityModel').to.not.be.undefined;
    expect(FormArrayTypesModel, 'FormArrayTypesModel').to.not.be.undefined;
  }, 30000);
});
