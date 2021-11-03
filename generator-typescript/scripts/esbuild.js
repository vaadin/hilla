/* eslint-disable import/no-extraneous-dependencies */
import { transform } from 'esbuild';
import { readFile } from 'fs/promises';
import { fileURLToPath } from 'url';

export async function resolve(specifier, context, defaultResolve) {
  if (specifier.startsWith('.') && specifier.endsWith('.js') && !context.parentURL.includes('node_modules')) {
    const { url } = defaultResolve(`${specifier.substring(0, specifier.length - 3)}.ts`, context, defaultResolve);

    return {
      url,
      format: 'module',
    };
  }

  return defaultResolve(specifier, context, defaultResolve);
}

export async function load(url, context, defaultLoad) {
  if (url.endsWith('.ts')) {
    const source = await readFile(fileURLToPath(url), 'utf8');
    const { code } = await transform(source, {
      sourcefile: url.pathname,
      sourcemap: 'both',
      loader: 'ts',
      target: `node${process.versions.node}`,
      format: 'esm',
    });

    return { source: code, format: 'module' };
  }

  return defaultLoad(url, context, defaultLoad);
}
