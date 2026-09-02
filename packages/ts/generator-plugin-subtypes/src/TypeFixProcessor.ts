import ts, { type SourceFile } from '@typescript/typescript6';
import createSourceFile from '@vaadin/hilla-generator-utils/createSourceFile.js';
import { createDiscriminatorProperty, propertyNameToString } from './utils.js';

export class TypeFixProcessor {
  readonly #source: SourceFile;
  readonly #discriminatorPropertyName: string;
  readonly #typeValue: string | undefined;

  /**
   * @param typeValue - the discriminator value of the type, or `undefined` when
   * the property has to be dropped because the discriminator is applied in the
   * union type instead.
   */
  constructor(source: ts.SourceFile, discriminatorPropertyName: string, typeValue: string | undefined) {
    this.#source = source;
    this.#discriminatorPropertyName = discriminatorPropertyName;
    this.#typeValue = typeValue;
  }

  process(): SourceFile {
    const statements = this.#source.statements.map((statement) => {
      // search in the interface definition
      if (ts.isInterfaceDeclaration(statement)) {
        const members = statement.members.flatMap((member) => {
          // search for the discriminator property
          if (
            !ts.isPropertySignature(member) ||
            propertyNameToString(member.name) !== this.#discriminatorPropertyName
          ) {
            return [member];
          }

          return this.#typeValue === undefined
            ? []
            : [createDiscriminatorProperty(this.#discriminatorPropertyName, this.#typeValue)];
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
