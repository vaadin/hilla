/* eslint-disable symbol-description */
import {
  ArraySchema,
  BooleanSchema,
  convertReferenceSchemaToPath,
  convertReferenceSchemaToSpecifier,
  decomposeSchema,
  IntegerSchema,
  isArraySchema,
  isBooleanSchema,
  isComposedSchema,
  isIntegerSchema,
  isMapSchema,
  isNullableSchema,
  isNumberSchema,
  isReferenceSchema,
  isStringSchema,
  MapSchema,
  NumberSchema,
  ReferenceSchema,
  Schema,
  StringSchema,
} from '@hilla/generator-typescript-core/Schema.js';
import type DependencyManager from '@hilla/generator-typescript-utils/dependencies/DependencyManager';
import type { Expression, Identifier, TypeNode, TypeReferenceNode } from 'typescript';
import ts from 'typescript';
import {
  AnnotatedSchema,
  Annotation,
  AnnotationNamedAttributes,
  AnnotationPrimitiveAttribute,
  isAnnotatedSchema,
  isValidationConstrainedSchema,
  ValidationConstrainedSchema,
} from './Annotation.js';
import parseAnnotation from './parseAnnotation.js';
import { importBuiltInFormModel } from './utils.js';

function convertAttribute(attribute: AnnotationPrimitiveAttribute): Expression {
  switch (typeof attribute) {
    case 'boolean':
      return attribute ? ts.factory.createTrue() : ts.factory.createFalse();
    case 'number':
      return ts.factory.createNumericLiteral(attribute);
    case 'string':
      return ts.factory.createStringLiteral(attribute);
    default:
      return ts.factory.createOmittedExpression();
  }
}

function convertNamedAttributes(attributes: AnnotationNamedAttributes): Expression {
  const attributeEntries = Object.entries(attributes);
  if (attributeEntries.length === 1 && attributeEntries[0][0] === 'value') {
    return convertAttribute(attributeEntries[0][1]);
  }

  return ts.factory.createObjectLiteralExpression(
    attributeEntries.map(([key, value]) => ts.factory.createPropertyAssignment(key, convertAttribute(value))),
    false,
  );
}

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

export type OptionalChecker = (schema: Schema) => boolean;

export abstract class ModelSchemaPartProcessor<T> {
  readonly [$dependencies]: DependencyManager;
  readonly [$originalSchema]: Schema;
  readonly [$schema]: Schema;

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
  protected abstract [$processNumber](schema: NumberSchema | IntegerSchema): T;
  protected abstract [$processRecord](schema: MapSchema): T;
  protected abstract [$processReference](schema: ReferenceSchema): T;
  protected abstract [$processString](schema: StringSchema): T;
  protected abstract [$processUnknown](schema: Schema): T;
}

class ModelSchemaInternalTypeProcessor extends ModelSchemaPartProcessor<TypeNode> {
  protected override [$processArray](schema: ArraySchema): TypeNode {
    return ts.factory.createTypeReferenceNode(ts.factory.createIdentifier('ReadonlyArray'), [
      new ModelSchemaInternalTypeProcessor(schema.items, this[$dependencies]).process(),
    ]);
  }

  protected override [$processBoolean](_: BooleanSchema): TypeNode {
    return ts.factory.createKeywordTypeNode(ts.SyntaxKind.BooleanKeyword);
  }

  protected override [$processNumber](_: NumberSchema | IntegerSchema): TypeNode {
    return ts.factory.createKeywordTypeNode(ts.SyntaxKind.NumberKeyword);
  }

  protected override [$processRecord]({ additionalProperties: props }: MapSchema): TypeNode {
    const valueType =
      typeof props === 'boolean'
        ? ts.factory.createKeywordTypeNode(ts.SyntaxKind.AnyKeyword)
        : new ModelSchemaInternalTypeProcessor(props, this[$dependencies]).process();

    return ts.factory.createTypeReferenceNode(ts.factory.createIdentifier('Record'), [
      ts.factory.createKeywordTypeNode(ts.SyntaxKind.StringKeyword),
      valueType,
    ]);
  }

  protected override [$processReference](schema: ReferenceSchema): TypeNode {
    const { paths, imports } = this[$dependencies];
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

  override [$processNumber](_: NumberSchema | IntegerSchema): Identifier {
    return importBuiltInFormModel('NumberModel', this[$dependencies]);
  }

  override [$processRecord](_: MapSchema): Identifier {
    return importBuiltInFormModel('ObjectModel', this[$dependencies]);
  }

  override [$processReference](schema: ReferenceSchema): Identifier {
    const { paths, imports } = this[$dependencies];

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
      new ModelSchemaInternalTypeProcessor(schema.items, this[$dependencies]).process(),
      new ModelSchemaTypeProcessor(schema.items, this[$dependencies]).process(),
    ]);
  }

  protected override [$processBoolean](schema: BooleanSchema): TypeReferenceNode {
    return ts.factory.createTypeReferenceNode(this.#id[$processBoolean](schema));
  }

  protected override [$processNumber](schema: NumberSchema | IntegerSchema): TypeReferenceNode {
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
  readonly #checkOptional: OptionalChecker;

  constructor(schema: Schema, dependencies: DependencyManager, checkOptional: OptionalChecker = isNullableSchema) {
    super(schema, dependencies);
    this.#checkOptional = checkOptional;
  }

  public override process(): readonly ts.Expression[] {
    const schema = this[$schema];

    let result = super.process();

    if (isAnnotatedSchema(schema)) {
      result = [...result, ...this.#getValidatorsFromAnnotations(schema)];
    }

    if (isValidationConstrainedSchema(schema)) {
      result = [...result, ...this.#getValidatorsFromValidationConstraints(schema)];
    }

    return [this.#checkOptional(this[$originalSchema]) ? ts.factory.createTrue() : ts.factory.createFalse(), ...result];
  }

  protected override [$processArray](schema: ArraySchema): readonly Expression[] {
    return [
      new ModelSchemaIdentifierProcessor(schema.items, this[$dependencies]).process(),
      ts.factory.createArrayLiteralExpression(
        new ModelSchemaExpressionProcessor(schema.items, this[$dependencies]).process(),
      ),
    ];
  }

  protected override [$processBoolean](_: BooleanSchema): readonly Expression[] {
    return [];
  }

  protected override [$processNumber](_: NumberSchema | IntegerSchema): readonly Expression[] {
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

  #getValidator = (annotation: Annotation): Expression =>
    ts.factory.createNewExpression(
      importBuiltInFormModel(annotation.simpleName, this[$dependencies]),
      undefined,
      annotation.attributes !== undefined ? [convertNamedAttributes(annotation.attributes)] : [],
    );

  #getValidatorsFromAnnotations(schema: AnnotatedSchema): readonly Expression[] {
    return schema['x-annotations'].map(parseAnnotation).map(this.#getValidator);
  }

  #getValidatorsFromValidationConstraints(schema: ValidationConstrainedSchema): readonly Expression[] {
    return schema['x-validation-constraints'].map(this.#getValidator);
  }
}
