import { dirname } from 'path/posix';
import ts, { type SourceFile, type TypeNode } from '@typescript/typescript6';
import {
  convertReferenceSchemaToPath,
  convertReferenceSchemaToSpecifier,
  type ReferenceSchema,
  simplifyFullyQualifiedName,
} from '@vaadin/hilla-generator-core/Schema.js';
import createSourceFile from '@vaadin/hilla-generator-utils/createSourceFile.js';
import DependencyManager from '@vaadin/hilla-generator-utils/dependencies/DependencyManager.js';
import PathManager from '@vaadin/hilla-generator-utils/dependencies/PathManager.js';
import type { Discriminator } from './utils.js';
import { createDiscriminatedType, schemaKey } from './utils.js';

export class SubTypesProcessor {
  readonly #typeName: string;
  readonly #source: SourceFile;
  readonly #oneOf: readonly ReferenceSchema[];
  readonly #discriminator: Discriminator;
  readonly #dependencies: DependencyManager;

  constructor(typeName: string, source: SourceFile, oneOf: readonly ReferenceSchema[], discriminator: Discriminator) {
    this.#typeName = typeName;
    this.#source = source;
    this.#oneOf = oneOf;
    this.#discriminator = discriminator;
    this.#dependencies = new DependencyManager(
      new PathManager({ extension: '.js', relativeTo: dirname(source.fileName) }),
    );
  }

  process(): SourceFile {
    const { exports, imports, paths } = this.#dependencies;

    // import all subtypes and return them
    const subTypes = this.#oneOf.map((schema): TypeNode => {
      const path = paths.createRelativePath(convertReferenceSchemaToPath(schema));
      const subType = convertReferenceSchemaToSpecifier(schema);
      const type = ts.factory.createTypeReferenceNode(imports.default.add(path, subType, true));

      // a subtype that is the supertype of another subtype keeps the
      // discriminator open, so it has to be pinned here
      const typeValue = this.#discriminator.openTypes.get(schemaKey(schema));

      return typeValue === undefined
        ? type
        : createDiscriminatedType(type, this.#discriminator.propertyName, typeValue);
    });

    // create a union type from the subtypes
    const union = ts.factory.createUnionTypeNode(subTypes);

    // create the statement
    const { fileName, statements } = this.#source;
    const unionTypeName = `${simplifyFullyQualifiedName(this.#typeName)}`;
    const unionIdentifier = ts.factory.createIdentifier(unionTypeName);
    const statement = ts.factory.createTypeAliasDeclaration(undefined, unionIdentifier, undefined, union);

    exports.default.set(unionTypeName);

    return createSourceFile([...imports.toCode(), ...statements, statement, ...exports.toCode()], fileName);
  }
}
