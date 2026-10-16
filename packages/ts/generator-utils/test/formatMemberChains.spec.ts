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

    // the indent comes from the enclosing statement, so the inner chain lines
    // up with the outer one rather than with the argument it sits in
    expect(formatMemberChains(code)).to.equal(
      [
        'const X = m',
        '  .object("X")',
        '  .property("a", n',
        '  .object("N")',
        '  .property("b", S)',
        '  .build())',
        '  .build();',
        '',
      ].join('\n'),
    );
  });
});
