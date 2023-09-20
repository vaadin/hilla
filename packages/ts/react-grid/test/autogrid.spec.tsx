import { expect, use } from '@esm-bundle/chai';
import type { GridElement } from '@hilla/react-components/Grid.js';
import type { TextFieldElement } from '@hilla/react-components/TextField.js';
import { render } from '@testing-library/react';
import sinonChai from 'sinon-chai';
import { AutoGrid, type AutoGridProps } from '../src/autogrid.js';
import type { CrudService } from '../src/crud.js';
import type AndFilter from '../src/types/dev/hilla/crud/filter/AndFilter.js';
import type Filter from '../src/types/dev/hilla/crud/filter/Filter.js';
import type PropertyStringFilter from '../src/types/dev/hilla/crud/filter/PropertyStringFilter.js';
import Matcher from '../src/types/dev/hilla/crud/filter/PropertyStringFilter/Matcher.js';
import type Pageable from '../src/types/dev/hilla/mappedtypes/Pageable.js';
import Direction from '../src/types/org/springframework/data/domain/Sort/Direction.js';
import { PersonModel, type Person } from './TestModels.js';
import { getBodyCellContent, getHeaderCellContent, getVisibleRowCount } from './grid-test-helpers.js';

use(sinonChai);
let lastFilter: Filter | undefined;

const fakeService: CrudService<Person> = {
  list: async (request: Pageable, filter: Filter | undefined): Promise<Person[]> => {
    lastFilter = filter;
    let data: Person[] = [
      { firstName: 'John', lastName: 'Dove', email: 'john@example.com', someNumber: 12 },
      { firstName: 'Jane', lastName: 'Love', email: 'jane@example.com', someNumber: 55 },
    ];
    if (request.pageNumber === 0) {
      /* eslint-disable */
      if (filter && (filter as any).t === 'propertyString') {
        const propertyFilter: PropertyStringFilter = filter as PropertyStringFilter;
        data = data.filter((person) => {
          const propertyValue = (person as any)[propertyFilter.propertyId];
          if (propertyFilter.matcher === 'CONTAINS') {
            return propertyValue.includes(propertyFilter.filterValue);
          }
          return propertyValue === propertyFilter.filterValue;
        });
      }
      /* eslint-enable */
    } else {
      data = [];
    }

    if (request.sort.orders.length === 1) {
      const sortPropertyId = request.sort.orders[0]!.property;
      const directionMod = request.sort.orders[0]!.direction === Direction.ASC ? 1 : -1;
      data.sort((a, b) =>
        // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
        (a as any)[sortPropertyId] > (b as any)[sortPropertyId] ? Number(directionMod) : -1 * directionMod,
      );
    }
    return data;
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
  describe('Auto grid', () => {
    it('creates columns based on model', async () => {
      const result = render(<TestAutoGrid />);
      const columns = result.container.querySelectorAll('vaadin-grid-sort-column');
      expect(columns.length).to.equal(4);
      expect(columns[0].path).to.equal('firstName');
      expect(columns[0].header).to.equal('First name');
      expect(columns[1].path).to.equal('lastName');
      expect(columns[1].header).to.equal('Last name');
      expect(columns[2].path).to.equal('email');
      expect(columns[2].header).to.equal('Email');
      expect(columns[3].path).to.equal('someNumber');
      expect(columns[3].header).to.equal('Some number');
    });
    it('creates sortable columns', async () => {
      const result = render(<TestAutoGrid />);
      const grid: GridElement = result.container.querySelector('vaadin-grid')!;
      await nextFrame();
      await nextFrame();
      const firstNameSorter = getHeaderCellContent(grid, 0, 0).firstElementChild as HTMLElement;
      firstNameSorter.click();
      await nextFrame();
      expect(getBodyCellContent(grid, 0, 0).innerText).to.equal('Jane');
      expect(getBodyCellContent(grid, 1, 0).innerText).to.equal('John');
      firstNameSorter.click();
      await nextFrame();
      expect(getBodyCellContent(grid, 0, 0).innerText).to.equal('John');
      expect(getBodyCellContent(grid, 1, 0).innerText).to.equal('Jane');
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

    describe('header filters', () => {
      it('created for string columns', async () => {
        const result = render(<TestAutoGrid headerFilters />);
        await nextFrame();
        await nextFrame();
        const grid: GridElement = result.container.querySelector('vaadin-grid')!;
        const cell = getHeaderCellContent(grid, 0, 0);
        expect(cell.firstElementChild?.localName).to.equal('vaadin-text-field');
      });
      it('no filters created for other columns', async () => {
        const result = render(<TestAutoGrid headerFilters />);
        await nextFrame();
        await nextFrame();
        const grid: GridElement = result.container.querySelector('vaadin-grid')!;
        const cell = getHeaderCellContent(grid, 0, 3);
        expect(cell.firstElementChild).to.null;
      });
      it('filter when you type in the field for a string column', async () => {
        const result = render(<TestAutoGrid headerFilters />);
        await nextFrame();
        await nextFrame();
        const grid: GridElement = result.container.querySelector('vaadin-grid')!;
        const firstNameFilterField = getHeaderCellContent(grid, 0, 0).firstElementChild as TextFieldElement;
        firstNameFilterField.value = 'filter-value';
        firstNameFilterField.dispatchEvent(new CustomEvent('input'));

        const expectedPropertyFilter: PropertyStringFilter = {
          ...{ t: 'propertyString' },
          filterValue: 'filter-value',
          propertyId: 'firstName',
          matcher: Matcher.CONTAINS,
        };
        const expectedFilter: AndFilter = { ...{ t: 'and' }, children: [expectedPropertyFilter] };
        expect(lastFilter).to.eql(expectedFilter);
      });
      it('combine filters (and) when you type in multiple fields', async () => {
        const result = render(<TestAutoGrid headerFilters />);
        await nextFrame();
        await nextFrame();
        const grid: GridElement = result.container.querySelector('vaadin-grid')!;
        const firstNameFilterField = getHeaderCellContent(grid, 0, 0).firstElementChild as TextFieldElement;
        firstNameFilterField.value = 'filterFirst';
        firstNameFilterField.dispatchEvent(new CustomEvent('input'));

        const lastNameFilterField = getHeaderCellContent(grid, 0, 1).firstElementChild as TextFieldElement;
        lastNameFilterField.value = 'filterLast';
        lastNameFilterField.dispatchEvent(new CustomEvent('input'));

        const expectedFirstNameFilter: PropertyStringFilter = {
          ...{ t: 'propertyString' },
          filterValue: 'filterFirst',
          propertyId: 'firstName',
          matcher: Matcher.CONTAINS,
        };
        const expectedLastNameFilter: PropertyStringFilter = {
          ...{ t: 'propertyString' },
          filterValue: 'filterLast',
          propertyId: 'lastName',
          matcher: Matcher.CONTAINS,
        };
        const expectedFilter: AndFilter = {
          ...{ t: 'and' },
          children: [expectedFirstNameFilter, expectedLastNameFilter],
        };
        expect(lastFilter).to.eql(expectedFilter);
      });
    });
    it('removes the filters when you clear the fields', async () => {
      const result = render(<TestAutoGrid headerFilters />);
      await nextFrame();
      await nextFrame();
      const grid: GridElement = result.container.querySelector('vaadin-grid')!;
      const firstNameFilter = getHeaderCellContent(grid, 0, 0).firstElementChild as TextFieldElement;
      const lastNameFilter = getHeaderCellContent(grid, 0, 1).firstElementChild as TextFieldElement;
      firstNameFilter.value = 'filterFirst';
      lastNameFilter.value = 'filterLast';
      firstNameFilter.dispatchEvent(new CustomEvent('input'));
      lastNameFilter.dispatchEvent(new CustomEvent('input'));

      const expectedFilter: AndFilter = {
        ...{ t: 'and' },
        children: [],
      };
      expect(lastFilter).not.to.eql(expectedFilter);
      firstNameFilter.value = '';
      lastNameFilter.value = '';
      firstNameFilter.dispatchEvent(new CustomEvent('input'));
      lastNameFilter.dispatchEvent(new CustomEvent('input'));

      expect(lastFilter).to.eql(expectedFilter);
    });
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
