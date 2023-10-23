import { readFile } from 'node:fs/promises';
import { sep } from 'node:path';
import { fileURLToPath, pathToFileURL } from 'node:url';
import { build } from 'esbuild';
import { glob } from 'glob';
import type { PackageJson } from 'type-fest';

const scriptsDir = new URL('./', import.meta.url);
const packageRoot = pathToFileURL(process.cwd() + sep);
const [packageJsonFile, srcFiles] = await Promise.all([
  readFile(new URL('package.json', packageRoot), 'utf8'),
  glob('src/**/*.{ts,tsx}', { ignore: 'src/**/*.d.ts' }),
]);

const packageJson: PackageJson = JSON.parse(packageJsonFile);

await build({
  define: {
    __NAME__: `'${packageJson.name ?? '@hilla/unknown'}'`,
    __VERSION__: `'${packageJson.version ?? '0.0.0'}'`,
  },
  inject: [fileURLToPath(new URL('./register.js', scriptsDir))],
  entryPoints: srcFiles.map((file) => new URL(file, packageRoot)).map(fileURLToPath),
  format: 'esm',
  outdir: fileURLToPath(packageRoot),
  packages: 'external',
  sourcemap: 'linked',
  sourcesContent: true,
  tsconfig: fileURLToPath(new URL('./tsconfig.build.json', packageRoot)),
});
