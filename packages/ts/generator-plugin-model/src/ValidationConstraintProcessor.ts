import ts, { type Expression, type Identifier, type NewExpression } from '@typescript/typescript6';
import type { NonComposedRegularSchema, Schema } from '@vaadin/hilla-generator-core/Schema.js';
import { createExpressionFromValue } from './utils.js';

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

export class ValidationConstraintProcessor {
  readonly #importer: ValidationConstraintImporter;

  constructor(importer: ValidationConstraintImporter) {
    this.#importer = importer;
  }

  process(constraint: ValidationConstraint): NewExpression {
    return ts.factory.createNewExpression(
      this.#importer(constraint.simpleName),
      undefined,
      constraint.attributes ? [ValidationConstraintProcessor.#processAttributes(constraint.attributes)] : [],
    );
  }

  static #processAttributes(attributes: Record<string, unknown>): Expression {
    // A sole `value` attribute is the argument of the validator itself
    const names = Object.keys(attributes);

    return createExpressionFromValue(names.length === 1 && names[0] === 'value' ? attributes.value : attributes);
  }
}
