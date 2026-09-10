import ts, { type CallExpression, type Node, type SourceFile } from '@typescript/typescript6';

/**
 * The number of calls a chain needs before it is worth breaking up. Two links
 * cover `m.object(…).build()`, which reads better on one line; three is where
 * the builder starts declaring properties.
 */
const MIN_LINKS = 3;

const INDENT = '  ';

/**
 * The offset of the dot of a property access, which sits between the end of the
 * object expression and the start of the property name. Comments cannot appear
 * there in printed output, so the first dot found is the right one.
 */
function findDot(source: SourceFile, node: ts.PropertyAccessExpression): number {
  return source.text.indexOf('.', node.expression.end);
}

/**
 * Collects the calls of a chain, from its outermost link inwards, for as long
 * as every link is a call on a property access. A chain interrupted by anything
 * else, such as an element access or a bare property read, ends there.
 */
function collectChain(node: CallExpression): readonly CallExpression[] {
  const links: CallExpression[] = [];

  for (let current: ts.Expression = node; ; ) {
    if (!ts.isCallExpression(current) || !ts.isPropertyAccessExpression(current.expression)) {
      // the head of the chain, e.g. the `m` of `m.object(…)`
      return links;
    }

    links.push(current);
    current = current.expression.expression;
  }
}

/**
 * The column the statement starts at, so that the continuation lines line up
 * under it rather than at the left margin.
 */
function columnOf(source: SourceFile, position: number): number {
  const lineStart = source.text.lastIndexOf('\n', position - 1) + 1;
  return position - lineStart;
}

/**
 * Breaks long fluent chains over several lines, one call per line.
 *
 * The TypeScript printer emits a member chain on a single line and offers no
 * way to influence that: `setStartsOnNewLine` only applies to nodes in a list,
 * and no emit flag covers chains. So the text it produced is parsed back and
 * the line breaks are spliced in at the offsets the syntax tree reports, which
 * keeps string literals — a `Pattern` regexp may contain anything — untouched.
 */
export default function formatMemberChains(code: string, fileName = 'generated.ts'): string {
  const source = ts.createSourceFile(fileName, code, ts.ScriptTarget.Latest, true, ts.ScriptKind.TS);
  const breaks: Array<readonly [offset: number, indent: string]> = [];

  function visit(node: Node): void {
    if (ts.isCallExpression(node)) {
      const links = collectChain(node);

      if (links.length >= MIN_LINKS) {
        const statement = ts.findAncestor(node, ts.isStatement) ?? node;
        const indent = ' '.repeat(columnOf(source, statement.getStart(source))) + INDENT;

        links.forEach((link) => {
          breaks.push([findDot(source, link.expression as ts.PropertyAccessExpression), indent]);
          // an argument may hold a chain of its own; the links themselves must
          // not be walked again, or they would be broken twice
          link.arguments.forEach(visit);
        });

        return;
      }
    }

    ts.forEachChild(node, visit);
  }

  visit(source);

  if (breaks.length === 0) {
    return code;
  }

  return breaks
    .sort(([a], [b]) => b - a)
    .reduce((text, [offset, indent]) => `${text.slice(0, offset)}\n${indent}${text.slice(offset)}`, code);
}
