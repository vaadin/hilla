import { dirname } from 'path/posix';
import type Plugin from '@hilla/generator-typescript-core/Plugin.js';
import {
  type EnumSchema,
  type ReferenceSchema,
  type Schema,
  convertReferenceSchemaToPath,
  convertReferenceSchemaToSpecifier,
  decomposeSchema,
  isComposedSchema,
  isEmptyObject,
  isEnumSchema,
  isNullableSchema,
  isObjectSchema,
  isReferenceSchema,
  type ObjectSchema,
} from '@hilla/generator-typescript-core/Schema.js';
import {
  convertFullyQualifiedNameToRelativePath,
  simplifyFullyQualifiedName,
} from '@hilla/generator-typescript-core/utils.js';
import createSourceFile from '@hilla/generator-typescript-utils/createSourceFile.js';
import DependencyManager from '@hilla/generator-typescript-utils/dependencies/DependencyManager.js';
import PathManager from '@hilla/generator-typescript-utils/dependencies/PathManager.js';
import ts, {
  type Identifier,
  type InterfaceDeclaration,
  type SourceFile,
  type Statement,
  type TypeElement,
} from 'typescript';
import TypeSchemaProcessor from './TypeSchemaProcessor.js';

export class EntityProcessor {
  readonly #component: Schema;
  readonly #dependencies;
  readonly #fullyQualifiedName: string;
  readonly #name: string;
  readonly #outputPathManager = new PathManager({ extension: 'ts' });
  readonly #owner: Plugin;
  readonly #path: string;

  constructor(name: string, component: Schema, owner: Plugin) {
    this.#component = component;
    this.#owner = owner;
    this.#fullyQualifiedName = name;
    this.#name = simplifyFullyQualifiedName(name);
    this.#path = convertFullyQualifiedNameToRelativePath(name);
    this.#dependencies = new DependencyManager(new PathManager({ extension: '.js', relativeTo: dirname(this.#path) }));
  }

  get #id(): Identifier {
    const id = ts.factory.createIdentifier(this.#name);

    this.#dependencies.exports.default.set(id);

    return id;
  }

  process(): SourceFile {
    this.#owner.logger.debug(`Processing entity: ${this.#name}`);

    const declaration = isEnumSchema(this.#component)
      ? this.#processEnum(this.#component)
      : this.#processExtendedClass(this.#component);

    const statements = declaration ? [declaration] : [];

    const { imports, exports } = this.#dependencies;

    return createSourceFile(
      [...imports.toCode(), ...statements, ...exports.toCode()],
      this.#outputPathManager.createRelativePath(this.#path),
    );
  }

  #processClass(schema: Schema): InterfaceDeclaration | undefined {
    const { logger } = this.#owner;

    if (!isObjectSchema(schema)) {
      logger.error(schema, `Component is not an object: '${this.#fullyQualifiedName}'`);
      return undefined;
    }

    if (isEmptyObject(schema)) {
      logger.warn(`Component has no properties:' ${this.#fullyQualifiedName}'`);
    }

    return ts.factory.createInterfaceDeclaration(
      undefined,
      this.#id,
      undefined,
      undefined,
      this.#processTypeElements(schema as ObjectSchema),
    );
  }

  #processEnum({ enum: members }: EnumSchema): Statement {
    return ts.factory.createEnumDeclaration(
      undefined,
      this.#id,
      members.map((member) => ts.factory.createEnumMember(member, ts.factory.createStringLiteral(member))),
    );
  }

  #processExtendedClass(schema: Schema): Statement | undefined {
    const { logger } = this.#owner;

    if (isComposedSchema(schema)) {
      const decomposed = decomposeSchema(schema);

      if (decomposed.length > 2) {
        logger.error(schema, `Schema for '${this.#fullyQualifiedName}' class component is broken.`);
        return undefined;
      }

      const [parent, child] = decomposed;

      if (!isReferenceSchema(parent)) {
        logger.error(parent, 'Only reference schema allowed for parent class');
        return undefined;
      }

      const declaration = this.#processClass(child);
      const identifier = this.#processParentClass(parent);

      return (
        declaration &&
        ts.factory.updateInterfaceDeclaration(
          declaration,
          declaration.modifiers,
          declaration.name,
          undefined,
          [
            ts.factory.createHeritageClause(ts.SyntaxKind.ExtendsKeyword, [
              ts.factory.createExpressionWithTypeArguments(identifier, undefined),
            ]),
          ],
          declaration.members,
        )
      );
    }

    return this.#processClass(schema);
  }

  #processParentClass(schema: ReferenceSchema): Identifier {
    const { imports, paths } = this.#dependencies;

    const specifier = convertReferenceSchemaToSpecifier(schema);
    const path = paths.createRelativePath(convertReferenceSchemaToPath(schema));

    return imports.default.add(path, specifier, true);
  }

  #processTypeElements({ properties }: ObjectSchema): readonly TypeElement[] {
    return Object.entries(properties ?? {}).map(([name, schema]) => {
      const [type] = new TypeSchemaProcessor(schema, this.#dependencies).process();

      return ts.factory.createPropertySignature(
        undefined,
        name,
        isNullableSchema(schema) ? ts.factory.createToken(ts.SyntaxKind.QuestionToken) : undefined,
        type,
      );
    });
  }
}
