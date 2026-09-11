import ts, { type Identifier } from '@typescript/typescript6';

/**
 * Words that cannot serve as a binding name. Generated files are modules, hence
 * the strict mode ones are in as well, including `arguments` and `eval`, which
 * are not keywords but are equally illegal. Contextual keywords (`type`, `as`,
 * `get`, `string`, ...) are valid identifiers and thus are not listed.
 */
const reservedWords = [
  'arguments',
  'await',
  'break',
  'case',
  'catch',
  'class',
  'const',
  'continue',
  'debugger',
  'default',
  'delete',
  'do',
  'else',
  'enum',
  'eval',
  'export',
  'extends',
  'false',
  'finally',
  'for',
  'function',
  'if',
  'implements',
  'import',
  'in',
  'instanceof',
  'interface',
  'let',
  'new',
  'null',
  'package',
  'private',
  'protected',
  'public',
  'return',
  'static',
  'super',
  'switch',
  'this',
  'throw',
  'true',
  'try',
  'typeof',
  'var',
  'void',
  'while',
  'with',
  'yield',
];

/**
 * Tracks the identifier names taken in a single generated file so that a name
 * is only suffixed when it actually collides.
 *
 * The TypeScript printer cannot do this for us: its own unique-name generator
 * only compares generated names against each other, so a generated import would
 * still collide with a plain declaration in the same file.
 */
export default class NameRegistry {
  // reserved words count as taken, so that a name matching one is suffixed
  readonly #used = new Set(reservedWords);

  /**
   * Marks a name as taken as-is. Used for names the registry does not get to
   * choose, such as a declaration name or an identifier parsed from existing
   * code.
   */
  claim(name: string): void {
    this.#used.add(name);
  }

  /**
   * Takes the given name, appending a counter if it is taken already.
   */
  reserve(name: string): string {
    if (!this.#used.has(name)) {
      this.#used.add(name);
      return name;
    }

    let i = 1;
    let candidate = `${name}_${i}`;

    while (this.#used.has(candidate)) {
      i += 1;
      candidate = `${name}_${i}`;
    }

    this.#used.add(candidate);
    return candidate;
  }

  reserveIdentifier(name: string): Identifier {
    return ts.factory.createIdentifier(this.reserve(name));
  }
}
