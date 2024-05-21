import { readFile } from 'node:fs/promises';
import { pathToFileURL } from 'node:url';

const scriptsDir = new URL('../', import.meta.url);

const registerJsCall = /(?!function)\s+__REGISTER__\(.*\)/gu;

const cwd = new URL(`${pathToFileURL(process.cwd())}/`);

export async function load(url, context, nextLoad) {
  const packageJson = JSON.parse(await readFile(new URL('package.json', cwd), 'utf8'));

  const result = await nextLoad(url, context);
  let source = result.source?.toString('utf8');

  if (registerJsCall.test(source)) {
    const registerCode = await readFile(new URL('./register.js', scriptsDir), 'utf8').then((c) =>
      c
        .replaceAll('export', '')
        .replaceAll('__NAME__', `'${packageJson.name ?? '@hilla/unknown'}'`)
        .replaceAll('__VERSION__', `'${packageJson.version ?? '0.0.0'}'`),
    );

    source = `${registerCode}\n\n${source}`;

    return {
      ...result,
      source,
    };
  }

  return result;
}
