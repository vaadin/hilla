import ts, { type SourceFile } from '@typescript/typescript6';
import createSourceFile from '@vaadin/hilla-generator-utils/createSourceFile.js';
import { createPropertyName, propertyNameToString } from './utils.js';

export class TypeFixProcessor {
  readonly #source: SourceFile;
  readonly #discriminatorPropertyName: string;
  readonly #typeValue: string;

  constructor(source: ts.SourceFile, discriminatorPropertyName: string, typeValue: string) {
    this.#source = source;
    this.#discriminatorPropertyName = discriminatorPropertyName;
    this.#typeValue = typeValue;
  }

  process(): SourceFile {
    const statements = this.#source.statements.map((statement) => {
      // search in the interface definition
      if (ts.isInterfaceDeclaration(statement)) {
        const members = statement.members.map((member) => {
          // search for the discriminator property and replace its type with a
          // string literal
          if (ts.isPropertySignature(member) && propertyNameToString(member.name) === this.#discriminatorPropertyName) {
            return ts.factory.createPropertySignature(
              undefined,
              createPropertyName(this.#discriminatorPropertyName),
              undefined,
              ts.factory.createLiteralTypeNode(ts.factory.createStringLiteral(this.#typeValue)),
            );
          }

          return member;
        });

        return ts.factory.createInterfaceDeclaration(
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
