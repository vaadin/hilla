import { opendir } from 'node:fs/promises';
import { basename, extname, relative } from 'node:path';
import { fileURLToPath } from 'node:url';

export type RouteMeta = Readonly<{
  path: string;
  file?: URL;
  layout?: URL;
  children: RouteMeta[];
}>;

export type CollectRoutesOptions = Readonly<{
  extensions: readonly string[];
  parent?: URL;
}>;

function cleanUp(blank: string) {
  return blank
    .replaceAll(/\{\.{3}(.+)\}/gu, '$1')
    .replaceAll(/\{{2}(.+)\}{2}/gu, '$1')
    .replaceAll(/\{(.+)\}/gu, '$1');
}

const collator = new Intl.Collator('en-US');

export default async function collectRoutes(
  dir: URL,
  { extensions, parent = dir }: CollectRoutesOptions,
): Promise<RouteMeta> {
  const path = relative(fileURLToPath(parent), fileURLToPath(dir));
  const children: RouteMeta[] = [];
  let layout: URL | undefined;

  for await (const d of await opendir(dir)) {
    if (d.isDirectory()) {
      children.push(await collectRoutes(new URL(`${d.name}/`, dir), { extensions, parent: dir }));
    } else if (d.isFile() && extensions.includes(extname(d.name))) {
      const file = new URL(d.name, dir);
      const name = basename(d.name, extname(d.name));

      if (name.includes('.layout')) {
        layout = file;
      } else if (!name.startsWith('_')) {
        children.push({
          path: name === 'index' ? '' : name,
          file,
          children: [],
        });
      }
    }
  }

  return {
    path,
    layout,
    children: children.sort(({ path: a }, { path: b }) => collator.compare(cleanUp(a), cleanUp(b))),
  };
}
