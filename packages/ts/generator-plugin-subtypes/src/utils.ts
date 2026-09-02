import ts, { type PropertyName, type TypeNode } from '@typescript/typescript6';
import type { ReferenceSchema } from '@vaadin/hilla-generator-core/Schema.js';

const SCHEMA_PREFIX = '#/components/schemas/';

/**
 * The discriminator of a union type: the name of the property that holds it,
 * and the value of each subtype that does not declare the property itself,
 * mapped by schema name.
 */
export type Discriminator = Readonly<{
  propertyName: string;
  openTypes: ReadonlyMap<string, string>;
}>;
const IDENTIFIER_PATTERN = /^[A-Za-z_$][A-Za-z0-9_$]*$/u;

export function propertyNameToString(node: PropertyName): string | null {
  if (ts.isIdentifier(node) || ts.isStringLiteral(node) || ts.isNumericLiteral(node)) {
    return node.text;
  }
  return null;
}

export function schemaKey(schema: ReferenceSchema): string {
  return schema.$ref.substring(SCHEMA_PREFIX.length);
}

/**
 * Creates a property name node, quoting it when it is not a valid identifier:
 * the default discriminator property of Jackson is `@type`.
 */
export function createPropertyName(name: string): PropertyName {
  return IDENTIFIER_PATTERN.test(name) ? ts.factory.createIdentifier(name) : ts.factory.createStringLiteral(name);
}

/**
 * Creates the property signature that pins the discriminator to the value of a
 * single subtype, e. g. `"@type": "add"`.
 */
export function createDiscriminatorProperty(propertyName: string, typeValue: string): ts.PropertySignature {
  return ts.factory.createPropertySignature(
    undefined,
    createPropertyName(propertyName),
    undefined,
    ts.factory.createLiteralTypeNode(ts.factory.createStringLiteral(typeValue)),
  );
}

/**
 * Creates a type that pins the discriminator of an otherwise open type, e. g.
 * `(BaseEvent & { "@type": "base" })`.
 */
export function createDiscriminatedType(type: TypeNode, propertyName: string, typeValue: string): TypeNode {
  return ts.factory.createParenthesizedType(
    ts.factory.createIntersectionTypeNode([
      type,
      ts.factory.createTypeLiteralNode([createDiscriminatorProperty(propertyName, typeValue)]),
    ]),
  );
}

/**
 * Removes the imports that are not referenced anymore, which happens when the
 * discriminator property was the only user of a model type.
 */
export function removeUnusedImports(statements: readonly ts.Statement[]): readonly ts.Statement[] {
  const used = new Set<string>();

  function collect(node: ts.Node): void {
    if (ts.isIdentifier(node)) {
      used.add(node.text);
    }

    ts.forEachChild(node, collect);
  }

  statements.filter((statement) => !ts.isImportDeclaration(statement)).forEach(collect);

  function keepBindings(bindings: ts.NamedImportBindings | undefined): ts.NamedImportBindings | undefined {
    if (!bindings || !ts.isNamedImports(bindings)) {
      return bindings;
    }

    const elements = bindings.elements.filter((element) => used.has(element.name.text));

    return elements.length === 0 ? undefined : ts.factory.updateNamedImports(bindings, elements);
  }

  return statements.flatMap((statement) => {
    if (!ts.isImportDeclaration(statement) || !statement.importClause) {
      return [statement];
    }

    const { name, namedBindings, phaseModifier } = statement.importClause;
    const keptName = name && used.has(name.text) ? name : undefined;
    const keptBindings = keepBindings(namedBindings);

    if (keptName === name && keptBindings === namedBindings) {
      return [statement];
    }

    if (!keptName && !keptBindings) {
      return [];
    }

    return [
      ts.factory.updateImportDeclaration(
        statement,
        statement.modifiers,
        ts.factory.updateImportClause(statement.importClause, phaseModifier, keptName, keptBindings),
        statement.moduleSpecifier,
        statement.attributes,
      ),
    ];
  });
}
