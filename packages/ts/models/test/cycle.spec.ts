import { fileURLToPath } from 'node:url';
import ts from '@typescript/typescript6';
import { describe, expect, it, vi } from 'vitest';

/**
 * Two models that refer to each other across modules, in the shape the model
 * generator emits for a class whose property refers to its own subclass —
 * `Person.team: List<Employee>` with `Employee extends Person`.
 *
 * `PersonModel` defers its property reference with `m.lazy`, and `EmployeeModel`
 * defers its base with `m.extend(() => PersonModel)`. Neither module reads the
 * other while it is being evaluated, so either one can be the entry.
 *
 * Asserting that evaluation succeeds, rather than on any message, is deliberate:
 * an uninitialized binding surfaces as a `ReferenceError` under raw Node ESM but
 * as something else under a bundler that rewrites imports into namespace
 * accesses, which is what Vite — and therefore this suite — does.
 *
 * The fixtures stand in for generated code, so they are excluded from the
 * package tsconfig and from linting; this spec compiles them itself.
 */
const fixtures = new URL('./fixtures/', import.meta.url);

const ROOTS = ['cycle/PersonModel.ts', 'cycle/EmployeeModel.ts', 'cycle/typedAccess.ts'];

function compile(): readonly string[] {
  const program = ts.createProgram(
    ROOTS.map((name) => fileURLToPath(new URL(name, fixtures))),
    {
      module: ts.ModuleKind.ESNext,
      moduleResolution: ts.ModuleResolutionKind.Bundler,
      noEmit: true,
      skipLibCheck: true,
      strict: true,
      target: ts.ScriptTarget.ES2022,
    },
  );

  return ts
    .getPreEmitDiagnostics(program)
    .filter(({ file }) => file?.fileName.includes('/fixtures/'))
    .map(
      ({ file, messageText }) =>
        `${file!.fileName.split('/').slice(-2).join('/')}: ${ts.flattenDiagnosticMessageText(messageText, ' ')}`,
    );
}

type ModelObject = Readonly<{ [key: string]: unknown; [key: symbol]: unknown }>;

// the specifier is kept non-literal on purpose: a fixture TypeScript can resolve
// is a fixture it type checks, and these stand in for generated code that the
// package tsconfig deliberately leaves alone
async function load(entry: string): Promise<Readonly<{ default: ModelObject }>> {
  return (await import(/* @vite-ignore */ `./fixtures/cycle/${entry}.js`)) as Readonly<{ default: ModelObject }>;
}

/** Loads both models and the model library from one fresh module graph. */
async function loadBoth(entry: string) {
  vi.resetModules();

  // the entry has to be imported on its own and first: it decides which module
  // body runs before the other, which is the whole point of the fixture
  await load(entry);

  const person = await load('PersonModel');
  const employee = await load('EmployeeModel');
  const models = await import(/* @vite-ignore */ `../src/index.js`);

  return { $defaultValue: models.$defaultValue, EmployeeModel: employee.default, PersonModel: person.default };
}

describe('a reference cycle closed by inheritance', () => {
  it('compiles', () => {
    expect(compile()).to.deep.equal([]);
  });

  it.each(['PersonModel', 'EmployeeModel'])('inherits the base through %s as the entry', async (entry) => {
    const { EmployeeModel, PersonModel } = await loadBoth(entry);

    expect(EmployeeModel).to.have.property('name');
    expect(EmployeeModel).to.have.property('zip');
    expect(EmployeeModel).to.have.property('title');
    expect(EmployeeModel).to.be.an.instanceof(PersonModel);
  });

  it.each(['PersonModel', 'EmployeeModel'])('builds a default value through %s as the entry', async (entry) => {
    const { $defaultValue, EmployeeModel } = await loadBoth(entry);

    expect(EmployeeModel[$defaultValue]).to.deep.equal({
      name: '',
      team: [],
      title: '',
      zip: { code: '' },
    });
  });
});
