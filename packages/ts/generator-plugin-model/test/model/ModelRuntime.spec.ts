/* eslint-disable import/no-extraneous-dependencies */
import { mkdir, readFile, rm, writeFile } from 'node:fs/promises';
import { dirname, join } from 'node:path';
import { pathToFileURL } from 'node:url';
import ts from '@typescript/typescript6';
import Generator from '@vaadin/hilla-generator-core/Generator.js';
import BackbonePlugin from '@vaadin/hilla-generator-plugin-backbone/index.js';
import LoggerFactory from '@vaadin/hilla-generator-utils/LoggerFactory.js';
import { $defaultValue } from '@vaadin/hilla-models';
import chaiLike from 'chai-like';
import { beforeAll, chai, describe, expect, it } from 'vitest';
import ModelPlugin from '../../src/index.js';

chai.use(chaiLike);

/**
 * The snapshot tests only compare text, so generated code that cannot be
 * evaluated still passes them. Models that refer to one another are the case
 * that matters: they import each other, and reading the other binding while the
 * module is still evaluating throws. `Model.json` has several such cycles, e.g.
 * `FormEntity` and `FormArrayTypes`.
 *
 * The sources are written into the package so that their imports resolve the
 * way an application's would, and are pulled in through a single barrel, which
 * is also how an application loads them: one module graph, resolved statically.
 * Importing them one by one instead would let the module runner resolve a cycle
 * in an order no bundler would produce.
 */
const outputDir = join(import.meta.dirname, '.generated');
const barrelName = 'all-models.ts';

type Model = { readonly [$defaultValue]: unknown };
type Models = Readonly<Record<string, Model | undefined>>;

async function generate(): Promise<Models> {
  const generator = new Generator([BackbonePlugin, ModelPlugin], {
    logger: new LoggerFactory({ name: 'model-plugin-runtime-test', verbose: true }),
  });

  const input = await readFile(new URL('./Model.json', import.meta.url), 'utf8');
  const files = await generator.process(input);

  await rm(outputDir, { force: true, recursive: true });

  const names = await Promise.all(
    files.map(async (file) => {
      const path = join(outputDir, file.name);
      await mkdir(dirname(path), { recursive: true });
      await writeFile(path, await file.text());
      return file.name;
    }),
  );

  const models = names
    .filter((name) => name.endsWith('Model.ts'))
    .map((name) => [name.slice(name.lastIndexOf('/') + 1, -'.ts'.length), name.slice(0, -'.ts'.length)] as const);

  expect(models.length, 'generated model files').to.be.greaterThan(0);

  const barrel = [
    ...models.map(([local, path]) => `import ${local} from './${path}.js';`),
    `export default { ${models.map(([local]) => local).join(', ')} };`,
  ].join('\n');

  await writeFile(join(outputDir, barrelName), `${barrel}\n`);

  const module = (await import(pathToFileURL(join(outputDir, barrelName)).href)) as { default: Models };

  return module.default;
}

/**
 * Compiles what was written to the output directory. The snapshot tests compare
 * text and the evaluation tests run it, so neither notices a model the type
 * checker rejects, which is what an application would hit first.
 */
function typeCheck(): readonly string[] {
  const config = {
    module: ts.ModuleKind.ESNext,
    moduleResolution: ts.ModuleResolutionKind.Bundler,
    noEmit: true,
    skipLibCheck: true,
    strict: true,
    target: ts.ScriptTarget.ES2022,
  };

  const program = ts.createProgram([join(outputDir, barrelName)], config);

  return ts
    .getPreEmitDiagnostics(program)
    .filter((diagnostic) => diagnostic.file?.fileName.includes('/.generated/'))
    .map(
      (diagnostic) =>
        `${diagnostic.file!.fileName.slice(outputDir.length + 1)}: ${ts.flattenDiagnosticMessageText(diagnostic.messageText, ' ')}`,
    );
}

describe('ModelPlugin', () => {
  let models: Models;

  beforeAll(async () => {
    models = await generate();
  }, 30000);

  it('generates models that can be evaluated', () => {
    const missing = Object.entries(models)
      .filter(([, model]) => model === undefined)
      .map(([name]) => name);

    expect(missing).to.deep.equal([]);
  });

  it('resolves models that refer to each other', () => {
    // FormEntity has a FormArrayTypes property and vice versa, so whichever
    // module is evaluated first sees the other one uninitialised
    expect(models.FormEntityModel, 'FormEntityModel').to.not.be.undefined;
    expect(models.FormArrayTypesModel, 'FormArrayTypesModel').to.not.be.undefined;
  });

  it('generates models that type check', () => {
    expect(typeCheck()).to.deep.equal([]);
  }, 60000);

  it('builds a default value out of the generated models', () => {
    expect(models.FormEntityIdModel![$defaultValue]).to.have.property('Id').which.is.NaN;
    expect(models.FormArrayTypesModel![$defaultValue]).to.have.property('stringArray').which.deep.equals([]);
  });
});
