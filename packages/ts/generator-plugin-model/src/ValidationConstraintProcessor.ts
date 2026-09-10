import ts, { type CallExpression, type Expression, type Identifier, type Statement } from '@typescript/typescript6';
import type { NonComposedRegularSchema, Schema } from '@vaadin/hilla-generator-core/Schema.js';
import { template, transform } from '@vaadin/hilla-generator-utils/ast.js';

export type ValidationConstrainedSchema = NonComposedRegularSchema &
  Readonly<{ 'x-validation-constraints': readonly ValidationConstraint[] }>;

export function hasValidationConstraints(schema: Schema): schema is ValidationConstrainedSchema {
  return (
    'x-validation-constraints' in schema &&
    (schema as ValidationConstrainedSchema)['x-validation-constraints'].length > 0
  );
}

export interface ValidationConstraint {
  simpleName: string;
  attributes?: Record<string, unknown>;
}

export type ValidationConstraintImporter = (name: string) => Identifier;

/**
 * The kind of model a constraint applies to. `Constraint[$assertSupportedModel]`
 * throws while the generated module is being evaluated when a constraint is
 * applied to a model it does not support, so a constraint the schema cannot
 * satisfy has to be left out rather than emitted.
 *
 * Mirrors the `model()` declarations of `@vaadin/hilla-models/constraints.js`.
 */
export type ModelKind = 'array' | 'boolean' | 'number' | 'object' | 'record' | 'string';

const SUPPORTED_MODELS: Readonly<Partial<Record<string, readonly ModelKind[]>>> = {
  AssertFalse: ['boolean'],
  AssertTrue: ['boolean'],
  DecimalMax: ['number', 'string'],
  DecimalMin: ['number', 'string'],
  Digits: ['number', 'string'],
  Email: ['string'],
  Future: ['string'],
  FutureOrPresent: ['string'],
  Max: ['number'],
  Min: ['number'],
  Negative: ['number'],
  NegativeOrZero: ['number'],
  NotBlank: ['string'],
  NotEmpty: ['array', 'record', 'string'],
  Past: ['string'],
  PastOrPresent: ['string'],
  Pattern: ['string'],
  Positive: ['number'],
  PositiveOrZero: ['number'],
  Size: ['array', 'string'],
};

// `Null` and `NotNull` accept any model, so they are not listed above
const ANY_MODEL = new Set(['Null', 'NotNull']);

/**
 * The constraints the binder cannot map to a validator. Emitting them would
 * make `BinderNode` throw for every node of the model.
 *
 * @see `getConstraintValidator` in `@vaadin/hilla-lit-form/BinderNode.js`
 */
const UNSUPPORTED_BY_BINDER = new Set(['FutureOrPresent', 'PastOrPresent']);

export function isApplicable(constraint: ValidationConstraint, model: ModelKind): boolean {
  if (UNSUPPORTED_BY_BINDER.has(constraint.simpleName)) {
    return false;
  }

  if (ANY_MODEL.has(constraint.simpleName)) {
    return true;
  }

  return SUPPORTED_MODELS[constraint.simpleName]?.includes(model) ?? true;
}

function selector<T extends Expression>([statement]: readonly Statement[]): T {
  return (statement as ts.VariableStatement).declarationList.declarations[0].initializer as T;
}

const variableStatementVar = 'const a';

export class ValidationConstraintProcessor {
  readonly #importer: ValidationConstraintImporter;

  constructor(importer: ValidationConstraintImporter) {
    this.#importer = importer;
  }

  process(constraint: ValidationConstraint): CallExpression {
    return ts.factory.createCallExpression(
      this.#importer(constraint.simpleName),
      undefined,
      constraint.attributes ? [ValidationConstraintProcessor.#processAttributes(constraint.attributes)] : [],
    );
  }

  static #processAttributes(attributes: Record<string, unknown>): Expression {
    const names = Object.keys(attributes);
    const tpl = JSON.stringify(names.includes('value') && names.length === 1 ? attributes.value : attributes);

    return template(`${variableStatementVar}=${tpl}`, selector, [
      transform((node) =>
        ts.isPropertyAssignment(node) && ts.isStringLiteral(node.name)
          ? ts.factory.createPropertyAssignment(node.name.text, node.initializer)
          : node,
      ),
    ]);
  }
}
