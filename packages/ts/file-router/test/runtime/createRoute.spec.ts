import chaiLike from 'chai-like';
import { expect, chai, describe, it } from 'vitest';
import { createRoute, extendModule } from '../../src/runtime/createRoute.js';

chai.use(chaiLike);

describe('@vaadin/hilla-file-router', () => {
  describe('createRoute', () => {
    it('should create a route with a component and config', () => {
      const dummyComponent = () => null;
      const dummyConfig = { flowLayout: true };
      const route = createRoute('/path', dummyComponent, dummyConfig);

      expect(route).to.be.like({
        path: '/path',
        component: dummyComponent,
        config: dummyConfig,
      });
    });

    it('should create a route with a module (deprecated)', () => {
      const dummyModule = { default: () => null };

      const route = createRoute('/path', dummyModule);

      expect(route).to.be.like({
        path: '/path',
        module: dummyModule,
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

  describe('extendModule (deprecated)', () => {
    it('should extend a module', () => {
      const dummyComponent = () => null;
      const module = { default: dummyComponent };
      const extendedModule = extendModule(module, { flowLayout: true });

      expect(extendedModule).to.be.like({
        default: dummyComponent,
        config: { flowLayout: true },
      });
    });

    it('should prefer the original module config over the extension', () => {
      const dummyComponent = () => null;
      const module = { default: dummyComponent, config: { flowLayout: false } };
      const extendedModule = extendModule(module, { flowLayout: true });

      expect(extendedModule).to.be.like({
        default: dummyComponent,
        config: { flowLayout: false },
      });
    });
  });
});
