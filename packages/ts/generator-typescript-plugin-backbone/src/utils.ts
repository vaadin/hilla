import type Pino from 'pino';
import type { SourceFile, Statement } from 'typescript';
import ts from 'typescript';
import { posix, parse } from 'path';
import type ReferenceResolver from '@vaadin/generator-typescript-core/ReferenceResolver.js';

export type BackbonePluginContext = Readonly<{
  logger: Pino.Logger;
  resolver: ReferenceResolver;
}>;

export const defaultMediaType = 'application/json';

export const clientLib = {
  specifier: 'client',
  path: './connect-client.default.js',
} as const;

export function createSourceFile(statements: readonly Statement[], filePath: string): SourceFile {
  const sourceFile = ts.factory.createSourceFile(
    statements,
    ts.factory.createToken(ts.SyntaxKind.EndOfFileToken),
    ts.NodeFlags.None,
  );

  const { dir, ext, name } = parse(filePath);

  sourceFile.fileName = `./${ext === '.js' ? posix.join(dir, name) : filePath}.ts`;

  // Fixes the `createUniqueName` bug that causes the following error:
  //
  // TypeError: Cannot read properties of undefined (reading 'has')
  //   at Object.isFileLevelUniqueName
  (sourceFile as any).identifiers = new Map();

  return sourceFile;
}
