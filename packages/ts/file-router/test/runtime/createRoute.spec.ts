import { expect, use } from '@esm-bundle/chai';
import chaiLike from 'chai-like';
import { createRoute, extendModule } from '../../src/runtime/createRoute.js';

use(chaiLike);

describe('@vaadin/hilla-file-router', () => {
  describe('createRoute', () => {
    it('should create a route with a module', () => {
      const route = createRoute('/path', { default: 'module' });

      expect(route).to.be.like({
        path: '/path',
        module: { default: 'module' },
      });
    });

    it('should create a route with children', () => {
      const route = createRoute('/path', [createRoute('/child1'), createRoute('/child2')]);

      expect(route).to.be.like({
        path: '/path',
        children: [{ path: '/child1' }, { path: '/child2' }],
      });
    });
  });

  describe('extendModule', () => {
    it('should extend a module', () => {
      const module = { default: 'module' };
      const extendedModule = extendModule(module, { flowLayout: true });

      expect(extendedModule).to.be.like({
        default: 'module',
        config: { flowLayout: true },
      });
    });
  });
});
