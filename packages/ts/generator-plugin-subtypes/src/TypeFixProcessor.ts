import ts, { type SourceFile } from '@typescript/typescript6';
import createSourceFile from '@vaadin/hilla-generator-utils/createSourceFile.js';
import { createDiscriminatorProperty, propertyNameToString } from './utils.js';

export class TypeFixProcessor {
  readonly #source: SourceFile;
  readonly #discriminatorPropertyName: string;
  readonly #typeValues: readonly string[];

  /**
   * @param typeValues - the discriminator values the type accepts: its own
   * value, followed by the values of the subtypes below it, if any.
   */
  constructor(source: ts.SourceFile, discriminatorPropertyName: string, typeValues: readonly string[]) {
    this.#source = source;
    this.#discriminatorPropertyName = discriminatorPropertyName;
    this.#typeValues = typeValues;
  }

  process(): SourceFile {
    const statements = this.#source.statements.map((statement) => {
      // search in the interface definition
      if (ts.isInterfaceDeclaration(statement)) {
        const members = statement.members.map((member) => {
          // search for the discriminator property
          if (
            !ts.isPropertySignature(member) ||
            propertyNameToString(member.name) !== this.#discriminatorPropertyName
          ) {
            return member;
          }

          return createDiscriminatorProperty(this.#discriminatorPropertyName, this.#typeValues);
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
