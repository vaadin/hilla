/* eslint-disable import/no-extraneous-dependencies */
import { transform } from 'esbuild';
import { fileURLToPath } from 'url';

export async function resolve(specifier, context, defaultResolve) {
  if (specifier.startsWith('.') && specifier.endsWith('.js') && !context.parentURL.includes('node_modules')) {
    const { url } = defaultResolve(`${specifier.substring(0, specifier.length - 3)}.ts`, context, defaultResolve);

    return {
      format: 'module',
      url,
    };
  }

  return defaultResolve(specifier, context, defaultResolve);
}

export async function load(url, context, defaultLoad) {
  if (url.endsWith('.ts')) {
    const { source: rawSource, format } = await defaultLoad(url, { format: 'module' }, defaultLoad);

    if (rawSource === undefined || rawSource === null) {
      throw new Error(`Failed to load raw source: Format was '${format}' and url was '${url}''.`);
    }

    const source = typeof rawSource === 'string' ? rawSource : rawSource.toString('utf8');

    const { code } = await transform(source, {
      sourcefile: fileURLToPath(url),
      sourcemap: 'both',
      loader: 'ts',
      target: `node${process.versions.node}`,
      format: 'esm',
    });

    return { source: code, format };
  }

  return defaultLoad(url, context, defaultLoad);
}
