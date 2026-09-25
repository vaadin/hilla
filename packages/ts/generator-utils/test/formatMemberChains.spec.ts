import ts from '@typescript/typescript6';
import { describe, expect, it } from 'vitest';
import formatMemberChains from '../src/formatMemberChains.js';

describe('formatMemberChains', () => {
  it('should break a chain of three or more calls', () => {
    const code = 'const X = m.object("X").property("a", StringModel).build();\n';

    expect(formatMemberChains(code)).to.equal(
      ['const X = m', '  .object("X")', '  .property("a", StringModel)', '  .build();', ''].join('\n'),
    );
  });

  it('should leave shorter chains alone', () => {
    const twoLinks = 'const X = m.object("X").build();\n';
    const oneLink = 'const X = m.enum(Role, "Role");\n';

    expect(formatMemberChains(twoLinks)).to.equal(twoLinks);
    expect(formatMemberChains(oneLink)).to.equal(oneLink);
  });

  it('should leave an endpoint call alone', () => {
    const code = 'async function get(init) { return client.call("Endpoint", "get", {}, init); }\n';

    expect(formatMemberChains(code)).to.equal(code);
  });

  it('should not touch a chain that only appears inside a string', () => {
    const code = 'const X = m.object("X").property("re", Pattern({ regexp: "a.property(b).build(" })).build();\n';
    const formatted = formatMemberChains(code);

    expect(formatted).to.contain('"a.property(b).build("');
    expect(formatted.split('\n').filter((line) => line.includes('.property('))).to.have.lengthOf(1);
  });

  it('should indent under the statement it belongs to', () => {
    const code = 'function f() {\n    const X = m.object("X").property("a", S).build();\n}\n';

    expect(formatMemberChains(code)).to.equal(
      [
        'function f() {',
        '    const X = m',
        '      .object("X")',
        '      .property("a", S)',
        '      .build();',
        '}',
        '',
      ].join('\n'),
    );
  });

  it('should break a nested chain independently of the one that holds it', () => {
    const code = 'const X = m.object("X").property("a", n.object("N").property("b", S).build()).build();\n';
    const formatted = formatMemberChains(code);

    // both chains break, and the inner one keeps its own deeper indent
    expect(formatted).to.contain('\n  .property("a", n');
    expect(formatted).to.contain('\n  .object("N")');
  });

  it('should only add whitespace', () => {
    const code = 'const X = m.object("X").property("a", S).property("b", S).build();\n';
    const formatted = formatMemberChains(code);

    expect(formatted).to.not.equal(code);
    // removing the inserted breaks gives back the original
    expect(formatted.replaceAll(/\n +\./gu, '.')).to.equal(code);
  });

  it('should produce code the parser accepts', () => {
    const code = 'const X = m.object("X").property("a", S).property("b", S).build();\n';
    const source = ts.createSourceFile('x.ts', formatMemberChains(code), ts.ScriptTarget.Latest, true);

    // `parseDiagnostics` is internal but is the only way to see syntax errors
    // on a standalone source file
    expect((source as unknown as { parseDiagnostics: readonly unknown[] }).parseDiagnostics).to.be.empty;
  });
});
