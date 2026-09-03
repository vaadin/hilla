import ts, { type PropertyName } from '@typescript/typescript6';

const IDENTIFIER_PATTERN = /^[A-Za-z_$][A-Za-z0-9_$]*$/u;

export function propertyNameToString(node: PropertyName): string | null {
  if (ts.isIdentifier(node) || ts.isStringLiteral(node) || ts.isNumericLiteral(node)) {
    return node.text;
  }
  return null;
}

/**
 * Creates a property name node, quoting it when it is not a valid identifier:
 * the default discriminator property of Jackson is `@type`.
 */
export function createPropertyName(name: string): PropertyName {
  return IDENTIFIER_PATTERN.test(name) ? ts.factory.createIdentifier(name) : ts.factory.createStringLiteral(name);
}
