import type { EnumSchema, ReferenceSchema, Schema } from '@vaadin/generator-typescript-core/Schema.js';
import {
  convertReferenceSchemaToPath,
  convertReferenceSchemaToSpecifier,
  decomposeSchema,
  isComposedSchema,
  isEmptyObject,
  isEnumSchema,
  isNullableSchema,
  isObjectSchema,
  isReferenceSchema,
  NonEmptyObjectSchema,
} from '@vaadin/generator-typescript-core/Schema.js';
import {
  convertFullyQualifiedNameToRelativePath,
  simplifyFullyQualifiedName,
} from '@vaadin/generator-typescript-core/utils.js';
import createSourceFile from '@vaadin/generator-typescript-utils/createSourceFile.js';
import DependencyManager from '@vaadin/generator-typescript-utils/dependencies/DependencyManager.js';
import PathManager from '@vaadin/generator-typescript-utils/dependencies/PathManager.js';
import type { Identifier, InterfaceDeclaration, SourceFile, Statement } from 'typescript';
import ts, { TypeElement } from 'typescript';
import TypeSchemaProcessor from './TypeSchemaProcessor.js';
import type { BackbonePluginContext } from './utils.js';

const exportDefaultModifiers = [
  ts.factory.createModifier(ts.SyntaxKind.ExportKeyword),
  ts.factory.createModifier(ts.SyntaxKind.DefaultKeyword),
];

export class EntityProcessor {
  readonly #component: Schema;
  readonly #context: BackbonePluginContext;
  readonly #dependencies = new DependencyManager(new PathManager());
  readonly #fullyQualifiedName: string;
  readonly #name: string;
  readonly #path: string;
  readonly #sourcePaths = new PathManager('ts');

  public constructor(name: string, component: Schema, context: BackbonePluginContext) {
    this.#component = component;
    this.#context = context;
    this.#fullyQualifiedName = name;
    this.#name = simplifyFullyQualifiedName(name);
    this.#path = convertFullyQualifiedNameToRelativePath(name);
  }

  public process(): SourceFile {
    this.#context.logger.debug(`Processing entity: ${this.#name}`);

    const declaration = isEnumSchema(this.#component)
      ? this.#processEnum(this.#component)
      : this.#processExtendedClass(this.#component);

    const statements = declaration ? [declaration] : [];

    const { imports, exports } = this.#dependencies;

    return createSourceFile(
      [...imports.toCode(), ...statements, ...exports.toCode()],
      this.#sourcePaths.createRelativePath(this.#path),
    );
  }

  #processClass(schema: Schema): InterfaceDeclaration | undefined {
    if (!isObjectSchema(schema)) {
      this.#context.logger.error(schema, `The component is not an object: ${this.#fullyQualifiedName}`);
      return undefined;
    }

    if (isEmptyObject(schema)) {
      this.#context.logger.error(`The component has no properties: ${this.#fullyQualifiedName}`);
      return undefined;
    }

    return ts.factory.createInterfaceDeclaration(
      undefined,
      exportDefaultModifiers,
      this.#name,
      undefined,
      undefined,
      this.#processTypeElements(schema as NonEmptyObjectSchema),
    );
  }

  #processEnum({ enum: members }: EnumSchema): Statement {
    return ts.factory.createEnumDeclaration(
      undefined,
      exportDefaultModifiers,
      this.#name,
      members.map((member) => ts.factory.createEnumMember(member, ts.factory.createStringLiteral(member))) ?? [],
    );
  }

  #processExtendedClass(schema: Schema): Statement | undefined {
    const { logger } = this.#context;

    if (isComposedSchema(schema)) {
      const decomposed = decomposeSchema(schema);

      if (decomposed.length > 2) {
        logger.error(schema, `The schema for a class component ${this.#fullyQualifiedName} is broken.`);
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
          undefined,
          undefined,
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

  #processTypeElements({ properties }: NonEmptyObjectSchema): readonly TypeElement[] {
    return Object.entries(properties).map(([name, schema]) => {
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
