import ts, { SourceFile, Statement } from 'typescript';

export default function createSourceFile(statements: readonly Statement[], fileName: string): SourceFile {
  const sourceFile = ts.factory.createSourceFile(
    statements,
    ts.factory.createToken(ts.SyntaxKind.EndOfFileToken),
    ts.NodeFlags.None,
  );

  sourceFile.fileName = fileName;

  // Fixes the `createUniqueName` bug that causes the following error:
  //
  // TypeError: Cannot read properties of undefined (reading 'has')
  //   at Object.isFileLevelUniqueName
  (sourceFile as any).identifiers = new Map();

  return sourceFile;
}
