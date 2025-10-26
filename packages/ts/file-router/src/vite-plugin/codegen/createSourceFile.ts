import ts, { type SourceFile, type Statement } from 'typescript';

export default function createSourceFile(statements: readonly Statement[], fileName: string): SourceFile {
  const sourceFile = ts.createSourceFile(fileName, '', ts.ScriptTarget.ES2021, undefined, ts.ScriptKind.TS);
  return ts.factory.updateSourceFile(sourceFile, statements);
}
