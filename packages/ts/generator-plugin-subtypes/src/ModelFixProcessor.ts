import ts, { type SourceFile } from '@typescript/typescript6';
import createSourceFile from '@vaadin/hilla-generator-utils/createSourceFile.js';
import { propertyNameToString } from './utils.js';

export class ModelFixProcessor {
  readonly #source: SourceFile;
  readonly #propertyName: string;

  constructor(source: SourceFile, propertyName: string) {
    this.#source = source;
    this.#propertyName = propertyName;
  }

  process(): SourceFile {
    const statements = this.#source.statements.map((statement) => {
      // filter out the discriminator property from all models
      if (ts.isClassDeclaration(statement)) {
        const members = statement.members.filter(
          (member) => !(ts.isGetAccessor(member) && propertyNameToString(member.name) === this.#propertyName),
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
