import {
  convertReferenceSchemaToPath,
  convertReferenceSchemaToSpecifier,
  decomposeSchema,
  isComposedSchema,
  isEmptyObject,
  isObjectSchema,
  isReferenceSchema,
  ObjectSchema,
  ReferenceSchema,
  Schema,
} from '@hilla/generator-typescript-core/Schema.js';
import type DependencyManager from '@hilla/generator-typescript-utils/dependencies/DependencyManager.js';
import ts, { ClassDeclaration, ClassElement, Identifier } from 'typescript';
import { ModelSchemaExpressionProcessor, ModelSchemaTypeProcessor } from './ModelSchemaProcessor.js';
import type { Context, DependencyData } from './utils.js';
import { importBuiltInFormModel } from './utils.js';

export class EntityClassModelProcessor {
  readonly #component: Schema;
  readonly #context: Context;
  readonly #dependencies: DependencyManager;
  readonly #entity: DependencyData;
  readonly #fullyQualifiedName: string;
  readonly #getPropertyModelSymbol: Identifier;
  readonly #model: DependencyData;

  public constructor(
    name: string,
    component: Schema,
    dependencies: DependencyManager,
    entity: DependencyData,
    model: DependencyData,
    context: Context,
  ) {
    this.#component = component;
    this.#context = context;
    this.#dependencies = dependencies;
    this.#fullyQualifiedName = name;
    this.#entity = entity;
    this.#model = model;

    this.#getPropertyModelSymbol = dependencies.imports.named.add('@hilla/form', '_getPropertyModel');
  }

  public process(): ClassDeclaration | undefined {
    const { logger } = this.#context.owner;

    let entitySchema = this.#component;
    let parent;

    if (isComposedSchema(this.#component)) {
      const decomposed = decomposeSchema(this.#component);

      if (decomposed.length > 2) {
        logger.error(this.#component, `The schema for a class component ${this.#fullyQualifiedName} is broken.`);
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
      parent = importBuiltInFormModel('ObjectModel', this.#dependencies);
    }

    return this.#processModelClass(entitySchema, this.#entity.id, parent);
  }

  #processClassElements({ required, properties }: ObjectSchema): readonly ClassElement[] {
    if (!properties) {
      return [];
    }

    const ctx = {
      dependencies: this.#dependencies,
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
      this.#model.id,
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
