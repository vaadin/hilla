import { beforeEach, describe, expect, it } from 'vitest';
import { calculatePath } from '../src/CookieManager.js';

describe('@vaadin/hilla-frontend', () => {
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
