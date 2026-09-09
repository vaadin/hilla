import ts from '@typescript/typescript6';
import chaiLike from 'chai-like';
import { beforeEach, chai, describe, expect, it } from 'vitest';
import createSourceFile from '../src/createSourceFile.js';
import ImportManager from '../src/dependencies/ImportManager.js';

chai.use(chaiLike);

describe('ImportManager', () => {
  describe('default', () => {
    let manager: ImportManager;

    beforeEach(() => {
      manager = new ImportManager(new Intl.Collator());
      manager.named.add('@vaadin/hilla-frontend', 'EndpointRequestInit');
      manager.default.add('@vaadin/hilla-generator-plugin-client', 'client');
      manager.namespace.add('Frontend/generated/FooEndpoint', 'FooEndpoint');
    });

    it('should convert imports to a code', async () => {
      const code = manager.toCode();
      const file = createSourceFile(code, 'foo.ts');
      const printer = ts.createPrinter();

      await expect(printer.printFile(file)).toMatchFileSnapshot('fixtures/ImportManager.snap.ts');
    });

    it('should extract imports from a code', () => {
      const code = `import { EndpointRequestInit } from "@vaadin/hilla-frontend";
import client from "@vaadin/hilla-generator-plugin-client";
import * as FooEndpoint from "Frontend/generated/FooEndpoint";`;

      manager = new ImportManager(new Intl.Collator());
      manager.fromCode(ts.createSourceFile('foo.ts', code, ts.ScriptTarget.ESNext, true, ts.ScriptKind.TS));

      expect(manager.named[Symbol.iterator]().next().value).to.be.like([
        '@vaadin/hilla-frontend',
        'EndpointRequestInit',
        { text: 'EndpointRequestInit' },
        false,
      ]);
      expect(manager.default[Symbol.iterator]().next().value).to.be.like([
        '@vaadin/hilla-generator-plugin-client',
        { text: 'client' },
        false,
      ]);
      expect(manager.namespace[Symbol.iterator]().next().value).to.be.like([
        'Frontend/generated/FooEndpoint',
        { text: 'FooEndpoint' },
      ]);
    });
  });

  describe('names', () => {
    let manager: ImportManager;

    beforeEach(() => {
      manager = new ImportManager(new Intl.Collator());
    });

    it('should suffix only the colliding specifiers', () => {
      expect(manager.named.add('foo', 'Model').text).to.equal('Model');
      expect(manager.named.add('bar', 'Model').text).to.equal('Model_1');
      expect(manager.default.add('baz', 'Model').text).to.equal('Model_2');
    });

    it('should reuse the identifier of an already imported specifier', () => {
      const id = manager.named.add('foo', 'Model');

      expect(manager.named.add('foo', 'Model')).to.equal(id);
    });

    it('should keep names claimed elsewhere out of reach', () => {
      manager.names.claim('Model');

      expect(manager.named.add('foo', 'Model').text).to.equal('Model_1');
    });

    it('should alias a specifier only when it was suffixed', () => {
      manager.named.add('foo', 'Model');
      manager.named.add('bar', 'Model');

      const printer = ts.createPrinter();
      const code = printer.printFile(createSourceFile(manager.toCode(), 'foo.ts'));

      expect(code).to.contain('import { Model } from "foo";');
      expect(code).to.contain('import { Model as Model_1 } from "bar";');
    });

    it('should not hand out a name that the parsed code already uses', () => {
      const code = `import { Model } from "foo";
const Sample = 1;`;

      manager.fromCode(ts.createSourceFile('foo.ts', code, ts.ScriptTarget.ESNext, true, ts.ScriptKind.TS));

      expect(manager.named.add('bar', 'Model').text).to.equal('Model_1');
      expect(manager.named.add('bar', 'Sample').text).to.equal('Sample_1');
    });
  });
});
