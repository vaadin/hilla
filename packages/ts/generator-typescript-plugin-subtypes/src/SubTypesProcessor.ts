import { dirname } from 'path/posix';
import {
  convertReferenceSchemaToPath,
  convertReferenceSchemaToSpecifier,
  type ReferenceSchema,
} from '@hilla/generator-typescript-core/Schema.js';
import { simplifyFullyQualifiedName } from '@hilla/generator-typescript-core/utils.js';
import createSourceFile from '@hilla/generator-typescript-utils/createSourceFile.js';
import DependencyManager from '@hilla/generator-typescript-utils/dependencies/DependencyManager.js';
import PathManager from '@hilla/generator-typescript-utils/dependencies/PathManager.js';
import ts from 'typescript';

export class SubTypesProcessor {
  readonly #typeName: string;
  readonly #source: ts.SourceFile;
  readonly #oneOf: ReferenceSchema[];
  readonly #dependencies;

  constructor(typeName: string, source: ts.SourceFile, oneOf: ReferenceSchema[]) {
    this.#typeName = typeName;
    this.#source = source;
    this.#oneOf = oneOf;
    this.#dependencies = new DependencyManager(
      new PathManager({ extension: '.js', relativeTo: dirname(source.fileName) }),
    );
  }

  process(): ts.SourceFile {
    const { exports, imports, paths } = this.#dependencies;

    // import all sub types and return them
    const subTypes = this.#oneOf.map((schema) => {
      const path = paths.createRelativePath(convertReferenceSchemaToPath(schema));
      const subType = convertReferenceSchemaToSpecifier(schema);
      return imports.default.add(path, subType, true);
    });

    // create a union type from the sub types
    const union = ts.factory.createUnionTypeNode(
      subTypes.map((subType) => ts.factory.createTypeReferenceNode(subType)),
    );

    // create the statement
    const { fileName, statements } = this.#source;
    const unionTypeName = `${simplifyFullyQualifiedName(this.#typeName)}`;
    const unionIdentifier = ts.factory.createIdentifier(unionTypeName);
    const statement = ts.factory.createTypeAliasDeclaration(undefined, unionIdentifier, undefined, union);

    exports.default.set(unionTypeName);

    return createSourceFile([...imports.toCode(), ...statements, statement, ...exports.toCode()], fileName);
  }
}
