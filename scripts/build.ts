import { readFile } from 'node:fs/promises';
import { sep } from 'node:path';
import { fileURLToPath, pathToFileURL } from 'node:url';
import { build } from 'esbuild';
import { glob } from 'glob';
import type { PackageJson } from 'type-fest';

const root = pathToFileURL(process.cwd() + sep);
const [packageJsonFile, srcFiles] = await Promise.all([
  readFile(new URL('package.json', root), 'utf8'),
  glob('src/**/*.{ts,tsx}', { ignore: 'src/**/*.d.ts' }),
]);

const packageJson: PackageJson = JSON.parse(packageJsonFile);

await build({
  define: {
    __VERSION__: `'${packageJson.version ?? '0.0.0'}'`,
  },
  entryPoints: srcFiles.map((file) => new URL(file, root)).map(fileURLToPath),
  format: 'esm',
  minify: true,
  outdir: fileURLToPath(root),
  packages: 'external',
  sourcemap: 'linked',
  sourcesContent: true,
  tsconfig: fileURLToPath(new URL('./tsconfig.build.json', root)),
});
