import { mkdir, readFile, writeFile } from 'node:fs/promises';
import { fileURLToPath, pathToFileURL } from 'node:url';
import cssnanoPlugin from 'cssnano';
import { glob } from 'glob';
import postcss from 'postcss';
import ast, { createTransformer } from 'tsc-template';
import type { PackageJson, TsConfigJson, Writable } from 'type-fest';
import {
  createCompilerHost,
  createProgram,
  factory,
  isIdentifier,
  isImportDeclaration,
  isStringLiteral,
  parseConfigFileTextToJson,
  type ParsedCommandLine,
  parseJsonConfigFileContent,
  type SourceFile,
  sys,
  type TransformerFactory,
} from 'typescript';

type Files = ReadonlyArray<readonly [fileName: URL, data: string]>;

const cwd = pathToFileURL(`${process.cwd()}/`);

/**
 * Loads the TypeScript compiler options from a tsconfig file.
 *
 * @param file - The URL of the tsconfig file.
 * @returns The parsed compiler options.
 */
async function loadTsConfigCompilerOptions(file: URL): Promise<ParsedCommandLine> {
  const { config } = parseConfigFileTextToJson(fileURLToPath(file), await readFile(file, 'utf8')) as {
    config: TsConfigJson;
  };

  return parseJsonConfigFileContent(config, sys, './');
}

const cssTransformer = postcss([cssnanoPlugin()]);

async function compileCssFile(fileName: string, inputDir: URL): Promise<readonly [string, string]> {
  const file = new URL(fileName, inputDir);
  const contents = await readFile(file, 'utf8');
  const compiled = await cssTransformer
    .process(contents, { from: fileURLToPath(file) })
    .then(({ content: c }) => c.replaceAll(/[`$]/gmu, '\\$&'));

  return [
    fileName.replace('.css', '.js'),
    `const css = new CSSStyleSheet();css.replaceSync(\`${compiled}\`);export default css;`,
  ];
}

/**
 * Loads the CSS files from the project.
 * @returns The CSS files and their content.
 */
async function compileCssFiles(): Promise<ReadonlyArray<readonly [string, string]>> {
  const inputDir = new URL('src/', cwd);
  const fileNames = await glob('**/*.obj.css', { cwd: inputDir });
  return await Promise.all(fileNames.map(async (fileName) => compileCssFile(fileName, inputDir)));
}

/**
 * Creates a transformer that injects the code that registers the custom
 * elements in the Vaadin namespace.
 */
async function createRegisterTransformer(): Promise<TransformerFactory<SourceFile>> {
  const { name, version } = JSON.parse(await readFile(new URL('package.json', cwd), 'utf8')) as PackageJson;

  const { node: registerFunctionCall } = ast`%{ ((feature, vaadinObj = (window.Vaadin ??= {})) => {
  vaadinObj.registrations ??= [];
  vaadinObj.registrations.push({
    is: feature ? \`${name}/\${feature}\` : '${name}',
    version: '${version}',
  });
}) }% ()`;

  return createTransformer((node) =>
    isIdentifier(node) && node.text === '__REGISTER__' ? registerFunctionCall : node,
  );
}

/**
 * A transformer that replaces the import of CSS files with the import of the
 * generated JS files.
 */
const importCssTransformer = createTransformer((node) =>
  isImportDeclaration(node) && isStringLiteral(node.moduleSpecifier) && node.moduleSpecifier.text.endsWith('.obj.css')
    ? factory.updateImportDeclaration(
        node,
        node.modifiers,
        node.importClause,
        factory.createStringLiteral(node.moduleSpecifier.text.replace('.obj.css', '.obj.js')),
        node.attributes,
      )
    : node,
);

/**
 * Compiles the TypeScript files applying the given transformers, and filters
 * empty JS files.
 *
 * @param options - The compiler options.
 * @param fileNames - The names of files to compile.
 * @param transformers - The transformers to apply.
 * @returns The array of names of the compiled files and their contents.
 */
function compileTypeScript(
  { options, fileNames }: ParsedCommandLine,
  transformers: ReadonlyArray<TransformerFactory<SourceFile>>,
): Files {
  const createdFiles: Array<[fileName: string, data: string]> = [];
  const host = createCompilerHost(options);
  host.writeFile = (fileName, data) => {
    createdFiles.push([fileName, data]);
  };
  const program = createProgram(fileNames, options, host);

  program.emit(undefined, undefined, undefined, undefined, {
    before: transformers as Writable<typeof transformers>,
  });

  return createdFiles
    .filter(
      ([fileName, data]) => !(fileName.endsWith('.js') && data.startsWith('export {}') && data.split('\n').length < 3),
    )
    .filter(([fileName], _, arr) => {
      if (!fileName.endsWith('.js.map')) {
        return true;
      }
      const sourceName = fileName.replace('.map', '');
      return arr.some(([f]) => f === sourceName);
    })
    .map(([fileName, data]) => [new URL(fileName, cwd), data]);
}

const [config, registerTransformer, cssFiles] = await Promise.all([
  loadTsConfigCompilerOptions(new URL('tsconfig.build.json', cwd)),
  createRegisterTransformer(),
  compileCssFiles(),
]);

const outDir = new URL(config.options.outDir ?? 'dist/', cwd);

await Promise.all(
  [
    ...compileTypeScript(config, [importCssTransformer, registerTransformer]),
    ...cssFiles.map(([fileName, data]) => [new URL(fileName, outDir), data] as const),
  ].map(async ([file, data]) => {
    await mkdir(new URL('./', file), { recursive: true });
    await writeFile(file, data, 'utf8');
  }),
);
