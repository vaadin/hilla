import ts, { type SourceFile } from '@typescript/typescript6';
import createSourceFile from '@vaadin/hilla-generator-utils/createSourceFile.js';
import { propertyNameToString } from './utils.js';

export class ModelFixProcessor {
  readonly #source: SourceFile;
  readonly #discriminatorPropertyName: string;

  constructor(source: SourceFile, discriminatorPropertyName: string) {
    this.#source = source;
    this.#discriminatorPropertyName = discriminatorPropertyName;
  }

  process(): SourceFile {
    const statements = this.#source.statements.map((statement) => {
      // filter out the discriminator property from all models
      if (ts.isClassDeclaration(statement)) {
        const members = statement.members.filter(
          (member) =>
            !(ts.isGetAccessor(member) && propertyNameToString(member.name) === this.#discriminatorPropertyName),
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
