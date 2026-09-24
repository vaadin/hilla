import { describe, expect, it } from 'vitest';
import DependencyManager from '../src/dependencies/DependencyManager.js';
import PathManager from '../src/dependencies/PathManager.js';

describe('DependencyManager', () => {
  it('should share a single name registry between imports and exports', () => {
    const manager = new DependencyManager(new PathManager({ extension: '.js' }));

    expect(manager.imports.names).to.equal(manager.names);
    expect(manager.exports.names).to.equal(manager.names);

    manager.exports.named.add('Model');

    expect(manager.imports.named.add('./Model.js', 'Model').text).to.equal('Model_1');
  });
});
