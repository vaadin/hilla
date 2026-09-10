import ts, { type Expression, type TypeNode } from '@typescript/typescript6';
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
} from '@vaadin/hilla-generator-core/Schema.js';
import type DependencyManager from '@vaadin/hilla-generator-utils/dependencies/DependencyManager.js';
import { type Cycles, schemaName } from './cycles.js';
import { process as processMetadata } from './MetadataProcessor.js';
import { createModelCall, createModelProvider, importM, importModel } from './utils.js';
import {
  hasValidationConstraints,
  isApplicable,
  type ModelKind,
  ValidationConstraintProcessor,
} from './ValidationConstraintProcessor.js';

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

/**
 * The raw TypeScript type of a value, needed where the builder cannot infer it
 * from the model, i.e. the type argument of the object model.
 */
export class ModelSchemaInternalTypeProcessor extends ModelSchemaPartProcessor<TypeNode> {
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

/**
 * Builds the model of a property, i.e. the second argument of `.property()`.
 *
 * The converters wrap the base model from the inside out: the base is wrapped
 * by `m.array` or `m.record`, then by `m.optional`, then by `m.constrained` and
 * finally by `m.meta`.
 */
export class ModelSchemaExpressionProcessor extends ModelSchemaPartProcessor<Expression> {
  readonly #constraints: ValidationConstraintProcessor;
  readonly #cycles: Cycles;
  readonly #owner: string;

  constructor(schema: Schema, dependencies: DependencyManager, owner: string, cycles: Cycles) {
    super(schema, dependencies);
    this.#owner = owner;
    this.#cycles = cycles;
    this.#constraints = new ValidationConstraintProcessor((name) => importModel(name, dependencies));
  }

  override process(): Expression {
    const originalSchema = this[$originalSchema];
    let expression = super.process();

    if (isNullableSchema(originalSchema)) {
      expression = this.#call('optional', [expression]);
    }

    const constraints = this.#createConstraints(originalSchema);

    if (constraints.length > 0) {
      expression = this.#call('constrained', [expression, ...constraints]);
    }

    const metadata = processMetadata(originalSchema);

    if (metadata) {
      expression = this.#call('meta', [expression, metadata]);
    }

    return expression;
  }

  protected override [$processArray](schema: ArraySchema): Expression {
    return this.#call('array', [this.#nested(schema.items)]);
  }

  protected override [$processBoolean](_: BooleanSchema): Expression {
    return importModel('BooleanModel', this[$dependencies]);
  }

  protected override [$processNumber](_: IntegerSchema | NumberSchema): Expression {
    return importModel('NumberModel', this[$dependencies]);
  }

  protected override [$processRecord]({ additionalProperties: props }: MapSchema): Expression {
    const value = typeof props === 'boolean' ? importModel('Model', this[$dependencies]) : this.#nested(props);

    return this.#call('record', [value]);
  }

  protected override [$processReference](schema: ReferenceSchema): Expression {
    const { imports, paths } = this[$dependencies];

    const target = schemaName(schema);

    // a property of the very type that declares it resolves against the owner
    // model, which needs no import and, unlike a deferred reference, keeps the
    // type of the model inferable
    if (target === this.#owner) {
      return ts.factory.createPropertyAccessExpression(importM(this[$dependencies]), 'self');
    }

    const name = `${convertReferenceSchemaToSpecifier(schema)}Model`;
    const path = paths.createRelativePath(`${convertReferenceSchemaToPath(schema)}Model`);
    const model = imports.default.getIdentifier(path) ?? imports.default.add(path, name);

    return this.#cycles.isDeferred(this.#owner, target) ? this.#call('lazy', [createModelProvider(model)]) : model;
  }

  protected override [$processString](_: StringSchema): Expression {
    return importModel('StringModel', this[$dependencies]);
  }

  protected override [$processUnknown](_: Schema): Expression {
    return importModel('Model', this[$dependencies]);
  }

  #call(name: string, args: readonly Expression[]): Expression {
    return createModelCall(this[$dependencies], name, args);
  }

  #nested(schema: Schema): Expression {
    return new ModelSchemaExpressionProcessor(schema, this[$dependencies], this.#owner, this.#cycles).process();
  }

  #createConstraints(schema: Schema): readonly Expression[] {
    if (!hasValidationConstraints(schema)) {
      return [];
    }

    const kind = this.#modelKind();

    return schema['x-validation-constraints']
      .filter((constraint) => isApplicable(constraint, kind))
      .map((constraint) => this.#constraints.process(constraint));
  }

  /**
   * Mirrors the dispatch of {@link ModelSchemaPartProcessor.process}, so that a
   * constraint is tried against the same kind of model the emitter produces.
   */
  #modelKind(): ModelKind {
    const schema = this[$schema];

    if (isReferenceSchema(schema)) {
      return 'object';
    }

    if (isArraySchema(schema)) {
      return 'array';
    }

    if (isMapSchema(schema)) {
      return 'record';
    }

    if (isNumberSchema(schema) || isIntegerSchema(schema)) {
      return 'number';
    }

    if (isBooleanSchema(schema)) {
      return 'boolean';
    }

    if (isStringSchema(schema)) {
      return 'string';
    }

    return 'unknown';
  }
}
