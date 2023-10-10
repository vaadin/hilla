import { expect, use } from '@esm-bundle/chai';
import type { GridElement } from '@hilla/react-components/Grid.js';
import { GridColumn } from '@hilla/react-components/GridColumn.js';
import type { SelectElement } from '@hilla/react-components/Select.js';
import type { TextFieldElement } from '@hilla/react-components/TextField.js';
import { render, type RenderResult } from '@testing-library/react';

import sinonChai from 'sinon-chai';
import { AutoGrid, type AutoGridProps } from '../src/autogrid.js';
import { _generateHeader } from '../src/property-info.js';
import type AndFilter from '../src/types/dev/hilla/crud/filter/AndFilter.js';
import Matcher from '../src/types/dev/hilla/crud/filter/PropertyStringFilter/Matcher.js';
import type PropertyStringFilter from '../src/types/dev/hilla/crud/filter/PropertyStringFilter.js';
import {
  getBodyCellContent,
  getGrid,
  getHeaderCellContent,
  getHeaderRows,
  getSortOrder,
  getVisibleRowCount,
  reactRender,
  sortGrid,
} from './grid-test-helpers.js';
import {
  ColumnRendererTestModel,
  CompanyModel,
  PersonModel,
  columnRendererTestService,
  companyService,
  personService,
  type Person,
} from './test-models-and-services.js';

use(sinonChai);

async function assertColumns(result: RenderResult, ...ids: string[]) {
  const grid = getGrid(result);
  await reactRender();
  const columns = grid.querySelectorAll('vaadin-grid-column');
  expect(columns.length).to.equal(ids.length);
  for (let i = 0; i < ids.length; i++) {
    expect(getHeaderCellContent(grid, 0, i).innerText).to.equal(_generateHeader(ids[i]));
    if (ids[i] === '') {
      expect(columns[i].path).to.equal(undefined);
    } else {
      expect(columns[i].path).to.equal(ids[i]);
    }
  }
}

describe('@hilla/react-crud', () => {
  function TestAutoGridNoHeaderFilters(customProps: Partial<AutoGridProps<Person>>) {
    return <AutoGrid service={personService()} model={PersonModel} noHeaderFilters {...customProps}></AutoGrid>;
  }

  function TestAutoGrid(customProps: Partial<AutoGridProps<Person>>) {
    return <AutoGrid service={personService()} model={PersonModel} {...customProps}></AutoGrid>;
  }

  describe('Auto grid', () => {
    it('creates columns based on model', async () => {
      const result: RenderResult = render(<TestAutoGridNoHeaderFilters />);
      await assertColumns(result, 'firstName', 'lastName', 'email', 'someNumber', 'vip');
    });
    it('can change model and recreate columns', async () => {
      const result = render(<AutoGrid service={personService()} model={PersonModel}></AutoGrid>);
      await assertColumns(result, 'firstName', 'lastName', 'email', 'someNumber', 'vip');
      result.rerender(<AutoGrid service={companyService()} model={CompanyModel}></AutoGrid>);
      await assertColumns(result, 'name', 'foundedDate');
    });
    it('sorts according to first column by default', async () => {
      const result = render(<TestAutoGridNoHeaderFilters />);
      const grid: GridElement = result.container.querySelector('vaadin-grid')!;
      await reactRender();
      expect(getBodyCellContent(grid, 0, 0).innerText).to.equal('Jane');
      expect(getBodyCellContent(grid, 1, 0).innerText).to.equal('John');
    });
    it('retains sorting when re-rendering', async () => {
      const result = render(<TestAutoGridNoHeaderFilters />);
      const grid: GridElement = result.container.querySelector('vaadin-grid')!;
      await reactRender();
      sortGrid(grid, 'lastName', 'desc');
      expect(getSortOrder(grid)).to.eql({ path: 'lastName', direction: 'desc' });
      result.rerender(<TestAutoGridNoHeaderFilters />);
      expect(getSortOrder(grid)).to.eql({ path: 'lastName', direction: 'desc' });
    });
    it('creates sortable columns', async () => {
      const result = render(<TestAutoGridNoHeaderFilters />);
      const grid = getGrid(result);
      await reactRender();
      sortGrid(grid, 'firstName', 'desc');
      await reactRender();
      expect(getBodyCellContent(grid, 0, 0).innerText).to.equal('John');
      expect(getBodyCellContent(grid, 1, 0).innerText).to.equal('Jane');
    });
    it('sets a data provider, but only once', async () => {
      const service = personService();
      const result = render(<TestAutoGridNoHeaderFilters service={service} />);
      const grid = getGrid(result);
      await reactRender();
      const dp = grid.dataProvider;
      expect(dp).to.not.be.undefined;
      result.rerender(<TestAutoGridNoHeaderFilters service={service} />);
      const grid2 = getGrid(result);
      expect(dp).to.equal(grid2.dataProvider);
    });
    it('data provider provides data', async () => {
      const result = render(<TestAutoGridNoHeaderFilters />);
      await reactRender();
      const grid = getGrid(result);
      await reactRender();
      expect(getVisibleRowCount(grid)).to.equal(2);
      expect(getBodyCellContent(grid, 0, 0).innerText).to.equal('Jane');
      expect(getBodyCellContent(grid, 0, 1).innerText).to.equal('Love');
      expect(getBodyCellContent(grid, 1, 0).innerText).to.equal('John');
      expect(getBodyCellContent(grid, 1, 1).innerText).to.equal('Dove');
    });
    it('does not pass its own parameters to the underlying grid', () => {
      const result = render(<TestAutoGridNoHeaderFilters />);
      const grid = getGrid(result);
      expect(grid.getAttribute('model')).to.be.null;
      expect(grid.getAttribute('service')).to.be.null;
    });
    it('calls data provider list() only once for initial data', async () => {
      const testService = personService();
      expect(testService.callCount).to.equal(0);
      render(<AutoGrid service={testService} model={PersonModel} />);
      await reactRender();
      expect(testService.callCount).to.equal(1);
    });
    it('passes filter to the data provider', async () => {
      const filter: PropertyStringFilter = { filterValue: 'Jan', matcher: Matcher.CONTAINS, propertyId: 'firstName' };
      // eslint-disable-next-line
      (filter as any).t = 'propertyString'; // Workaround for https://github.com/vaadin/hilla/issues/438

      const result = render(<TestAutoGrid experimentalFilter={filter} />);
      await reactRender();
      const grid = getGrid(result);
      expect(getVisibleRowCount(grid)).to.equal(1);
      expect(getBodyCellContent(grid, 0, 0).innerText).to.equal('Jane');
      expect(getBodyCellContent(grid, 0, 1).innerText).to.equal('Love');
    });

    describe('header filters', () => {
      it('created for string columns', async () => {
        const result = render(<TestAutoGrid />);
        await reactRender();
        const grid = getGrid(result);
        const cell = getHeaderCellContent(grid, 1, 0);
        expect(cell.firstElementChild?.localName).to.equal('vaadin-text-field');
      });
      it('created for number columns', async () => {
        const result = render(<TestAutoGrid />);
        await reactRender();
        const grid = getGrid(result);
        const cell = getHeaderCellContent(grid, 1, 3);
        expect(cell.firstElementChild?.localName).to.equal('vaadin-select');
      });
      it('filter when you type in the field for a string column', async () => {
        const service = personService();
        const result = render(<TestAutoGrid service={service} />);
        await reactRender();
        const grid = getGrid(result);
        const firstNameFilterField = getHeaderCellContent(grid, 1, 0).firstElementChild as TextFieldElement;
        firstNameFilterField.value = 'filter-value';
        firstNameFilterField.dispatchEvent(new CustomEvent('input'));
        await reactRender();

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
        const result = render(<TestAutoGrid service={service} />);
        await reactRender();
        const grid = getGrid(result);
        const someNumberFilterField = getHeaderCellContent(grid, 1, 3).firstElementChild!
          .nextElementSibling as TextFieldElement;
        someNumberFilterField.value = '123';
        someNumberFilterField.dispatchEvent(new CustomEvent('input'));
        await reactRender();

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
        await reactRender();

        const expectedPropertyFilter2: PropertyStringFilter = {
          ...{ t: 'propertyString' },
          filterValue: '123',
          propertyId: 'someNumber',
          matcher: Matcher.EQUALS,
        };
        const expectedFilter2: AndFilter = { ...{ t: 'and' }, children: [expectedPropertyFilter2] };
        expect(service.lastFilter).to.eql(expectedFilter2);
      });
      it('filters for a boolean column', async () => {
        const service = personService();
        const result = render(<TestAutoGrid service={service} />);
        await reactRender();
        const grid: GridElement = result.container.querySelector('vaadin-grid')!;
        const select = getHeaderCellContent(grid, 1, 4).firstElementChild as SelectElement;
        select.value = 'True';
        await reactRender();

        const expectedPropertyFilter: PropertyStringFilter = {
          ...{ t: 'propertyString' },
          filterValue: 'True',
          propertyId: 'vip',
          matcher: Matcher.EQUALS,
        };
        const expectedFilter: AndFilter = { ...{ t: 'and' }, children: [expectedPropertyFilter] };
        expect(service.lastFilter).to.eql(expectedFilter);

        select.value = 'False';
        await reactRender();

        const expectedPropertyFilter2: PropertyStringFilter = {
          ...{ t: 'propertyString' },
          filterValue: 'False',
          propertyId: 'vip',
          matcher: Matcher.EQUALS,
        };
        const expectedFilter2: AndFilter = { ...{ t: 'and' }, children: [expectedPropertyFilter2] };
        expect(service.lastFilter).to.eql(expectedFilter2);
      });
      it('combine filters (and) when you type in multiple fields', async () => {
        const service = personService();
        const result = render(<TestAutoGrid service={service} />);
        await reactRender();
        const grid = getGrid(result);
        const firstNameFilterField = getHeaderCellContent(grid, 1, 0).firstElementChild as TextFieldElement;
        firstNameFilterField.value = 'filterFirst';
        firstNameFilterField.dispatchEvent(new CustomEvent('input'));

        const lastNameFilterField = getHeaderCellContent(grid, 1, 1).firstElementChild as TextFieldElement;
        lastNameFilterField.value = 'filterLast';
        lastNameFilterField.dispatchEvent(new CustomEvent('input'));
        await reactRender();

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

        const result = render(<AutoGrid service={service} model={PersonModel}></AutoGrid>);
        await reactRender();
        const grid = getGrid(result);
        expect(getHeaderRows(grid).length).to.equal(2);

        const companyNameFilter = getHeaderCellContent(grid, 1, 0).firstElementChild as TextFieldElement;
        companyNameFilter.value = 'Joh';
        companyNameFilter.dispatchEvent(new CustomEvent('input'));
        await reactRender();

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

        result.rerender(<AutoGrid service={service} model={PersonModel} noHeaderFilters></AutoGrid>);
        await reactRender();
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

        const result = render(<AutoGrid service={_personService} model={PersonModel}></AutoGrid>);
        await reactRender();
        result.rerender(<AutoGrid service={_companyService} model={CompanyModel}></AutoGrid>);
        await reactRender();
        const grid = getGrid(result);
        const companyNameFilter = getHeaderCellContent(grid, 1, 0).firstElementChild as TextFieldElement;
        companyNameFilter.value = 'vaad';
        companyNameFilter.dispatchEvent(new CustomEvent('input'));
        await reactRender();

        const expectedPropertyFilter: PropertyStringFilter = {
          ...{ t: 'propertyString' },
          filterValue: 'vaad',
          propertyId: 'name',
          matcher: Matcher.CONTAINS,
        };
        const expectedFilter: AndFilter = { ...{ t: 'and' }, children: [expectedPropertyFilter] };
        expect(_companyService.lastFilter).to.eql(expectedFilter);
      });
    });
    it('removes the filters when you clear the fields', async () => {
      const service = personService();
      const result = render(<TestAutoGrid service={service} />);
      await reactRender();
      const grid = getGrid(result);
      const firstNameFilter = getHeaderCellContent(grid, 1, 0).firstElementChild as TextFieldElement;
      const lastNameFilter = getHeaderCellContent(grid, 1, 1).firstElementChild as TextFieldElement;
      firstNameFilter.value = 'filterFirst';
      lastNameFilter.value = 'filterLast';
      firstNameFilter.dispatchEvent(new CustomEvent('input'));
      lastNameFilter.dispatchEvent(new CustomEvent('input'));
      await reactRender();

      const expectedFilter: AndFilter = {
        ...{ t: 'and' },
        children: [],
      };
      expect(service.lastFilter).not.to.eql(expectedFilter);

      firstNameFilter.value = '';
      firstNameFilter.dispatchEvent(new CustomEvent('input'));
      lastNameFilter.value = '';
      lastNameFilter.dispatchEvent(new CustomEvent('input'));
      await reactRender();
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

    it('renders custom columns at the end', async () => {
      const NameRenderer = ({ item }: { item: Person }): JSX.Element => (
        <span>
          {item.firstName} {item.lastName}
        </span>
      );
      const result = render(
        <TestAutoGrid customColumns={[<GridColumn autoWidth renderer={NameRenderer}></GridColumn>]} />,
      );
      await reactRender();
      const grid = getGrid(result);
      await assertColumns(result, 'firstName', 'lastName', 'email', 'someNumber', 'vip', '');
      expect(getBodyCellContent(grid, 0, 5).innerText).to.equal('Jane Love');
    });
    it('uses custom column options on top of the type defaults', async () => {
      const NameRenderer = ({ item }: { item: Person }): JSX.Element => <span>{item.firstName.toUpperCase()}</span>;
      const result = render(<TestAutoGrid columnOptions={{ firstName: { renderer: NameRenderer } }} />);
      await reactRender();
      const grid = getGrid(result);
      await assertColumns(result, 'firstName', 'lastName', 'email', 'someNumber', 'vip');
      const janeCell = getBodyCellContent(grid, 0, 0);
      expect(janeCell.innerText).to.equal('JANE');
      // The header filter was not overridden
      const cell = getHeaderCellContent(grid, 1, 0);
      expect(cell.firstElementChild?.localName).to.equal('vaadin-text-field');
    });
    it('renders row numbers if requested', async () => {
      const result = render(<TestAutoGrid rowNumbers />);
      await reactRender();
      const grid = getGrid(result);
      await assertColumns(result, '', 'firstName', 'lastName', 'email', 'someNumber', 'vip');
      expect(getBodyCellContent(grid, 0, 0).innerText).to.equal('1');
    });
  });

  describe('default renderers', () => {
    let grid: GridElement;

    beforeEach(async () => {
      const result = render(
        <AutoGrid service={columnRendererTestService()} model={ColumnRendererTestModel}></AutoGrid>,
      );
      await reactRender();
      grid = result.container.querySelector('vaadin-grid')!;
    });

    it('renders strings without formatting and with default alignment', () => {
      expect(getBodyCellContent(grid, 0, 0).style.textAlign).to.equal('');
      expect(getBodyCellContent(grid, 0, 0).innerText).to.equal('Hello World 1');
      expect(getBodyCellContent(grid, 1, 0).innerText).to.equal('Hello World 2');
    });

    it('renders numbers as right aligned numbers', () => {
      expect(getBodyCellContent(grid, 0, 1).style.textAlign).to.equal('end');
      expect(getBodyCellContent(grid, 0, 1).innerText).to.equal('123,456');
      expect(getBodyCellContent(grid, 1, 1).innerText).to.equal('-12');
    });

    it('renders booleans as icons', () => {
      const vip = getBodyCellContent(grid, 0, 2).querySelector('vaadin-icon')!;
      expect(vip.icon).to.equal('lumo:checkmark');
      const notVip = getBodyCellContent(grid, 1, 2).querySelector('vaadin-icon')!;
      expect(notVip.icon).to.equal('lumo:minus');
    });

    it('renders java.util.Date as right aligned', () => {
      expect(getBodyCellContent(grid, 0, 3).style.textAlign).to.equal('end');
      expect(getBodyCellContent(grid, 0, 3).textContent).to.equal('5/13/2021');
      expect(getBodyCellContent(grid, 1, 3).textContent).to.equal('5/14/2021');
      expect(getBodyCellContent(grid, 2, 3).textContent).to.equal('');
      expect(getBodyCellContent(grid, 3, 3).textContent).to.equal('');
    });

    it('renders java.time.LocalDate as right aligned', () => {
      expect(getBodyCellContent(grid, 0, 4).style.textAlign).to.equal('end');
      expect(getBodyCellContent(grid, 0, 4).textContent).to.equal('5/13/2021');
      expect(getBodyCellContent(grid, 1, 4).textContent).to.equal('5/14/2021');
      expect(getBodyCellContent(grid, 2, 4).textContent).to.equal('');
      expect(getBodyCellContent(grid, 3, 4).textContent).to.equal('');
    });

    it('renders java.time.LocalTime as right aligned', () => {
      expect(getBodyCellContent(grid, 0, 5).style.textAlign).to.equal('end');
      expect(getBodyCellContent(grid, 0, 5).textContent).to.equal('8:45 AM');
      expect(getBodyCellContent(grid, 1, 5).textContent).to.equal('8:45 PM');
      expect(getBodyCellContent(grid, 2, 5).textContent).to.equal('');
      expect(getBodyCellContent(grid, 3, 5).textContent).to.equal('');
    });

    it('renders java.time.LocalDateTime as right aligned', () => {
      expect(getBodyCellContent(grid, 0, 6).style.textAlign).to.equal('end');
      expect(getBodyCellContent(grid, 0, 6).textContent).to.equal('5/13/2021, 8:45 AM');
      expect(getBodyCellContent(grid, 1, 6).textContent).to.equal('5/14/2021, 8:45 PM');
      expect(getBodyCellContent(grid, 2, 6).textContent).to.equal('');
      expect(getBodyCellContent(grid, 3, 6).textContent).to.equal('');
    });

    it('renders nested strings without formatting and with default alignment', () => {
      expect(getBodyCellContent(grid, 0, 7).style.textAlign).to.equal('');
      expect(getBodyCellContent(grid, 0, 7).innerText).to.equal('Nested string 1');
      expect(getBodyCellContent(grid, 1, 7).innerText).to.equal('');
    });

    it('renders nested numbers as right aligned numbers', () => {
      expect(getBodyCellContent(grid, 0, 8).style.textAlign).to.equal('end');
      expect(getBodyCellContent(grid, 0, 8).innerText).to.equal('123,456');
      expect(getBodyCellContent(grid, 1, 8).innerText).to.equal('');
    });

    it('renders nested booleans as icons', () => {
      const vip = getBodyCellContent(grid, 0, 9).querySelector('vaadin-icon')!;
      expect(vip.icon).to.equal('lumo:checkmark');
      const notVip = getBodyCellContent(grid, 1, 9).querySelector('vaadin-icon')!;
      expect(notVip.icon).to.equal('lumo:minus');
    });

    it('renders java.util.Date as right aligned', () => {
      expect(getBodyCellContent(grid, 0, 10).style.textAlign).to.equal('end');
      expect(getBodyCellContent(grid, 0, 10).textContent).to.equal('5/13/2021');
      expect(getBodyCellContent(grid, 1, 10).textContent).to.equal('');
    });
  });
});
