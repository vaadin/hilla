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
      if (statement.kind === ts.SyntaxKind.ClassDeclaration) {
        const classDeclaration = statement as ClassDeclaration;
        const members = classDeclaration.members.filter((member) => {
          if (
            member.kind === ts.SyntaxKind.GetAccessor &&
            propertyNameToString((member as GetAccessorDeclaration).name) === '@type'
          ) {
            return false;
          }

          return true;
        });

        return ts.factory.createClassDeclaration(
          classDeclaration.modifiers,
          classDeclaration.name,
          classDeclaration.typeParameters,
          classDeclaration.heritageClauses,
          members,
        );
      }

      return statement;
    });

    return createSourceFile(statements, this.#source.fileName);
  }
}
