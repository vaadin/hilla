import { createRequire } from 'node:module';

export default function resolveNodeESM(packageName: string, importMeta: ImportMeta): string {
  const require = createRequire(importMeta.url);
  return require.resolve(packageName);
}
