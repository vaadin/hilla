import createSourceFile from '@hilla/generator-typescript-utils/createSourceFile.js';
import ts, { type ClassDeclaration, type GetAccessorDeclaration } from 'typescript';

function propertyNameToString(node: ts.PropertyName): string | null {
  if (ts.isIdentifier(node) || ts.isStringLiteral(node) || ts.isNumericLiteral(node)) {
    return node.text;
  }
  return null;
}

export class ModelFixProcessor {
  readonly #source: ts.SourceFile;

  constructor(source: ts.SourceFile) {
    this.#source = source;
  }

  process(): ts.SourceFile {
    const statements = this.#source.statements.map((statement) => {
      // filter out the @type property from all models
      if (ts.isClassDeclaration(statement)) {
        const members = statement.members.filter(
          (member) => !(ts.isGetAccessor(member) && propertyNameToString(member.name) === '@type'),
        );

        return ts.factory.createClassDeclaration(
          statement.modifiers,
          statement.name,
          statement.typeParameters,
          statement.heritageClauses,
          members,
        );
      }

      return statement;
    });

    return createSourceFile(statements, this.#source.fileName);
  }
}
