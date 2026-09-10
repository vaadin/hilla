/* eslint-disable symbol-description */
import { dirname } from 'path/posix';
import ts, { type Expression, type Identifier, type SourceFile, type Statement } from '@typescript/typescript6';
import {
  convertFullyQualifiedNameToRelativePath,
  convertReferenceSchemaToPath,
  convertReferenceSchemaToSpecifier,
  decomposeSchema,
  isComposedSchema,
  isEnumSchema,
  isObjectSchema,
  isReferenceSchema,
  type ObjectSchema,
  type ReferenceSchema,
  type Schema,
  simplifyFullyQualifiedName,
} from '@vaadin/hilla-generator-core/Schema.js';
import createSourceFile from '@vaadin/hilla-generator-utils/createSourceFile.js';
import DependencyManager from '@vaadin/hilla-generator-utils/dependencies/DependencyManager.js';
import PathManager from '@vaadin/hilla-generator-utils/dependencies/PathManager.js';
import { ModelSchemaExpressionProcessor } from './ModelSchemaProcessor.js';
import { type Context, importM, importModel } from './utils.js';

export type DependencyData = Readonly<{
  id: Identifier;
  path: string;
}>;

const $declare = Symbol();
const $dependencies = Symbol();
const $entity = Symbol();
const $fullyQualifiedName = Symbol();
const $model = Symbol();
const $processDeclaration = Symbol();

export abstract class EntityModelProcessor {
  static process(name: string, component: Schema, context: Context): SourceFile {
    context.owner.logger.debug(`Processing model for entity: ${name}`);

    const schema = isComposedSchema(component) ? decomposeSchema(component)[0] : component;

    return isEnumSchema(schema)
      ? // eslint-disable-next-line @typescript-eslint/no-use-before-define
        new EntityEnumModelProcessor(name).process() // eslint-disable-line no-use-before-define
      : // eslint-disable-next-line @typescript-eslint/no-use-before-define
        new EntityClassModelProcessor(name, component, context).process(); // eslint-disable-line no-use-before-define
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

  process(): SourceFile {
    const declaration = this[$processDeclaration]();

    const { exports, imports } = this[$dependencies];

    return createSourceFile(
      [...imports.toCode(), ...declaration, ...exports.toCode()],
      this.#outputPathManager.createRelativePath(this[$model].path),
    );
  }

  /**
   * Declares the model constant together with a type alias of the same name, so
   * that `export default` carries both meanings and consumers can keep using
   * the imported name in type and value position alike.
   */
  protected [$declare](initializer: Expression, type?: ts.TypeNode): readonly Statement[] {
    const { id } = this[$model];

    return [
      ts.factory.createVariableStatement(
        undefined,
        ts.factory.createVariableDeclarationList(
          [ts.factory.createVariableDeclaration(id, undefined, type, initializer)],
          ts.NodeFlags.Const,
        ),
      ),
      ts.factory.createTypeAliasDeclaration(undefined, id, undefined, ts.factory.createTypeQueryNode(id)),
    ];
  }

  protected abstract [$processDeclaration](): readonly Statement[];
}

export class EntityClassModelProcessor extends EntityModelProcessor {
  readonly #component: Schema;
  readonly #context: Context;

  constructor(name: string, component: Schema, context: Context) {
    super(name, true);

    this.#component = component;
    this.#context = context;
  }

  protected [$processDeclaration](): readonly Statement[] {
    const { logger } = this.#context.owner;

    let entitySchema = this.#component;
    let builder: Expression | undefined;

    if (isComposedSchema(this.#component)) {
      const decomposed = decomposeSchema(this.#component);

      if (decomposed.length > 2) {
        logger.debug(
          this.#component,
          `The schema for a class component ${this[$fullyQualifiedName]} has more than two components. This plugin will ignore it.`,
        );
        return [];
      }

      const [parentSchema, childSchema] = decomposed;

      if (!isReferenceSchema(parentSchema)) {
        logger.debug(parentSchema, 'Only reference schema allowed for parent class');
        return [];
      }

      entitySchema = childSchema;
      builder = ts.factory.createCallExpression(
        ts.factory.createPropertyAccessExpression(importM(this[$dependencies]), 'extend'),
        undefined,
        [this.#processParentClass(parentSchema)],
      );
    }

    return this.#processModelClass(entitySchema, builder);
  }

  #processModelClass(schema: Schema, base: Expression | undefined): readonly Statement[] {
    const { logger } = this.#context.owner;

    if (!isObjectSchema(schema)) {
      logger.debug(schema, `Component is not an object: ${this[$fullyQualifiedName]}`);
      return [];
    }

    const entity = this[$entity].id;
    const name = simplifyFullyQualifiedName(this[$fullyQualifiedName]);

    // `m.object(name)` starts a model, `m.extend(Parent).object(name)` a model
    // that inherits the properties of another one
    let expression: Expression = ts.factory.createCallExpression(
      ts.factory.createPropertyAccessExpression(base ?? importM(this[$dependencies]), 'object'),
      [ts.factory.createTypeReferenceNode(entity)],
      [ts.factory.createStringLiteral(name)],
    );

    expression = this.#processProperties(schema).reduce(
      (chain, [property, model]) =>
        ts.factory.createCallExpression(ts.factory.createPropertyAccessExpression(chain, 'property'), undefined, [
          ts.factory.createStringLiteral(property),
          model,
        ]),
      expression,
    );

    expression = ts.factory.createCallExpression(
      ts.factory.createPropertyAccessExpression(expression, 'build'),
      undefined,
      [],
    );

    return this[$declare](expression, this.#createTypeAnnotation(entity));
  }

  /**
   * Models that refer to one another cannot have their type inferred, as it
   * would refer to itself through the other one. Only those get an explicit
   * annotation, so that every other model keeps its properties navigable.
   */
  #createTypeAnnotation(entity: Identifier): ts.TypeNode | undefined {
    return this.#context.cycles.isMutuallyReferencing(this[$fullyQualifiedName])
      ? ts.factory.createTypeReferenceNode(importModel('ObjectModel', this[$dependencies]), [
          ts.factory.createTypeReferenceNode(entity),
        ])
      : undefined;
  }

  #processProperties({ properties }: ObjectSchema): ReadonlyArray<readonly [string, Expression]> {
    return Object.entries(properties ?? {}).map(([name, schema]) => [
      name,
      new ModelSchemaExpressionProcessor(
        schema,
        this[$dependencies],
        this[$fullyQualifiedName],
        this.#context.cycles,
      ).process(),
    ]);
  }

  #processParentClass(schema: ReferenceSchema): Identifier {
    const { imports, paths } = this[$dependencies];

    const specifier = convertReferenceSchemaToSpecifier(schema);
    const path = convertReferenceSchemaToPath(schema);
    const modelPath = paths.createRelativePath(`${path}Model`);
    const modelSpecifier = `${specifier}Model`;

    return imports.default.getIdentifier(modelPath) ?? imports.default.add(modelPath, modelSpecifier, false);
  }
}

export class EntityEnumModelProcessor extends EntityModelProcessor {
  constructor(name: string) {
    super(name, false);
  }

  protected [$processDeclaration](): readonly Statement[] {
    const name = simplifyFullyQualifiedName(this[$fullyQualifiedName]);

    return this[$declare](
      ts.factory.createCallExpression(
        ts.factory.createPropertyAccessExpression(importM(this[$dependencies]), 'enum'),
        undefined,
        [this[$entity].id, ts.factory.createStringLiteral(name)],
      ),
    );
  }
}
