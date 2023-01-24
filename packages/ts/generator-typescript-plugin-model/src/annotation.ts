import {
  isNonComposedRegularSchema,
  NonComposedRegularSchema,
  Schema,
} from '@hilla/generator-typescript-core/Schema.js';
import { template, transform } from '@hilla/generator-typescript-utils/ast.js';
import ts, {
  type Expression,
  type Identifier,
  type NewExpression,
  type Statement,
  type VariableStatement,
} from 'typescript';

export type AnnotatedSchema = NonComposedRegularSchema & Readonly<{ 'x-annotations': ReadonlyArray<string> }>;
export type ValidationConstrainedSchema = NonComposedRegularSchema &
  Readonly<{ 'x-validation-constraints': ReadonlyArray<Annotation> }>;

export function isAnnotatedSchema(schema: Schema): schema is AnnotatedSchema {
  return isNonComposedRegularSchema(schema) && 'x-annotations' in schema;
}

export function isValidationConstrainedSchema(schema: Schema): schema is ValidationConstrainedSchema {
  return isNonComposedRegularSchema(schema) && 'x-validation-constraints' in schema;
}

export interface Annotation {
  simpleName: string;
  attributes?: Record<string, unknown>;
}

export type AnnotationImporter = (name: string) => Identifier;

function selector<T extends Expression>([statement]: readonly Statement[]): T {
  return (statement as VariableStatement).declarationList.declarations[0].initializer as T;
}

const variableStatementVar = 'const a';

export class AnnotationParser {
  readonly #importer: AnnotationImporter;

  constructor(importer: AnnotationImporter) {
    this.#importer = importer;
  }

  parse(annotation: string | Annotation) {
    if (typeof annotation === 'string') {
      const nameEndIndex = annotation.indexOf('(');
      const simpleName = nameEndIndex >= 0 ? annotation.slice(0, nameEndIndex) : annotation;
      const id = this.#importer(simpleName);

      return template<NewExpression>(`${variableStatementVar} = new ${annotation}`, selector, [
        transform((node) => (ts.isIdentifier(node) && node.text === simpleName ? id : node)),
      ]);
    }

    return ts.factory.createNewExpression(
      this.#importer(annotation.simpleName),
      undefined,
      annotation.attributes ? [this.#parseAnnotationAttributes(annotation.attributes)] : [],
    );
  }

  #parseAnnotationAttributes(attributes: Record<string, unknown>): Expression {
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
