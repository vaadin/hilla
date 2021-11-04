import type Pino from 'pino';
import type { SourceFile, Statement } from 'typescript';
import ts from 'typescript';
import type ReferenceResolver from '../../core/ReferenceResolver.js';

export type BackbonePluginContext = Readonly<{
  logger: Pino.Logger;
  resolver: ReferenceResolver;
}>;

export const defaultMediaType = 'application/json';

export function createSourceFile(statements: readonly Statement[], fileName: string): SourceFile {
  const sourceFile = ts.factory.createSourceFile(
    statements,
    ts.factory.createToken(ts.SyntaxKind.EndOfFileToken),
    ts.NodeFlags.None,
  );

  sourceFile.fileName = `${fileName}.ts`;

  // Fixes the `createUniqueName` bug that causes the following error:
  //
  // TypeError: Cannot read properties of undefined (reading 'has')
  //   at Object.isFileLevelUniqueName
  (sourceFile as any).identifiers = new Map();

  return sourceFile;
}
