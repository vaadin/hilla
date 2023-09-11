import { expect, use } from '@esm-bundle/chai';
import sinon from 'sinon';
import sinonChai from 'sinon-chai';
import { useAutoGrid as _useAutoGrid } from '../src/autogrid.js';

use(sinonChai);

describe('@hilla/react-form', () => {
  type UseAutoGridSpy = sinon.SinonSpy<Parameters<typeof _useAutoGrid>, ReturnType<typeof _useAutoGrid>>;
  const useAutoGrid = sinon.spy(_useAutoGrid) as typeof _useAutoGrid;

  beforeEach(() => {
    (useAutoGrid as UseAutoGridSpy).resetHistory();
  });

  describe('useAutoGrid', () => {
    it('exists', async () => {
      expect(useAutoGrid()).to.equal('foo');
    });
  });
});
