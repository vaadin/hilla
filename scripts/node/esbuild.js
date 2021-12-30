/* eslint-disable import/no-extraneous-dependencies */
import { transform } from 'esbuild';
import { relative } from 'path';
import { fileURLToPath, pathToFileURL } from 'url';

const cwd = pathToFileURL(process.cwd()).toString();

export async function resolve(specifier, context, defaultResolve) {
  if (
    specifier.startsWith('.') &&
    context.parentURL?.startsWith(cwd) &&
    !context.parentURL?.substring(cwd.length).startsWith('/node_modules/')
  ) {
    if (!specifier.endsWith('.js')) {
      throw new Error(
        `Local files without '.js' extensions are not supported: '${specifier}' in '${relative(
          process.cwd(),
          fileURLToPath(context.parentURL),
        )}'`,
      );
    }

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
