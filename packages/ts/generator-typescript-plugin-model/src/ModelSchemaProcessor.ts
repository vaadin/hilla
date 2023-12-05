import {
  type ArraySchema,
  type BooleanSchema,
  convertReferenceSchemaToPath,
  convertReferenceSchemaToSpecifier,
  decomposeSchema,
  type IntegerSchema,
  isArraySchema,
  isBooleanSchema,
  isComposedSchema,
  isIntegerSchema,
  isMapSchema,
  isNullableSchema,
  isNumberSchema,
  isReferenceSchema,
  isStringSchema,
  type MapSchema,
  type NumberSchema,
  type ReferenceSchema,
  type Schema,
  type StringSchema,
} from '@hilla/generator-typescript-core/Schema.js';
import type DependencyManager from '@hilla/generator-typescript-utils/dependencies/DependencyManager.js';
import ts, {
  type Expression,
  type Identifier,
  type PropertyAssignment,
  type TypeNode,
  type TypeReferenceNode,
} from 'typescript';
import { MetadataProcessor } from './MetadataProcessor.js';
import { createModelBuildingCallback, importBuiltInFormModel } from './utils.js';
import { hasValidationConstraints, ValidationConstraintProcessor } from './ValidationConstraintProcessor.js';

const $dependencies = Symbol();
const $processArray = Symbol();
const $processRecord = Symbol();
const $processReference = Symbol();
const $processString = Symbol();
const $processNumber = Symbol();
const $processBoolean = Symbol();
const $processUnknown = Symbol();
const $originalSchema = Symbol();
const $schema = Symbol();

export abstract class ModelSchemaPartProcessor<T> {
  protected readonly [$dependencies]: DependencyManager;
  protected readonly [$originalSchema]: Schema;
  protected readonly [$schema]: Schema;

  constructor(schema: Schema, dependencies: DependencyManager) {
    this[$dependencies] = dependencies;
    this[$originalSchema] = schema;
    this[$schema] = isComposedSchema(schema) ? decomposeSchema(schema)[0] : schema;
  }

  process(): T {
    const schema = this[$schema];

    if (isReferenceSchema(schema)) {
      return this[$processReference](schema);
    }

    if (isArraySchema(schema)) {
      return this[$processArray](schema);
    }

    if (isMapSchema(schema)) {
      return this[$processRecord](schema);
    }

    if (isStringSchema(schema)) {
      return this[$processString](schema);
    }

    if (isNumberSchema(schema) || isIntegerSchema(schema)) {
      return this[$processNumber](schema);
    }

    if (isBooleanSchema(schema)) {
      return this[$processBoolean](schema);
    }

    return this[$processUnknown](schema);
  }

  protected abstract [$processArray](schema: ArraySchema): T;
  protected abstract [$processBoolean](schema: BooleanSchema): T;
  protected abstract [$processNumber](schema: IntegerSchema | NumberSchema): T;
  protected abstract [$processRecord](schema: MapSchema): T;
  protected abstract [$processReference](schema: ReferenceSchema): T;
  protected abstract [$processString](schema: StringSchema): T;
  protected abstract [$processUnknown](schema: Schema): T;
}

function handleNullableInternalType(schema: Schema, typeNode: TypeNode): TypeNode {
  return isNullableSchema(schema)
    ? ts.factory.createUnionTypeNode([typeNode, ts.factory.createKeywordTypeNode(ts.SyntaxKind.UndefinedKeyword)])
    : typeNode;
}

class ModelSchemaInternalTypeProcessor extends ModelSchemaPartProcessor<TypeNode> {
  protected override [$processArray](schema: ArraySchema): TypeNode {
    return ts.factory.createTypeReferenceNode(ts.factory.createIdentifier('ReadonlyArray'), [
      handleNullableInternalType(
        schema.items,
        new ModelSchemaInternalTypeProcessor(schema.items, this[$dependencies]).process(),
      ),
    ]);
  }

  protected override [$processBoolean](_: BooleanSchema): TypeNode {
    return ts.factory.createKeywordTypeNode(ts.SyntaxKind.BooleanKeyword);
  }

  protected override [$processNumber](_: IntegerSchema | NumberSchema): TypeNode {
    return ts.factory.createKeywordTypeNode(ts.SyntaxKind.NumberKeyword);
  }

  protected override [$processRecord]({ additionalProperties: props }: MapSchema): TypeNode {
    const valueType =
      typeof props === 'boolean'
        ? ts.factory.createKeywordTypeNode(ts.SyntaxKind.AnyKeyword)
        : handleNullableInternalType(props, new ModelSchemaInternalTypeProcessor(props, this[$dependencies]).process());

    return ts.factory.createTypeReferenceNode(ts.factory.createIdentifier('Record'), [
      ts.factory.createKeywordTypeNode(ts.SyntaxKind.StringKeyword),
      valueType,
    ]);
  }

  protected override [$processReference](schema: ReferenceSchema): TypeNode {
    const { imports, paths } = this[$dependencies];
    const typeName = convertReferenceSchemaToSpecifier(schema);
    const typePath = paths.createRelativePath(convertReferenceSchemaToPath(schema));
    return ts.factory.createTypeReferenceNode(
      imports.default.getIdentifier(typePath) ?? imports.default.add(typePath, typeName, true),
    );
  }

  protected override [$processString](_: StringSchema): TypeNode {
    return ts.factory.createKeywordTypeNode(ts.SyntaxKind.StringKeyword);
  }

  protected override [$processUnknown](_: Schema): TypeNode {
    return ts.factory.createKeywordTypeNode(ts.SyntaxKind.UnknownKeyword);
  }
}

class ModelSchemaIdentifierProcessor extends ModelSchemaPartProcessor<Identifier> {
  override [$processArray](_: ArraySchema): Identifier {
    return importBuiltInFormModel('ArrayModel', this[$dependencies]);
  }

  override [$processBoolean](_: BooleanSchema): Identifier {
    return importBuiltInFormModel('BooleanModel', this[$dependencies]);
  }

  override [$processNumber](_: IntegerSchema | NumberSchema): Identifier {
    return importBuiltInFormModel('NumberModel', this[$dependencies]);
  }

  override [$processRecord](_: MapSchema): Identifier {
    return importBuiltInFormModel('ObjectModel', this[$dependencies]);
  }

  override [$processReference](schema: ReferenceSchema): Identifier {
    const { imports, paths } = this[$dependencies];

    const name = `${convertReferenceSchemaToSpecifier(schema)}Model`;
    const path = paths.createRelativePath(`${convertReferenceSchemaToPath(schema)}Model`);

    return imports.default.getIdentifier(path) ?? imports.default.add(path, name);
  }

  override [$processString](_: StringSchema): Identifier {
    return importBuiltInFormModel('StringModel', this[$dependencies]);
  }

  override [$processUnknown](_: Schema): Identifier {
    return importBuiltInFormModel('ObjectModel', this[$dependencies]);
  }
}

export class ModelSchemaTypeProcessor extends ModelSchemaPartProcessor<TypeReferenceNode> {
  readonly #id: ModelSchemaIdentifierProcessor;

  constructor(schema: Schema, dependencies: DependencyManager) {
    super(schema, dependencies);
    this.#id = new ModelSchemaIdentifierProcessor(schema, dependencies);
  }

  protected override [$processArray](schema: ArraySchema): TypeReferenceNode {
    return ts.factory.createTypeReferenceNode(this.#id[$processArray](schema), [
      new ModelSchemaTypeProcessor(schema.items, this[$dependencies]).process(),
    ]);
  }

  protected override [$processBoolean](schema: BooleanSchema): TypeReferenceNode {
    return ts.factory.createTypeReferenceNode(this.#id[$processBoolean](schema));
  }

  protected override [$processNumber](schema: IntegerSchema | NumberSchema): TypeReferenceNode {
    return ts.factory.createTypeReferenceNode(this.#id[$processNumber](schema));
  }

  protected override [$processRecord](schema: MapSchema): TypeReferenceNode {
    return ts.factory.createTypeReferenceNode(this.#id[$processRecord](schema), [
      new ModelSchemaInternalTypeProcessor(schema, this[$dependencies]).process(),
    ]);
  }

  protected override [$processReference](schema: ReferenceSchema): TypeReferenceNode {
    return ts.factory.createTypeReferenceNode(this.#id[$processReference](schema));
  }

  protected override [$processString](schema: StringSchema): TypeReferenceNode {
    return ts.factory.createTypeReferenceNode(this.#id[$processString](schema));
  }

  protected override [$processUnknown](schema: Schema): TypeReferenceNode {
    return ts.factory.createTypeReferenceNode(this.#id[$processUnknown](schema));
  }
}

export class ModelSchemaExpressionProcessor extends ModelSchemaPartProcessor<readonly Expression[]> {
  readonly #validationConstraintProcessor: ValidationConstraintProcessor;
  readonly #metadataProcessor: MetadataProcessor;

  constructor(schema: Schema, dependencies: DependencyManager) {
    super(schema, dependencies);
    this.#validationConstraintProcessor = new ValidationConstraintProcessor((name) =>
      importBuiltInFormModel(name, dependencies),
    );
    this.#metadataProcessor = new MetadataProcessor();
  }

  override process(): readonly ts.Expression[] {
    const originalSchema = this[$originalSchema];

    let result = super.process();

    const modelOptionsProperties = [
      this.#createValidatorsProperty(originalSchema),
      this.#createMetadataProperty(originalSchema),
    ].filter(Boolean) as PropertyAssignment[];

    if (modelOptionsProperties.length > 0) {
      const optionsObject = ts.factory.createObjectLiteralExpression(modelOptionsProperties);

      result = [...result, optionsObject];
    }

    return [isNullableSchema(originalSchema) ? ts.factory.createTrue() : ts.factory.createFalse(), ...result];
  }

  protected override [$processArray](schema: ArraySchema): readonly Expression[] {
    const model = new ModelSchemaIdentifierProcessor(schema.items, this[$dependencies]).process();

    return [
      createModelBuildingCallback(
        model,
        new ModelSchemaExpressionProcessor(schema.items, this[$dependencies]).process(),
      ),
    ];
  }

  protected override [$processBoolean](_: BooleanSchema): readonly Expression[] {
    return [];
  }

  protected override [$processNumber](_: IntegerSchema | NumberSchema): readonly Expression[] {
    return [];
  }

  protected override [$processRecord](_: MapSchema): readonly Expression[] {
    return [];
  }

  protected override [$processReference](_: ReferenceSchema): readonly Expression[] {
    return [];
  }

  protected override [$processString](_: StringSchema): readonly Expression[] {
    return [];
  }

  protected override [$processUnknown](_: Schema): readonly Expression[] {
    return [];
  }

  #createValidatorsProperty(schema: Schema): PropertyAssignment | null {
    if (!hasValidationConstraints(schema)) {
      return null;
    }

    const constraints = schema['x-validation-constraints'].map((constraint) =>
      this.#validationConstraintProcessor.process(constraint),
    );
    return ts.factory.createPropertyAssignment('validators', ts.factory.createArrayLiteralExpression(constraints));
  }

  #createMetadataProperty(schema: Schema): PropertyAssignment | null {
    const metadata = this.#metadataProcessor.process(schema);
    return metadata ? ts.factory.createPropertyAssignment('meta', metadata) : null;
  }
}
