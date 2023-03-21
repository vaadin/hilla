/* eslint-disable symbol-description */
import {
  convertReferenceSchemaToPath,
  convertReferenceSchemaToSpecifier,
  decomposeSchema,
  isComposedSchema,
  isEmptyObject,
  isEnumSchema,
  isObjectSchema,
  isReferenceSchema,
  type ObjectSchema,
  type ReferenceSchema,
  type Schema,
} from '@hilla/generator-typescript-core/Schema.js';
import {
  convertFullyQualifiedNameToRelativePath,
  simplifyFullyQualifiedName,
} from '@hilla/generator-typescript-core/utils.js';
import createSourceFile from '@hilla/generator-typescript-utils/createSourceFile.js';
import DependencyManager from '@hilla/generator-typescript-utils/dependencies/DependencyManager.js';
import PathManager from '@hilla/generator-typescript-utils/dependencies/PathManager.js';
import { dirname } from 'path/posix';
import type { ClassDeclaration, ClassElement, Identifier, SourceFile, Statement } from 'typescript';
import ts from 'typescript';
import { ModelSchemaExpressionProcessor, ModelSchemaTypeProcessor } from './ModelSchemaProcessor.js';
import type { Context } from './utils.js';
import { importBuiltInFormModel } from './utils.js';

export type DependencyData = Readonly<{
  id: Identifier;
  path: string;
}>;

const $dependencies = Symbol();
const $entity = Symbol();
const $fullyQualifiedName = Symbol();
const $model = Symbol();
const $processDeclaration = Symbol();

export abstract class EntityModelProcessor {
  public static process(name: string, component: Schema, context: Context): SourceFile {
    context.owner.logger.debug(`Processing model for entity: ${name}`);

    const schema = isComposedSchema(component) ? decomposeSchema(component)[0] : component;

    return isEnumSchema(schema)
      ? new EntityEnumModelProcessor(name).process() // eslint-disable-line no-use-before-define
      : new EntityClassModelProcessor(name, component, context).process(); // eslint-disable-line no-use-before-define
  }

  protected readonly [$dependencies]: DependencyManager;
  protected readonly [$entity]: DependencyData;
  protected readonly [$fullyQualifiedName]: string;
  protected readonly [$model]: DependencyData;
  readonly #outputPathManager = new PathManager({ extension: 'ts' });

  protected constructor(name: string, shouldImportEntityAsType: boolean) {
    this[$fullyQualifiedName] = name;

    const entityName = simplifyFullyQualifiedName(name);
    const entityPath = convertFullyQualifiedNameToRelativePath(name);

    const modelName = `${entityName}Model`;
    const modelPath = `${entityPath}Model`;
    this[$dependencies] = new DependencyManager(new PathManager({ extension: '.js', relativeTo: dirname(modelPath) }));

    const { exports, imports, paths } = this[$dependencies];

    this[$model] = {
      id: exports.default.set(modelName),
      path: modelPath,
    };

    this[$entity] = {
      id: imports.default.add(paths.createRelativePath(entityPath), entityName, shouldImportEntityAsType),
      path: entityPath,
    };
  }

  public process(): SourceFile {
    const declaration = this[$processDeclaration]();

    const { imports, exports } = this[$dependencies];
    const importStatements = imports.toCode();
    const exportStatement = exports.toCode();

    return createSourceFile(
      [...importStatements, declaration, ...exportStatement].filter(Boolean) as readonly Statement[],
      this.#outputPathManager.createRelativePath(this[$model].path),
    );
  }

  protected abstract [$processDeclaration](): ClassDeclaration | undefined;
}

export class EntityClassModelProcessor extends EntityModelProcessor {
  readonly #component: Schema;
  readonly #context: Context;
  readonly #fullyQualifiedName: string;
  readonly #getPropertyModelSymbol: Identifier;

  public constructor(name: string, component: Schema, context: Context) {
    super(name, true);

    this.#component = component;
    this.#context = context;
    this.#fullyQualifiedName = name;

    this.#getPropertyModelSymbol = this[$dependencies].imports.named.add('@hilla/form', '_getPropertyModel');
  }

  protected [$processDeclaration](): ClassDeclaration | undefined {
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
        logger.error(parentSchema, 'Only reference schema allowed for parent class');
        return undefined;
      }

      entitySchema = childSchema;
      parent = this.#processParentClass(parentSchema);
    } else {
      parent = importBuiltInFormModel('ObjectModel', this[$dependencies]);
    }

    return this.#processModelClass(entitySchema, this[$entity].id, parent);
  }

  #processClassElements({ properties }: ObjectSchema): readonly ClassElement[] {
    if (!properties) {
      return [];
    }

    return Object.entries(properties).map(([name, schema]) => {
      const type = new ModelSchemaTypeProcessor(schema, this[$dependencies]).process();
      const args = new ModelSchemaExpressionProcessor(schema, this[$dependencies]).process();

      return ts.factory.createGetAccessorDeclaration(
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
      [ts.factory.createModifier(ts.SyntaxKind.DeclareKeyword), ts.factory.createModifier(ts.SyntaxKind.StaticKeyword)],
      'createEmptyValue',
      undefined,
      ts.factory.createFunctionTypeNode(undefined, [], ts.factory.createTypeReferenceNode(entity)),
      undefined,
    );

    return ts.factory.createClassDeclaration(
      undefined,
      this[$model].id,
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
    const { imports, paths } = this[$dependencies];

    const specifier = convertReferenceSchemaToSpecifier(schema);
    const path = convertReferenceSchemaToPath(schema);
    const modelPath = paths.createRelativePath(`${path}Model`);
    const modelSpecifier = `${specifier}Model`;

    return imports.default.add(modelPath, modelSpecifier, false);
  }
}

export class EntityEnumModelProcessor extends EntityModelProcessor {
  public constructor(name: string) {
    super(name, false);
  }

  protected [$processDeclaration](): ClassDeclaration {
    const enumModel = importBuiltInFormModel('EnumModel', this[$dependencies]);
    const enumPropertySymbol = this[$dependencies].imports.named.add('@hilla/form', '_enum');

    return ts.factory.createClassDeclaration(
      undefined,
      this[$model].id,
      undefined,
      [
        ts.factory.createHeritageClause(ts.SyntaxKind.ExtendsKeyword, [
          ts.factory.createExpressionWithTypeArguments(enumModel, [
            ts.factory.createTypeQueryNode(this[$entity].id, undefined),
          ]),
        ]),
      ],
      [
        ts.factory.createPropertyDeclaration(
          [ts.factory.createModifier(ts.SyntaxKind.ReadonlyKeyword)],
          ts.factory.createComputedPropertyName(enumPropertySymbol),
          undefined,
          undefined,
          this[$entity].id,
        ),
      ],
    );
  }
}
