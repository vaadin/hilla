import type Plugin from '@hilla/generator-typescript-core/Plugin.js';
import type { ReferenceSchema, Schema } from '@hilla/generator-typescript-core/Schema.js';
import {
  convertReferenceSchemaToPath,
  convertReferenceSchemaToSpecifier,
  decomposeSchema,
  isComposedSchema,
  isEmptyObject,
  isObjectSchema,
  isReferenceSchema,
  ObjectSchema,
} from '@hilla/generator-typescript-core/Schema.js';
import {
  convertFullyQualifiedNameToRelativePath,
  simplifyFullyQualifiedName,
} from '@hilla/generator-typescript-core/utils.js';
import createSourceFile from '@hilla/generator-typescript-utils/createSourceFile.js';
import DependencyManager from '@hilla/generator-typescript-utils/dependencies/DependencyManager.js';
import PathManager from '@hilla/generator-typescript-utils/dependencies/PathManager.js';
import { dirname } from 'path/posix';
import type { ClassElement, Identifier, SourceFile, Statement } from 'typescript';
import ts, { ClassDeclaration } from 'typescript';
import {
  ModelSchemaContext,
  ModelSchemaExpressionProcessor,
  ModelSchemaTypeProcessor,
} from './ModelSchemaProcessor.js';

export type Context = Pick<ModelSchemaContext, 'isReferenceToEnum'> &
  Readonly<{
    owner: Plugin;
  }>;

export class EntityModelProcessor {
  readonly #component: Schema;
  readonly #dependencies: DependencyManager;
  readonly #entityId: Identifier;
  readonly #fullyQualifiedName: string;
  readonly #getPropertyModelSymbol: Identifier;
  readonly #id: Identifier;
  readonly #path: string;
  readonly #sourcePaths = new PathManager({ extension: 'ts' });
  readonly #context: Context;

  public constructor(name: string, component: Schema, context: Context) {
    this.#component = component;
    this.#context = context;
    this.#fullyQualifiedName = name;

    const entityName = simplifyFullyQualifiedName(name);
    const entityPath = convertFullyQualifiedNameToRelativePath(name);

    this.#path = `${entityPath}Model`;
    this.#dependencies = new DependencyManager(new PathManager({ relativeTo: dirname(this.#path) }));

    const { exports, imports, paths } = this.#dependencies;

    this.#getPropertyModelSymbol = imports.named.add('@hilla/form', '_getPropertyModel');
    this.#id = exports.default.set(`${entityName}Model`);

    this.#entityId = imports.default.add(paths.createRelativePath(entityPath), entityName, true);
  }

  public process(): SourceFile {
    this.#context.owner.logger.debug(`Processing model for entity: ${this.#entityId.text}`);

    const declaration = this.#processExtendedModelClass(this.#component, this.#entityId);

    const { imports, exports } = this.#dependencies;
    const importStatements = imports.toCode();
    const exportStatement = exports.toCode();

    return createSourceFile(
      [...importStatements, declaration, ...exportStatement].filter(Boolean) as readonly Statement[],
      this.#sourcePaths.createRelativePath(this.#path),
    );
  }

  #processClassElements({ required, properties }: ObjectSchema): readonly ClassElement[] {
    if (!properties) {
      return [];
    }

    const ctx = {
      dependencies: this.#dependencies,
      isReferenceToEnum: this.#context.isReferenceToEnum,
    };

    const requiredSet = new Set(required);
    return Object.entries(properties).map(([name, schema]) => {
      const type = new ModelSchemaTypeProcessor(schema, ctx).process();
      const args = new ModelSchemaExpressionProcessor(schema, ctx, (_) => !requiredSet.has(name)).process();

      return ts.factory.createGetAccessorDeclaration(
        undefined,
        undefined,
        ts.factory.createIdentifier(name),
        [],
        type,
        ts.factory.createBlock(
          [
            ts.factory.createReturnStatement(
              ts.factory.createAsExpression(
                ts.factory.createCallExpression(
                  ts.factory.createElementAccessExpression(ts.factory.createThis(), this.#getPropertyModelSymbol),
                  undefined,
                  [
                    ts.factory.createStringLiteral(name),
                    type.typeName as Identifier,
                    ts.factory.createArrayLiteralExpression(args),
                  ],
                ),
                type,
              ),
            ),
          ],
          true,
        ),
      );
    });
  }

  #processExtendedModelClass(schema: Schema, entity: Identifier): Statement | undefined {
    const { logger } = this.#context.owner;

    let entitySchema = schema;
    let parent;

    if (isComposedSchema(schema)) {
      const decomposed = decomposeSchema(schema);

      if (decomposed.length > 2) {
        logger.error(schema, `The schema for a class component ${this.#fullyQualifiedName} is broken.`);
        return undefined;
      }

      const [parentSchema, childSchema] = decomposed;

      if (!isReferenceSchema(parentSchema)) {
        logger.error(parent, 'Only reference schema allowed for parent class');
        return undefined;
      }

      entitySchema = childSchema;
      parent = this.#processParentClass(parentSchema);
    } else {
      parent = this.#dependencies.imports.named.add('@hilla/form', 'ObjectModel');
    }

    return this.#processModelClass(entitySchema, entity, parent);
  }

  #processModelClass(schema: Schema, entity: Identifier, parent: Identifier): ClassDeclaration | undefined {
    const { logger } = this.#context.owner;

    if (!isObjectSchema(schema)) {
      logger.error(schema, `Component is not an object: ${this.#fullyQualifiedName}`);
      return undefined;
    }

    if (isEmptyObject(schema)) {
      logger.warn(`Component has no properties: ${this.#fullyQualifiedName}`);
    }

    const typeT = ts.factory.createIdentifier('T');
    const modelTypeParameters = ts.factory.createTypeParameterDeclaration(
      undefined,
      typeT,
      ts.factory.createTypeReferenceNode(entity),
      ts.factory.createTypeReferenceNode(entity),
    );

    const emptyValueElement = ts.factory.createPropertyDeclaration(
      undefined,
      [ts.factory.createModifier(ts.SyntaxKind.StaticKeyword)],
      'createEmptyValue',
      undefined,
      ts.factory.createFunctionTypeNode(undefined, [], ts.factory.createTypeReferenceNode(entity)),
      undefined,
    );

    return ts.factory.createClassDeclaration(
      undefined,
      undefined,
      this.#id,
      [modelTypeParameters],
      [
        ts.factory.createHeritageClause(ts.SyntaxKind.ExtendsKeyword, [
          ts.factory.createExpressionWithTypeArguments(parent, [ts.factory.createTypeReferenceNode(typeT)]),
        ]),
      ],
      [emptyValueElement, ...this.#processClassElements(schema)],
    );
  }

  #processParentClass(schema: ReferenceSchema): Identifier {
    const { imports, paths } = this.#dependencies;

    const specifier = convertReferenceSchemaToSpecifier(schema);
    const path = convertReferenceSchemaToPath(schema);
    const modelPath = paths.createRelativePath(`${path}Model`);
    const modelSpecifier = `${specifier}Model`;

    return imports.default.add(modelPath, modelSpecifier, false);
  }
}
