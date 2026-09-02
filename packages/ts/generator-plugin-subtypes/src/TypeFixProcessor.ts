import ts, { type SourceFile } from '@typescript/typescript6';
import createSourceFile from '@vaadin/hilla-generator-utils/createSourceFile.js';
import { createPropertyName, propertyNameToString } from './utils.js';

export class TypeFixProcessor {
  readonly #source: SourceFile;
  readonly #typeValue: string;
  readonly #propertyName: string;
  readonly #omitInheritedProperty: boolean;

  constructor(source: ts.SourceFile, typeValue: string, propertyName: string, omitInheritedProperty: boolean) {
    this.#source = source;
    this.#typeValue = typeValue;
    this.#propertyName = propertyName;
    this.#omitInheritedProperty = omitInheritedProperty;
  }

  process(): SourceFile {
    const statements = this.#source.statements.map((statement) => {
      // search in the interface definition
      if (ts.isInterfaceDeclaration(statement)) {
        const members = statement.members.map((member) => {
          // search for the discriminator property and replace its type with a
          // string literal
          if (ts.isPropertySignature(member) && propertyNameToString(member.name) === this.#propertyName) {
            return ts.factory.createPropertySignature(
              undefined,
              createPropertyName(this.#propertyName),
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
          this.#fixHeritageClauses(statement.heritageClauses),
          members,
        );
      }

      return statement;
    });

    return createSourceFile(statements, this.#source.fileName);
  }

  /**
   * When the supertype declares the discriminator too, its value there is a
   * different string literal, so the property has to be omitted from the
   * inherited type to keep the generated interface valid.
   */
  #fixHeritageClauses(
    heritageClauses: ts.NodeArray<ts.HeritageClause> | undefined,
  ): readonly ts.HeritageClause[] | undefined {
    if (!this.#omitInheritedProperty || !heritageClauses) {
      return heritageClauses;
    }

    return heritageClauses.map((clause) =>
      ts.factory.updateHeritageClause(
        clause,
        clause.types.map((type) => {
          if (!ts.isIdentifier(type.expression)) {
            return type;
          }

          return ts.factory.createExpressionWithTypeArguments(ts.factory.createIdentifier('Omit'), [
            ts.factory.createTypeReferenceNode(type.expression, type.typeArguments),
            ts.factory.createLiteralTypeNode(ts.factory.createStringLiteral(this.#propertyName)),
          ]);
        }),
      ),
    );
  }
}
