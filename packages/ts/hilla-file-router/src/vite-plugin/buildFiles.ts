import { mkdir, readFile, writeFile } from 'node:fs/promises';
import type { Logger } from 'vite';
import collectRoutesFromFS from './collectRoutesFromFS.js';
import createRoutesFromMeta from './createRoutesFromMeta.js';
import createViewConfigJson from './createViewConfigJson.js';

export type RuntimeFileUrls = Readonly<{
  json: URL;
  code: URL;
}>;

async function maybeWriteFile(url: URL, data: string): Promise<void> {
  await mkdir(new URL('./', url), { recursive: true });
  const file = await readFile(url, 'utf-8');
  if (file !== data) {
    await writeFile(url, data, 'utf-8');
  }
}

async function generateRuntimeFiles(code: string, json: string, urls: RuntimeFileUrls, logger: Logger) {
  await Promise.all([
    maybeWriteFile(urls.json, json).then(() => logger.info(`Frontend route list is generated: ${String(urls.json)}`)),
    maybeWriteFile(urls.code, code).then(() => logger.info(`Views module is generated: ${String(urls.code)}`)),
  ]);
}

export async function buildFiles(
  viewsDir: URL,
  outDir: URL,
  generatedUrls: RuntimeFileUrls,
  extensions: readonly string[],
  logger: Logger,
): Promise<void> {
  const routeMeta = await collectRoutesFromFS(viewsDir, { extensions });
  logger.info('Collected file-based routes');
  const runtimeRoutesCode = createRoutesFromMeta(routeMeta, outDir);
  const viewConfigJson = await createViewConfigJson(routeMeta);

  await generateRuntimeFiles(runtimeRoutesCode, viewConfigJson, generatedUrls, logger);
}
