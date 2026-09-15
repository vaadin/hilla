import ts from '@typescript/typescript6';
import { beforeEach, describe, expect, it } from 'vitest';
import createSourceFile from '../src/createSourceFile.js';
import ExportManager from '../src/dependencies/ExportManager.js';

function print(manager: ExportManager): string {
  return ts.createPrinter().printFile(createSourceFile(manager.toCode(), 'foo.ts'));
}

describe('ExportManager', () => {
  let manager: ExportManager;

  beforeEach(() => {
    manager = new ExportManager(new Intl.Collator());
  });

  describe('named', () => {
    it('should reuse the identifier of an already exported name', () => {
      const id = manager.named.add('Model');

      expect(manager.named.add('Model')).to.equal(id);
    });

    it('should suffix a name taken elsewhere', () => {
      manager.names.claim('Model');

      expect(manager.named.add('Model').text).to.equal('Model_1');
    });

    it('should alias an export only when the identifier was suffixed', () => {
      manager.names.claim('Sample');
      manager.named.add('Model');
      manager.named.add('Sample');

      const code = print(manager);

      expect(code).to.contain('Model,');
      expect(code).to.contain('Sample_1 as Sample');
    });
  });

  describe('default', () => {
    it('should take the name as given and claim it', () => {
      expect(manager.default.set('client').text).to.equal('client');
      expect(manager.named.add('client').text).to.equal('client_1');
      expect(print(manager)).to.contain('export default client;');
    });
  });

  describe('namespace', () => {
    it('should reuse the identifier of an already exported path', () => {
      const id = manager.namespace.addCombined('./FooEndpoint.js', 'FooEndpoint');

      expect(manager.namespace.addCombined('./FooEndpoint.js', 'FooEndpoint')).to.equal(id);
      expect(print(manager)).to.contain('export * as FooEndpoint from "./FooEndpoint.js";');
    });
  });

  describe('fromCode', () => {
    it('should keep the local binding of an aliased export', () => {
      const code = `function delete_1() {}
export { delete_1 as delete, get };`;

      manager.fromCode(ts.createSourceFile('foo.ts', code, ts.ScriptTarget.ESNext, true, ts.ScriptKind.TS));

      expect(manager.named.getIdentifier('delete')?.text).to.equal('delete_1');
      expect(manager.named.getIdentifier('get')?.text).to.equal('get');
    });
  });
});
