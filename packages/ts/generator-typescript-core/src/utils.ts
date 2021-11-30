export type Nullified<T, K extends keyof T> = T & { [P in K]: undefined };

export function simplifyFullyQualifiedName(name: string): string {
  return name.substring(name.lastIndexOf(name.includes('$') ? '$' : '.') + 1, name.length);
}

const QUALIFIED_NAME_DELIMITER = /[$.]/g;

export function convertFullyQualifiedNameToRelativePath(name: string): string {
  return `./${name.replace(QUALIFIED_NAME_DELIMITER, '/')}.js`;
}
