import { expect } from '@esm-bundle/chai';
import { calculatePath } from '../src/CookieManager.js';

describe('@vaadin/hilla-core', () => {
  describe('CookieManager', () => {
    describe('calculatePath', () => {
      let base: URL;

      beforeEach(() => {
        base = new URL(document.baseURI);
      });

      it('should remove trailing slash', () => {
        expect(calculatePath(new URL('/foo/bar/', base))).to.equal('/foo/bar');
      });

      it('should preserve slash if the path is empty', () => {
        expect(calculatePath(new URL('/', base))).to.equal('/');
      });
    });
  });
});
