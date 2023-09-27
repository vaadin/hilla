import { expect, use } from '@esm-bundle/chai';
import type { GridElement } from '@hilla/react-components/Grid.js';
import type { SelectElement } from '@hilla/react-components/Select.js';
import type { TextFieldElement } from '@hilla/react-components/TextField.js';
import { render, type RenderResult } from '@testing-library/react';

import sinonChai from 'sinon-chai';
import { AutoGrid, type AutoGridProps } from '../src/autogrid.js';
import type AndFilter from '../src/types/dev/hilla/crud/filter/AndFilter.js';
import Matcher from '../src/types/dev/hilla/crud/filter/PropertyStringFilter/Matcher.js';
import type PropertyStringFilter from '../src/types/dev/hilla/crud/filter/PropertyStringFilter.js';
import { _generateHeader } from '../src/utils.js';
import {
  getBodyCellContent,
  getHeaderCellContent,
  getHeaderRows,
  getSortOrder,
  getVisibleRowCount,
  sortGrid,
} from './grid-test-helpers.js';
import { CompanyModel, PersonModel, companyService, personService, type Person } from './test-models-and-services.js';

use(sinonChai);

export async function nextFrame(): Promise<void> {
  return new Promise((resolve) => {
    requestAnimationFrame(() => {
      resolve(undefined);
    });
  });
}

async function assertColumns(result: RenderResult, ...ids: string[]) {
  const grid = result.container.querySelector('vaadin-grid')!;
  await nextFrame();
  await nextFrame();
  await nextFrame();
  const columns = grid.querySelectorAll('vaadin-grid-column');
  expect(columns.length).to.equal(ids.length);
  for (let i = 0; i < ids.length; i++) {
    expect(getHeaderCellContent(grid, 0, i).innerText).to.equal(_generateHeader(ids[i]));
    expect(columns[i].path).to.equal(ids[i]);
  }
}

describe('@hilla/react-grid', () => {
  function TestAutoGrid(customProps: Partial<AutoGridProps<Person>>) {
    return <AutoGrid service={personService()} model={PersonModel} {...customProps}></AutoGrid>;
  }

  describe('Auto grid', () => {
    it('creates columns based on model', async () => {
      const result: RenderResult = render(<TestAutoGrid />);
      await assertColumns(result, 'firstName', 'lastName', 'email', 'someNumber', 'vip');
    });
    it('can change model and recreate columns', async () => {
      const result = render(<AutoGrid service={personService()} model={PersonModel}></AutoGrid>);
      await assertColumns(result, 'firstName', 'lastName', 'email', 'someNumber', 'vip');
      result.rerender(<AutoGrid service={companyService()} model={CompanyModel}></AutoGrid>);
      await assertColumns(result, 'name', 'foundedDate');
    });
    it('sorts according to first column by default', async () => {
      const result = render(<TestAutoGrid />);
      const grid: GridElement = result.container.querySelector('vaadin-grid')!;
      await nextFrame();
      await nextFrame();
      expect(getBodyCellContent(grid, 0, 0).innerText).to.equal('Jane');
      expect(getBodyCellContent(grid, 1, 0).innerText).to.equal('John');
    });
    it('retains sorting when re-rendering', async () => {
      const result = render(<TestAutoGrid />);
      const grid: GridElement = result.container.querySelector('vaadin-grid')!;
      await nextFrame();
      await nextFrame();
      sortGrid(grid, 'lastName', 'desc');
      expect(getSortOrder(grid)).to.eql({ path: 'lastName', direction: 'desc' });
      result.rerender(<TestAutoGrid />);
      expect(getSortOrder(grid)).to.eql({ path: 'lastName', direction: 'desc' });
    });
    it('creates sortable columns', async () => {
      const result = render(<TestAutoGrid />);
      const grid: GridElement = result.container.querySelector('vaadin-grid')!;
      await nextFrame();
      await nextFrame();
      sortGrid(grid, 'firstName', 'desc');
      await nextFrame();
      expect(getBodyCellContent(grid, 0, 0).innerText).to.equal('John');
      expect(getBodyCellContent(grid, 1, 0).innerText).to.equal('Jane');
    });
    it('sets a data provider, but only once', async () => {
      const service = personService();
      const result = render(<TestAutoGrid service={service} />);
      const grid = result.container.querySelector('vaadin-grid')!;
      await nextFrame();
      await nextFrame();
      const dp = grid.dataProvider;
      expect(dp).to.not.be.undefined;
      result.rerender(<TestAutoGrid service={service} />);
      const grid2 = result.container.querySelector('vaadin-grid')!;
      expect(dp).to.equal(grid2.dataProvider);
    });
    it('data provider provides data', async () => {
      const result = render(<TestAutoGrid />);
      await nextFrame();
      await nextFrame();

      const grid: GridElement = result.container.querySelector('vaadin-grid')!;
      await nextFrame();
      expect(getVisibleRowCount(grid)).to.equal(2);
      expect(getBodyCellContent(grid, 0, 0).innerText).to.equal('Jane');
      expect(getBodyCellContent(grid, 0, 1).innerText).to.equal('Love');
      expect(getBodyCellContent(grid, 1, 0).innerText).to.equal('John');
      expect(getBodyCellContent(grid, 1, 1).innerText).to.equal('Dove');
    });
    it('renders strings without formatting and with default alignment', async () => {
      const result = render(<TestAutoGrid />);
      await nextFrame();
      const grid: GridElement = result.container.querySelector('vaadin-grid')!;
      await nextFrame();
      expect(getBodyCellContent(grid, 0, 1).style.textAlign).to.equal('');
      expect(getBodyCellContent(grid, 0, 1).innerText).to.equal('Dove');
      expect(getBodyCellContent(grid, 1, 1).innerText).to.equal('Love');
    });
    it('renders numbers as right aligned numbers', async () => {
      const result = render(<TestAutoGrid />);
      await nextFrame();
      const grid: GridElement = result.container.querySelector('vaadin-grid')!;
      await nextFrame();
      expect(getBodyCellContent(grid, 0, 3).style.textAlign).to.equal('end');
      expect(getBodyCellContent(grid, 0, 3).innerText).to.equal('-12');
      expect(getBodyCellContent(grid, 1, 3).innerText).to.equal('123,456');
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
        const cell = getHeaderCellContent(grid, 1, 0);
        expect(cell.firstElementChild?.localName).to.equal('vaadin-text-field');
      });
      it('created for number columns', async () => {
        const result = render(<TestAutoGrid headerFilters />);
        await nextFrame();
        await nextFrame();
        const grid: GridElement = result.container.querySelector('vaadin-grid')!;
        const cell = getHeaderCellContent(grid, 1, 3);
        expect(cell.firstElementChild?.localName).to.equal('vaadin-select');
      });
      it('no filters created for other columns', async () => {
        const result = render(<TestAutoGrid headerFilters />);
        await nextFrame();
        await nextFrame();
        const grid: GridElement = result.container.querySelector('vaadin-grid')!;
        const cell = getHeaderCellContent(grid, 1, 4);
        expect(cell.firstElementChild).to.null;
      });
      it('filter when you type in the field for a string column', async () => {
        const service = personService();
        const result = render(<TestAutoGrid service={service} headerFilters />);
        await nextFrame();
        await nextFrame();
        const grid: GridElement = result.container.querySelector('vaadin-grid')!;
        const firstNameFilterField = getHeaderCellContent(grid, 1, 0).firstElementChild as TextFieldElement;
        firstNameFilterField.value = 'filter-value';
        firstNameFilterField.dispatchEvent(new CustomEvent('input'));
        await nextFrame();

        const expectedPropertyFilter: PropertyStringFilter = {
          ...{ t: 'propertyString' },
          filterValue: 'filter-value',
          propertyId: 'firstName',
          matcher: Matcher.CONTAINS,
        };
        const expectedFilter: AndFilter = { ...{ t: 'and' }, children: [expectedPropertyFilter] };
        expect(service.lastFilter).to.eql(expectedFilter);
      });
      it('filter when you type in the field for a number column', async () => {
        const service = personService();
        const result = render(<TestAutoGrid service={service} headerFilters />);
        await nextFrame();
        await nextFrame();
        const grid: GridElement = result.container.querySelector('vaadin-grid')!;
        const someNumberFilterField = getHeaderCellContent(grid, 1, 3).firstElementChild!
          .nextElementSibling as TextFieldElement;
        someNumberFilterField.value = '123';
        someNumberFilterField.dispatchEvent(new CustomEvent('input'));
        await nextFrame();

        const expectedPropertyFilter: PropertyStringFilter = {
          ...{ t: 'propertyString' },
          filterValue: '123',
          propertyId: 'someNumber',
          matcher: Matcher.GREATER_THAN,
        };
        const expectedFilter: AndFilter = { ...{ t: 'and' }, children: [expectedPropertyFilter] };
        expect(service.lastFilter).to.eql(expectedFilter);

        const someNumberFilterSelect = someNumberFilterField.previousElementSibling as SelectElement;
        someNumberFilterSelect.value = Matcher.EQUALS;
        await nextFrame();
        await nextFrame();

        const expectedPropertyFilter2: PropertyStringFilter = {
          ...{ t: 'propertyString' },
          filterValue: '123',
          propertyId: 'someNumber',
          matcher: Matcher.EQUALS,
        };
        const expectedFilter2: AndFilter = { ...{ t: 'and' }, children: [expectedPropertyFilter2] };
        expect(service.lastFilter).to.eql(expectedFilter2);
      });
      it('combine filters (and) when you type in multiple fields', async () => {
        const service = personService();
        const result = render(<TestAutoGrid service={service} headerFilters />);
        await nextFrame();
        await nextFrame();
        const grid: GridElement = result.container.querySelector('vaadin-grid')!;
        const firstNameFilterField = getHeaderCellContent(grid, 1, 0).firstElementChild as TextFieldElement;
        firstNameFilterField.value = 'filterFirst';
        firstNameFilterField.dispatchEvent(new CustomEvent('input'));

        const lastNameFilterField = getHeaderCellContent(grid, 1, 1).firstElementChild as TextFieldElement;
        lastNameFilterField.value = 'filterLast';
        lastNameFilterField.dispatchEvent(new CustomEvent('input'));
        await nextFrame();

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
        expect(service.lastFilter).to.eql(expectedFilter);
      });
      it('removes filters if turning header filters off', async () => {
        const service = personService();

        const result = render(<AutoGrid service={service} model={PersonModel} headerFilters></AutoGrid>);
        await nextFrame();
        await nextFrame();
        const grid = result.container.querySelector('vaadin-grid')!;
        expect(getHeaderRows(grid).length).to.equal(2);

        const companyNameFilter = getHeaderCellContent(grid, 1, 0).firstElementChild as TextFieldElement;
        companyNameFilter.value = 'Joh';
        companyNameFilter.dispatchEvent(new CustomEvent('input'));
        await nextFrame();

        const filter: PropertyStringFilter = {
          ...{ t: 'propertyString' },
          filterValue: 'Joh',
          matcher: Matcher.CONTAINS,
          propertyId: 'firstName',
        };
        const expectedFilter1: AndFilter = {
          ...{ t: 'and' },
          children: [filter],
        };
        expect(service.lastFilter).to.eql(expectedFilter1);

        result.rerender(<AutoGrid service={service} model={PersonModel}></AutoGrid>);
        await nextFrame();
        await nextFrame();
        expect(getHeaderRows(grid).length).to.equal(1);

        const expectedFilter2: AndFilter = {
          ...{ t: 'and' },
          children: [],
        };
        expect(service.lastFilter).to.eql(expectedFilter2);
      });
      it('filters correctly after changing model', async () => {
        const _personService = personService();
        const _companyService = companyService();

        const result = render(<AutoGrid service={_personService} model={PersonModel} headerFilters></AutoGrid>);
        await nextFrame();
        await nextFrame();
        result.rerender(<AutoGrid service={_companyService} model={CompanyModel} headerFilters></AutoGrid>);
        await nextFrame();
        await nextFrame();
        const grid: GridElement = result.container.querySelector('vaadin-grid')!;
        const companyNameFilter = getHeaderCellContent(grid, 1, 0).firstElementChild as TextFieldElement;
        companyNameFilter.value = 'vaad';
        companyNameFilter.dispatchEvent(new CustomEvent('input'));
        await nextFrame();

        const expectedPropertyFilter: PropertyStringFilter = {
          ...{ t: 'propertyString' },
          filterValue: 'vaad',
          propertyId: 'name',
          matcher: Matcher.CONTAINS,
        };
        const expectedFilter: AndFilter = { ...{ t: 'and' }, children: [expectedPropertyFilter] };
        expect(_personService.lastFilter).to.eql(expectedFilter);
      });
    });
    it('removes the filters when you clear the fields', async () => {
      const service = personService();
      const result = render(<TestAutoGrid headerFilters service={service} />);
      await nextFrame();
      await nextFrame();
      const grid: GridElement = result.container.querySelector('vaadin-grid')!;
      const firstNameFilter = getHeaderCellContent(grid, 1, 0).firstElementChild as TextFieldElement;
      const lastNameFilter = getHeaderCellContent(grid, 1, 1).firstElementChild as TextFieldElement;
      firstNameFilter.value = 'filterFirst';
      lastNameFilter.value = 'filterLast';
      firstNameFilter.dispatchEvent(new CustomEvent('input'));
      lastNameFilter.dispatchEvent(new CustomEvent('input'));
      await nextFrame();

      const expectedFilter: AndFilter = {
        ...{ t: 'and' },
        children: [],
      };
      expect(service.lastFilter).not.to.eql(expectedFilter);

      firstNameFilter.value = '';
      firstNameFilter.dispatchEvent(new CustomEvent('input'));
      lastNameFilter.value = '';
      lastNameFilter.dispatchEvent(new CustomEvent('input'));
      await nextFrame();
      await nextFrame();

      expect(service.lastFilter).to.eql(expectedFilter);
    });
  });
  describe('customize columns', () => {
    it('should only show configured columns in specified order', async () => {
      const result = render(<TestAutoGrid visibleColumns={['email', 'firstName']} />);
      await assertColumns(result, 'email', 'firstName');
    });

    it('should show columns that would be excluded by default', async () => {
      const result = render(<TestAutoGrid visibleColumns={['id', 'version']} />);
      await assertColumns(result, 'id', 'version');
    });

    it('should ignore unknown columns', async () => {
      const result = render(<TestAutoGrid visibleColumns={['foo', 'email', 'bar', 'firstName']} />);
      await assertColumns(result, 'email', 'firstName');
    });
  });
});
