import type { NonComposedRegularSchema, Schema } from '@vaadin/hilla-generator-core/Schema.js';
import { template, transform } from '@vaadin/hilla-generator-utils/ast.js';
import ts, {
  type Expression,
  type Identifier,
  type NewExpression,
  type Statement,
  type VariableStatement,
} from 'typescript';

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

function selector<T extends Expression>([statement]: readonly Statement[]): T {
  return (statement as VariableStatement).declarationList.declarations[0].initializer as T;
}

const variableStatementVar = 'const a';

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
