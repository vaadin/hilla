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
  readonly #source: ts.SourceFile;
  readonly #typeName: string;
  readonly #oneOf: ReferenceSchema[];
  readonly #dependencies;

  constructor(typeName: string, source: ts.SourceFile, oneOf: ReferenceSchema[]) {
    this.#source = source;
    this.#typeName = typeName;
    this.#oneOf = oneOf;
    this.#dependencies = new DependencyManager(
      new PathManager({ extension: '.js', relativeTo: dirname(source.fileName) }),
    );
  }

  process(): ts.SourceFile {
    const { exports, imports, paths } = this.#dependencies;
    const subTypes = this.#oneOf.map((schema) => {
      const path = paths.createRelativePath(convertReferenceSchemaToPath(schema));
      const subType = convertReferenceSchemaToSpecifier(schema);
      return imports.default.add(path, subType, true);
    });

    const union = ts.factory.createUnionTypeNode(
      subTypes.map((subType) => ts.factory.createTypeReferenceNode(subType)),
    );

    const { fileName } = this.#source;
    const typeName = ts.factory.createIdentifier(simplifyFullyQualifiedName(this.#typeName));
    const statement = ts.factory.createTypeAliasDeclaration(undefined, typeName, undefined, union);

    exports.default.set(typeName);

    return createSourceFile([...imports.toCode(), statement, ...exports.toCode()], fileName);
  }
}
