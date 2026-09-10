import ts, { type CallExpression, type Expression, type Identifier, type Statement } from '@typescript/typescript6';
import type { NonComposedRegularSchema, Schema } from '@vaadin/hilla-generator-core/Schema.js';
import { template, transform } from '@vaadin/hilla-generator-utils/ast.js';
import * as models from '@vaadin/hilla-models';

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
 * The kind of model the emitter produces for a schema, and a model of that kind
 * to try a constraint against.
 */
const PROBES = {
  array: models.ArrayModel,
  boolean: models.BooleanModel,
  number: models.NumberModel,
  // a `$ref` property gets the referenced model, which derives from ObjectModel
  object: models.ObjectModel,
  record: models.RecordModel,
  string: models.StringModel,
  // a property the parser could not describe gets the bare Model
  unknown: models.Model,
} as const;

export type ModelKind = keyof typeof PROBES;

/**
 * The constraint declarations of the model library, by the name the parser uses
 * for them. Collected from the exports rather than listed, so that a constraint
 * added to the library needs no change here: a declaration is a function
 * carrying the assertion, and it already knows its own name.
 */
function isDeclaration(value: unknown): value is models.NonAttributedConstraint {
  return typeof value === 'function' && models.$assertSupportedModel in value;
}

const DECLARATIONS = new Map<string, models.NonAttributedConstraint>(
  (Object.values(models) as readonly unknown[])
    .filter(isDeclaration)
    .map((declaration) => [declaration.name, declaration]),
);

/**
 * The constraints the binder cannot map to a validator. Not derivable from the
 * model library, which accepts both on a string: it is lit-form that has no
 * client-side implementation for them, and `getConstraintValidator` throws on an
 * unmapped constraint from the `BinderNode` constructor, taking the whole form
 * down with it.
 *
 * @see `Validators.ts`, where the omission is documented
 */
const UNSUPPORTED_BY_BINDER = new Set(['FutureOrPresent', 'PastOrPresent']);

/**
 * Whether a constraint can be applied to the model of a schema of this kind.
 *
 * Asks the constraint itself rather than repeating the `model()` declarations of
 * `@vaadin/hilla-models/constraints.js`: `$assertSupportedModel` is the very
 * check that would otherwise throw while the generated module is evaluated.
 */
export function isApplicable(constraint: ValidationConstraint, kind: ModelKind): boolean {
  if (UNSUPPORTED_BY_BINDER.has(constraint.simpleName)) {
    return false;
  }

  const declaration = DECLARATIONS.get(constraint.simpleName);

  if (!declaration) {
    // emitting a name the model library does not export would break the
    // compilation of the generated file, which is worse than losing a check
    return false;
  }

  try {
    declaration[models.$assertSupportedModel](PROBES[kind]);
    return true;
  } catch {
    return false;
  }
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
