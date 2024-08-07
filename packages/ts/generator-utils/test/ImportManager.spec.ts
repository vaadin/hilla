import { expect, use } from 'chai';
import chaiLike from 'chai-like';
import ts from 'typescript';
import createSourceFile from '../src/createSourceFile.js';
import ImportManager from '../src/dependencies/ImportManager.js';
import snapshotMatcher from '../src/testing/snapshotMatcher.js';

use(snapshotMatcher);
use(chaiLike);

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

      await expect(printer.printFile(file)).toMatchSnapshot('ImportManager', import.meta.url);
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
});
