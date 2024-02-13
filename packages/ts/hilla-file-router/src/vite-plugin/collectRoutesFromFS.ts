import { opendir } from 'node:fs/promises';
import { basename, extname, relative } from 'node:path';
import { fileURLToPath } from 'node:url';
import { cleanUp } from './utils.js';

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

const collator = new Intl.Collator('en-US');

export default async function collectRoutesFromFS(
  dir: URL,
  { extensions, parent = dir }: CollectRoutesOptions,
): Promise<RouteMeta> {
  const path = relative(fileURLToPath(parent), fileURLToPath(dir));
  const children: RouteMeta[] = [];
  let layout: URL | undefined;

  for await (const d of await opendir(dir)) {
    if (d.isDirectory()) {
      children.push(await collectRoutesFromFS(new URL(`${d.name}/`, dir), { extensions, parent: dir }));
    } else if (d.isFile() && extensions.includes(extname(d.name))) {
      const file = new URL(d.name, dir);
      const name = basename(d.name, extname(d.name));

      if (name.startsWith('$')) {
        if (name === '$layout') {
          layout = file;
        } else if (name === '$index') {
          children.push({
            path: '',
            file,
            children: [],
          });
        } else {
          throw new Error('Symbol "$" is reserved for special files; only "$layout" and "$index" are allowed');
        }
      } else if (!name.startsWith('_')) {
        children.push({
          path: name,
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
