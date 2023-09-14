import { expect, use } from '@esm-bundle/chai';
import type { GridElement } from '@hilla/react-components/Grid.js';
import { render } from '@testing-library/react';
import sinonChai from 'sinon-chai';
import { AutoGrid, type AutoGridProps } from '../src/autogrid.js';
import type { CrudService } from '../src/crud.js';
import type Filter from '../src/types/dev/hilla/crud/filter/Filter.js';
import Matcher from '../src/types/dev/hilla/crud/filter/PropertyStringFilter/Matcher.js';
import type PropertyStringFilter from '../src/types/dev/hilla/crud/filter/PropertyStringFilter.js';
import type Pageable from '../src/types/dev/hilla/mappedtypes/Pageable.js';
import { getBodyCellContent, getVisibleRowCount } from './grid-test-helpers.js';
import { PersonModel, type Person } from './TestModels.js';

use(sinonChai);

const fakeService: CrudService<Person> = {
  list: async (request: Pageable, filter: Filter | undefined): Promise<Person[]> => {
    const data: Person[] = [
      { firstName: 'John', lastName: 'Dove', email: 'john@example.com' },
      { firstName: 'Jane', lastName: 'Love', email: 'jane@example.com' },
    ];
    if (request.pageNumber === 0) {
      /* eslint-disable */
      if (filter && (filter as any).t === 'propertyString') {
        const propertyFilter: PropertyStringFilter = filter as PropertyStringFilter;
        return data.filter((person) => {
          const propertyValue = (person as any)[propertyFilter.propertyId];
          if (propertyFilter.matcher === 'CONTAINS') {
            return propertyValue.includes(propertyFilter.filterValue);
          }
          return propertyValue === propertyFilter.filterValue;
        });
      }
      /* eslint-enable */
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
  function TestAutoGrid(customProps: Partial<AutoGridProps<Person>>) {
    return <AutoGrid service={fakeService} model={PersonModel} {...customProps}></AutoGrid>;
  }
  describe('useAutoGrid', () => {
    it('creates columns based on model', async () => {
      const result = render(<TestAutoGrid />);
      const columns = result.container.querySelectorAll('vaadin-grid-sort-column');
      expect(columns.length).to.equal(3);
      expect(columns[0].path).to.equal('firstName');
      expect(columns[0].header).to.equal('First name');
      expect(columns[1].path).to.equal('lastName');
      expect(columns[1].header).to.equal('Last name');
      expect(columns[2].path).to.equal('email');
      expect(columns[2].header).to.equal('Email');
    });
    it('sets a data provider, but only once', async () => {
      const result = render(<TestAutoGrid />);
      const grid = result.container.querySelector('vaadin-grid')!;
      const dp = grid.dataProvider;
      expect(dp).to.not.be.undefined;
      result.rerender(<TestAutoGrid />);
      const grid2 = result.container.querySelector('vaadin-grid')!;
      expect(dp).to.equal(grid2.dataProvider);
    });
    it('data provider provides data', async () => {
      const result = render(<TestAutoGrid />);
      await nextFrame();
      const grid: GridElement = result.container.querySelector('vaadin-grid')!;
      expect(getVisibleRowCount(grid)).to.equal(2);
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
    it('passes filter to the data provider', async () => {
      const filter: PropertyStringFilter = { filterValue: 'Jan', matcher: Matcher.CONTAINS, propertyId: 'firstName' };
      // eslint-disable-next-line
      (filter as any).t = 'propertyString'; // Workaround for https://github.com/vaadin/hilla/issues/438

      const result = render(<TestAutoGrid filter={filter} />);
      await nextFrame();
      const grid: GridElement = result.container.querySelector('vaadin-grid')!;
      expect(getVisibleRowCount(grid)).to.equal(1);
      expect(getBodyCellContent(grid, 0, 0).innerText).to.equal('Jane');
      expect(getBodyCellContent(grid, 0, 1).innerText).to.equal('Love');
    });

    describe('customize columns', () => {
      it('should only show configured columns in specified order', () => {
        const result = render(<TestAutoGrid visibleColumns={['email', 'firstName']} />);
        const columns = result.container.querySelectorAll('vaadin-grid-sort-column');
        expect(columns.length).to.equal(2);
        expect(columns[0].path).to.equal('email');
        expect(columns[0].header).to.equal('Email');
        expect(columns[1].path).to.equal('firstName');
        expect(columns[1].header).to.equal('First name');
      });

      it('should ignore unknown columns', () => {
        const result = render(<TestAutoGrid visibleColumns={['foo', 'email', 'bar', 'firstName']} />);
        const columns = result.container.querySelectorAll('vaadin-grid-sort-column');
        expect(columns.length).to.equal(2);
        expect(columns[0].path).to.equal('email');
        expect(columns[0].header).to.equal('Email');
        expect(columns[1].path).to.equal('firstName');
        expect(columns[1].header).to.equal('First name');
      });
    });
  });
});
