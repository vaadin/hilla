import { expect, use } from '@esm-bundle/chai';
import { Grid, GridElement } from '@hilla/react-components/Grid.js';
import { render } from '@testing-library/react';
import sinon from 'sinon';
import sinonChai from 'sinon-chai';
import { useAutoGrid as _useAutoGrid } from '../src/autogrid.js';
import type { CrudEndpoint } from '../src/crud.js';
import Pageable from '../src/types/Pageable.js';
import { Person, PersonModel } from './TestModels.js';
//@ts-ignore
import { getRowBodyCells, getBodyCellContent, getRows, getCellContent } from './grid-test-utils.js';
use(sinonChai);

const fakeEndpoint: CrudEndpoint<Person> = {
  list: async (request: Pageable): Promise<Person[]> => {
    const data: Person[] = [
      { firstName: 'John', lastName: 'Dove' },
      { firstName: 'Jane', lastName: 'Love' },
    ];
    if (request.pageNumber === 0) {
      return data;
    }

    return [];
  },
};

async function sleep(ms: number) {
  return new Promise((resolve) =>
    setTimeout(() => {
      resolve(undefined);
    }, 1),
  );
}
describe('@hilla/react-grid', () => {
  type UseAutoGridSpy = sinon.SinonSpy<Parameters<typeof _useAutoGrid>, ReturnType<typeof _useAutoGrid>>;
  const useAutoGrid = sinon.spy(_useAutoGrid) as typeof _useAutoGrid;

  beforeEach(() => {
    (useAutoGrid as UseAutoGridSpy).resetHistory();
  });

  function AutoGrid() {
    const autoGrid = useAutoGrid(fakeEndpoint, PersonModel);
    return <Grid {...autoGrid}></Grid>;
  }
  describe('useAutoGrid', () => {
    it('creates columns based on model', async () => {
      const result = render(<AutoGrid />);
      const columns = result.container.querySelectorAll('vaadin-grid-sort-column');
      expect(columns.length).to.equal(2);
      expect(columns[0].path).to.equal('firstName');
      expect(columns[0].header).to.equal('First name');
      expect(columns[1].path).to.equal('lastName');
      expect(columns[1].header).to.equal('Last name');
    });
    it('sets a data provider', async () => {
      const result = render(<AutoGrid />);
      const grid = result.container.querySelector('vaadin-grid');
      expect(grid?.dataProvider).to.not.be.undefined;
    });
    it('data provider provides data', async () => {
      const result = render(<AutoGrid />);
      const grid: GridElement = result.container.querySelector('vaadin-grid')!;
      grid.requestContentUpdate();
      await sleep(1);
      expect((grid as any)._cache.size).to.equal(2);
      expect(getBodyCellContent(grid, 0, 0).innerText).to.equal('John');
      expect(getBodyCellContent(grid, 0, 1).innerText).to.equal('Dove');
      expect(getBodyCellContent(grid, 1, 0).innerText).to.equal('Jane');
      expect(getBodyCellContent(grid, 1, 1).innerText).to.equal('Love');
    });
  });
});
function getBodyCellText(grid: GridElement, row: number, col: number): any {
  return getCellContent(getRowBodyCells(getRows(grid.shadowRoot)[row])[col]).innerText;
}
