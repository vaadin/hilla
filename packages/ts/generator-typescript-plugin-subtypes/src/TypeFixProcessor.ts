import { dirname } from 'path/posix';
import { convertFullyQualifiedNameToRelativePath } from '@hilla/generator-typescript-core/utils.js';
import createSourceFile from '@hilla/generator-typescript-utils/createSourceFile.js';
import DependencyManager from '@hilla/generator-typescript-utils/dependencies/DependencyManager.js';
import PathManager from '@hilla/generator-typescript-utils/dependencies/PathManager.js';
import ts from 'typescript';

function propertyNameToString(node: ts.PropertyName): string | null {
  if (ts.isIdentifier(node) || ts.isStringLiteral(node) || ts.isNumericLiteral(node)) {
    return node.text;
  }
  return null;
}

export class TypeFixProcessor {
  readonly #typeName: string;
  readonly #source: ts.SourceFile;
  readonly #typeValue: string;
  readonly #dependencies;

  constructor(typeName: string, source: ts.SourceFile, typeValue: string) {
    this.#typeName = typeName;
    this.#source = source;
    this.#typeValue = typeValue;
    this.#dependencies = new DependencyManager(
      new PathManager({ extension: '.js', relativeTo: dirname(source.fileName) }),
    );
  }

  process(): ts.SourceFile {
    const { paths } = this.#dependencies;
    const path = paths.createRelativePath(convertFullyQualifiedNameToRelativePath(this.#typeName));
    const statements = this.#source.statements.map((statement) => {
      if (
        ts.isImportDeclaration(statement) &&
        ts.isStringLiteral(statement.moduleSpecifier) &&
        propertyNameToString(statement.moduleSpecifier) === path
      ) {
        return undefined;
      } else if (ts.isInterfaceDeclaration(statement)) {
        const members = statement.members.map((member) => {
          if (ts.isPropertySignature(member)) {
            if (propertyNameToString(member.name) === '@type') {
              return ts.factory.createPropertySignature(
                undefined,
                ts.factory.createStringLiteral('@type'),
                undefined,
                ts.factory.createLiteralTypeNode(ts.factory.createStringLiteral(this.#typeValue)),
              );
            }
          }

          return member;
        });

        return ts.factory.createInterfaceDeclaration(
          statement.modifiers,
          statement.name,
          statement.typeParameters,
          undefined,
          members,
        );
      }

      return statement;
    });

    return createSourceFile(statements.filter((s) => s !== undefined) as ts.Statement[], this.#source.fileName);
  }
}
