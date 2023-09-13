import { expect, use } from '@esm-bundle/chai';
import type { GridElement } from '@hilla/react-components/Grid.js';
import { render } from '@testing-library/react';
import sinonChai from 'sinon-chai';
import { AutoGrid } from '../src/autogrid.js';
import type { CrudService } from '../src/crud.js';
import type Pageable from '../src/types/dev/hilla/mappedtypes/Pageable.js';
import { getBodyCellContent } from './grid-test-helpers.js';
import { type Person, PersonModel } from './TestModels.js';

use(sinonChai);

const fakeService: CrudService<Person> = {
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

export async function nextFrame(): Promise<void> {
  return new Promise((resolve) => {
    requestAnimationFrame(() => {
      resolve(undefined);
    });
  });
}

describe('@hilla/react-grid', () => {
  beforeEach(() => {});

  function TestAutoGrid() {
    return <AutoGrid service={fakeService} model={PersonModel}></AutoGrid>;
  }
  describe('useAutoGrid', () => {
    it('creates columns based on model', async () => {
      const result = render(<TestAutoGrid />);
      const columns = result.container.querySelectorAll('vaadin-grid-sort-column');
      expect(columns.length).to.equal(2);
      expect(columns[0].path).to.equal('firstName');
      expect(columns[0].header).to.equal('First name');
      expect(columns[1].path).to.equal('lastName');
      expect(columns[1].header).to.equal('Last name');
    });
    it('sets a data provider', async () => {
      const result = render(<TestAutoGrid />);
      const grid = result.container.querySelector('vaadin-grid');
      expect(grid?.dataProvider).to.not.be.undefined;
    });
    it('data provider provides data', async () => {
      const result = render(<TestAutoGrid />);
      const grid: GridElement = result.container.querySelector('vaadin-grid')!;
      await nextFrame();
      // eslint-disable-next-line
      expect((grid as any)._cache.size).to.equal(2);
      expect(getBodyCellContent(grid, 0, 0).innerText).to.equal('John');
      expect(getBodyCellContent(grid, 0, 1).innerText).to.equal('Dove');
      expect(getBodyCellContent(grid, 1, 0).innerText).to.equal('Jane');
      expect(getBodyCellContent(grid, 1, 1).innerText).to.equal('Love');
    });
    it('does not pass its own parameters to the underlying grid', async () => {
      const result = render(<TestAutoGrid />);
      const grid: GridElement = result.container.querySelector('vaadin-grid')!;
      expect(grid.getAttribute('model')).to.be.null;
      expect(grid.getAttribute('service')).to.be.null;
    });
  });
});
