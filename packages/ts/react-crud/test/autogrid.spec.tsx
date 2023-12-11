import { expect, use } from '@esm-bundle/chai';
import { GridColumn } from '@hilla/react-components/GridColumn.js';
import { render } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import chaiAsPromised from 'chai-as-promised';
import { useEffect, useRef } from 'react';
import sinon from 'sinon';
import sinonChai from 'sinon-chai';
import type { ListService } from '../crud';
import { AutoGrid, type AutoGridProps, type AutoGridRef } from '../src/autogrid.js';
import type { CrudService } from '../src/crud.js';
import { LocaleContext } from '../src/locale.js';
import type AndFilter from '../src/types/dev/hilla/crud/filter/AndFilter.js';
import Matcher from '../src/types/dev/hilla/crud/filter/PropertyStringFilter/Matcher.js';
import type PropertyStringFilter from '../src/types/dev/hilla/crud/filter/PropertyStringFilter.js';
import type Sort from '../src/types/dev/hilla/mappedtypes/Sort.js';
import Direction from '../src/types/org/springframework/data/domain/Sort/Direction.js';
import GridController from './GridController.js';
import SelectController from './SelectController.js';
import {
  ColumnRendererTestModel,
  columnRendererTestService,
  CompanyModel,
  companyService,
  createService,
  Gender,
  getItem,
  type HasTestInfo,
  type Person,
  personData,
  PersonModel,
  personService,
  PersonWithoutIdPropertyModel,
  PersonWithSimpleIdPropertyModel,
} from './test-models-and-services.js';
import TextFieldController from './TextFieldController.js';

use(sinonChai);
use(chaiAsPromised);

export async function nextFrame(): Promise<void> {
  return new Promise<void>((resolve) => {
    requestAnimationFrame(() => {
      resolve();
    });
  });
}

async function assertColumnsOrder(grid: GridController, ...ids: string[]) {
  const columns = await grid.getColumns();
  expect(columns).to.have.length(ids.length);
  await expect(grid.getHeaderCellContents()).to.eventually.deep.equal(grid.generateColumnHeaders(ids));
}

async function assertColumns(grid: GridController, ...ids: string[]) {
  await assertColumnsOrder(grid, ...ids);
  const columns = await grid.getColumns();
  for (let i = 0; i < ids.length; i++) {
    if (ids[i] === '') {
      expect(columns[i].path).to.equal(undefined);
    } else {
      expect(columns[i].path).to.equal(ids[i]);
    }
  }
}

describe('@hilla/react-crud', () => {
  describe('Auto grid', () => {
    function TestAutoGridNoHeaderFilters(customProps: Partial<AutoGridProps<Person>>) {
      return <AutoGrid service={personService()} model={PersonModel} noHeaderFilters {...customProps} />;
    }

    function TestAutoGrid(customProps: Partial<AutoGridProps<Person>>) {
      return <AutoGrid service={personService()} model={PersonModel} {...customProps} />;
    }

    let user: ReturnType<(typeof userEvent)['setup']>;

    beforeEach(() => {
      user = userEvent.setup();
    });

    describe('basics', () => {
      it('creates columns based on model', async () => {
        const grid = await GridController.init(render(<TestAutoGridNoHeaderFilters />), user);
        await assertColumns(
          grid,
          'firstName',
          'lastName',
          'gender',
          'email',
          'someInteger',
          'someDecimal',
          'vip',
          'birthDate',
          'shiftStart',
          'appointmentTime',
          'address.street',
          'address.city',
          'address.country',
          'department',
        );
      });

      it('can change model and recreate columns', async () => {
        const result = render(<AutoGrid service={personService()} model={PersonModel} />);
        await assertColumns(
          await GridController.init(result, user),
          'firstName',
          'lastName',
          'gender',
          'email',
          'someInteger',
          'someDecimal',
          'vip',
          'birthDate',
          'shiftStart',
          'appointmentTime',
          'address.street',
          'address.city',
          'address.country',
          'department',
        );
        result.rerender(<AutoGrid service={companyService()} model={CompanyModel} />);
        await assertColumns(await GridController.init(result, user), 'name', 'foundedDate');
      });

      it('sorts according to first column by default', async () => {
        const service = personService();
        const grid = await GridController.init(render(<TestAutoGridNoHeaderFilters service={service} />), user);

        const expectedSort: Sort = { orders: [{ property: 'firstName', direction: Direction.ASC, ignoreCase: false }] };
        expect(service.lastSort).to.deep.equal(expectedSort);
        await expect(grid.getSortOrder()).to.eventually.deep.equal([
          { property: 'firstName', direction: Direction.ASC },
        ]);
      });

      it('retains sorting when re-rendering', async () => {
        const result = render(<TestAutoGridNoHeaderFilters />);
        const grid = await GridController.init(result, user);

        await grid.sort('lastName', 'desc');
        await expect(grid.getSortOrder()).to.eventually.deep.equal([
          { property: 'lastName', direction: Direction.DESC },
        ]);

        result.rerender(<TestAutoGridNoHeaderFilters />);
        await expect(grid.getSortOrder()).to.eventually.deep.equal([
          { property: 'lastName', direction: Direction.DESC },
        ]);
      });

      it('creates sortable columns', async () => {
        const service = personService();
        const grid = await GridController.init(render(<TestAutoGridNoHeaderFilters service={service} />), user);
        await grid.sort('firstName', 'desc');

        const expectedSort: Sort = {
          orders: [{ property: 'firstName', direction: Direction.DESC, ignoreCase: false }],
        };
        expect(service.lastSort).to.deep.equal(expectedSort);
        await expect(grid.getSortOrder()).to.eventually.deep.equal([
          { property: 'firstName', direction: Direction.DESC },
        ]);
      });

      it('allows to disable sorting on specific columns', async () => {
        const service = personService();
        const grid = await GridController.init(
          render(<TestAutoGridNoHeaderFilters service={service} columnOptions={{ lastName: { sortable: false } }} />),
          user,
        );

        try {
          await grid.sort('firstName', 'desc');
          await grid.sort('lastName', 'desc');
        } catch (error) {
          expect(error).to.be.an.instanceOf(Error);
          expect((error as Error).message).to.equal('No sorter found for path lastName');
        }
      });

      it('sets a data provider, but only once', async () => {
        const service = personService();
        const result = render(<TestAutoGridNoHeaderFilters service={service} />);
        let grid = await GridController.init(result, user);
        const dp = grid.instance.dataProvider;
        expect(dp).to.not.be.undefined;
        result.rerender(<TestAutoGridNoHeaderFilters service={service} />);
        grid = await GridController.init(result, user);
        expect(dp).to.equal(grid.instance.dataProvider);
      });

      it('data provider provides data', async () => {
        const grid = await GridController.init(render(<TestAutoGridNoHeaderFilters />), user);
        expect(grid.getVisibleRowCount()).to.equal(2);
        const firstNameColumnIndex = await grid.findColumnIndexByHeaderText('First name');
        const lastNameColumnIndex = await grid.findColumnIndexByHeaderText('Last name');
        expect(grid.getBodyCellContent(0, firstNameColumnIndex)).to.have.rendered.text('Jane');
        expect(grid.getBodyCellContent(0, lastNameColumnIndex)).to.have.rendered.text('Love');
        expect(grid.getBodyCellContent(1, firstNameColumnIndex)).to.have.rendered.text('John');
        expect(grid.getBodyCellContent(1, lastNameColumnIndex)).to.have.rendered.text('Dove');
      });

      it('does not pass its own parameters to the underlying grid', async () => {
        const grid = await GridController.init(render(<TestAutoGridNoHeaderFilters />), user);
        expect(grid.instance.getAttribute('model')).to.be.null;
        expect(grid.instance.getAttribute('service')).to.be.null;
      });

      it('calls data provider list() only once for initial data', async () => {
        const testService = personService();
        expect(testService.callCount).to.equal(0);
        await GridController.init(render(<AutoGrid service={testService} model={PersonModel} />), user);
        expect(testService.callCount).to.equal(1);
      });

      it('passes filter to the data provider', async () => {
        const filter: PropertyStringFilter = {
          '@type': 'propertyString',
          filterValue: 'Jan',
          matcher: Matcher.CONTAINS,
          propertyId: 'firstName',
        };

        const grid = await GridController.init(render(<TestAutoGrid experimentalFilter={filter} />), user);
        expect(grid.getVisibleRowCount()).to.equal(1);
        const firstNameColumnIndex = await grid.findColumnIndexByHeaderText('First name');
        const lastNameColumnIndex = await grid.findColumnIndexByHeaderText('Last name');
        expect(grid.getBodyCellContent(0, firstNameColumnIndex)).to.have.rendered.text('Jane');
        expect(grid.getBodyCellContent(0, lastNameColumnIndex)).to.have.rendered.text('Love');
      });

      describe('multi-sort', () => {
        let grid: GridController;
        let service: CrudService<Person> & HasTestInfo;

        function TestAutoGridWithMultiSort(customProps: Partial<AutoGridProps<Person>>) {
          return (
            <AutoGrid
              service={personService()}
              model={PersonModel}
              noHeaderFilters
              multiSort
              multiSortPriority="append"
              {...customProps}
            ></AutoGrid>
          );
        }

        beforeEach(async () => {
          service = personService();
          grid = await GridController.init(render(<TestAutoGridWithMultiSort service={service} />), user);
        });

        it('sorts according to first column by default', async () => {
          expect(service.lastSort).to.deep.equal({
            orders: [{ property: 'firstName', direction: Direction.ASC, ignoreCase: false }],
          });

          await expect(grid.getSortOrder()).to.eventually.deep.equal([
            { property: 'firstName', direction: Direction.ASC },
          ]);
        });

        it('sorts by multiple columns', async () => {
          await grid.sort('lastName', 'asc');
          expect(service.lastSort).to.deep.equal({
            orders: [
              { property: 'firstName', direction: Direction.ASC, ignoreCase: false },
              { property: 'lastName', direction: Direction.ASC, ignoreCase: false },
            ],
          });

          await expect(grid.getSortOrder()).to.eventually.deep.equal([
            { property: 'firstName', direction: Direction.ASC },
            { property: 'lastName', direction: Direction.ASC },
          ]);

          await grid.sort('lastName', 'desc');
          expect(service.lastSort).to.deep.equal({
            orders: [
              { property: 'firstName', direction: Direction.ASC, ignoreCase: false },
              { property: 'lastName', direction: Direction.DESC, ignoreCase: false },
            ],
          });

          await expect(grid.getSortOrder()).to.eventually.deep.equal([
            { property: 'firstName', direction: Direction.ASC },
            { property: 'lastName', direction: Direction.DESC },
          ]);

          await grid.sort('lastName', null);
          expect(service.lastSort).to.deep.equal({
            orders: [{ property: 'firstName', direction: Direction.ASC, ignoreCase: false }],
          });

          await expect(grid.getSortOrder()).to.eventually.deep.equal([
            { property: 'firstName', direction: Direction.ASC },
          ]);
        });
      });

      describe('header filters', () => {
        let clock: sinon.SinonFakeTimers;

        beforeEach(() => {
          clock = sinon.useFakeTimers({ shouldAdvanceTime: true });
        });

        afterEach(() => {
          clock.restore();
        });

        it('created for string columns', async () => {
          const grid = await GridController.init(render(<TestAutoGrid />), user);
          const cell = grid.getHeaderCellContent(1, 0);
          expect(cell.querySelector('vaadin-text-field')).to.exist;
        });

        it('created for number columns', async () => {
          const grid = await GridController.init(render(<TestAutoGrid />), user);
          const cell = grid.getHeaderCellContent(1, 4);
          expect(cell.querySelector('vaadin-select')).to.exist;
        });

        it('filter when you type in the field for a string column', async () => {
          const service = personService();
          const grid = await GridController.init(render(<TestAutoGrid service={service} />), user);

          const firstNameFilterField = grid.getHeaderCellContent(1, 0).querySelector('vaadin-text-field')!;
          firstNameFilterField.value = 'filter-value';
          firstNameFilterField.dispatchEvent(new CustomEvent('input'));
          await clock.tickAsync(200);

          const expectedPropertyFilter: PropertyStringFilter = {
            '@type': 'propertyString',
            filterValue: 'filter-value',
            propertyId: 'firstName',
            matcher: Matcher.CONTAINS,
          };
          const expectedFilter: AndFilter = { '@type': 'and', children: [expectedPropertyFilter] };
          expect(service.lastFilter).to.deep.equal(expectedFilter);
        });

        it('filter when you type in the field for a number column', async () => {
          const service = personService();
          const grid = await GridController.init(render(<TestAutoGrid service={service} />), user);
          const someNumberFilter = grid.getHeaderCellContent(1, 4);
          const [someNumberFilterField, someNumberFieldSelect] = await Promise.all([
            TextFieldController.initByParent(someNumberFilter, user, 'vaadin-number-field'),
            SelectController.init(someNumberFilter, user),
          ]);
          await someNumberFilterField.type('123');
          await clock.tickAsync(200);

          const expectedPropertyFilter: PropertyStringFilter = {
            '@type': 'propertyString',
            filterValue: '123',
            propertyId: 'someInteger',
            matcher: Matcher.GREATER_THAN,
          };
          const expectedFilter: AndFilter = { '@type': 'and', children: [expectedPropertyFilter] };
          expect(service.lastFilter).to.deep.equal(expectedFilter);

          await someNumberFieldSelect.select(Matcher.EQUALS);

          const expectedPropertyFilter2: PropertyStringFilter = {
            '@type': 'propertyString',
            filterValue: '123',
            propertyId: 'someInteger',
            matcher: Matcher.EQUALS,
          };

          const expectedFilter2: AndFilter = { '@type': 'and', children: [expectedPropertyFilter2] };
          expect(service.lastFilter).to.deep.equal(expectedFilter2);
        });

        it('filters for a boolean column', async () => {
          const service = personService();
          const grid = await GridController.init(render(<TestAutoGrid service={service} />), user);
          const controller = await SelectController.init(grid.getHeaderCellContent(1, 6), user);
          await controller.select('True');

          const expectedPropertyFilter: PropertyStringFilter = {
            '@type': 'propertyString',
            filterValue: 'True',
            propertyId: 'vip',
            matcher: Matcher.EQUALS,
          };
          const expectedFilter: AndFilter = { '@type': 'and', children: [expectedPropertyFilter] };
          expect(service.lastFilter).to.deep.equal(expectedFilter);

          await controller.select('False');

          const expectedPropertyFilter2: PropertyStringFilter = {
            '@type': 'propertyString',
            filterValue: 'False',
            propertyId: 'vip',
            matcher: Matcher.EQUALS,
          };
          const expectedFilter2: AndFilter = { '@type': 'and', children: [expectedPropertyFilter2] };
          expect(service.lastFilter).to.deep.equal(expectedFilter2);
        });

        it('filters for an enum column', async () => {
          const service = personService();
          const grid = await GridController.init(render(<TestAutoGrid service={service} />), user);
          const controller = await SelectController.init(grid.getHeaderCellContent(1, 2), user);
          await controller.select(Gender.MALE);

          const expectedPropertyFilter: PropertyStringFilter = {
            '@type': 'propertyString',
            filterValue: Gender.MALE,
            propertyId: 'gender',
            matcher: Matcher.EQUALS,
          };
          const expectedFilter: AndFilter = { '@type': 'and', children: [expectedPropertyFilter] };
          expect(service.lastFilter).to.deep.equal(expectedFilter);

          await controller.select(Gender.FEMALE);

          const expectedPropertyFilter2: PropertyStringFilter = {
            '@type': 'propertyString',
            filterValue: Gender.FEMALE,
            propertyId: 'gender',
            matcher: Matcher.EQUALS,
          };
          const expectedFilter2: AndFilter = { '@type': 'and', children: [expectedPropertyFilter2] };
          expect(service.lastFilter).to.deep.equal(expectedFilter2);
        });

        it('filter for nested properties that are not included by default', async () => {
          const service = personService();
          const grid = await GridController.init(
            render(<TestAutoGrid service={service} visibleColumns={['department.name']} />),
            user,
          );

          const departmentNameField = grid.getHeaderCellContent(1, 0).querySelector('vaadin-text-field')!;
          departmentNameField.value = 'filter-value';
          departmentNameField.dispatchEvent(new CustomEvent('input'));
          await clock.tickAsync(200);

          const expectedPropertyFilter: PropertyStringFilter = {
            '@type': 'propertyString',
            filterValue: 'filter-value',
            propertyId: 'department.name',
            matcher: Matcher.CONTAINS,
          };
          const expectedFilter: AndFilter = { '@type': 'and', children: [expectedPropertyFilter] };
          expect(service.lastFilter).to.deep.equal(expectedFilter);
        });

        it('does not show a filter for object column', async () => {
          const service = personService();
          const grid = await GridController.init(
            render(<TestAutoGrid service={service} visibleColumns={['address', 'department']} />),
            user,
          );

          expect(grid.getHeaderCellContent(1, 0).childElementCount).to.equal(0);
          expect(grid.getHeaderCellContent(1, 1).childElementCount).to.equal(0);
        });

        it('can be disabled on specific columns', async () => {
          const service = personService();
          const grid = await GridController.init(
            render(
              <TestAutoGrid
                service={service}
                visibleColumns={['firstName', 'lastName']}
                columnOptions={{ lastName: { filterable: false } }}
              />,
            ),
            user,
          );

          expect(grid.getHeaderCellContent(1, 0).childElementCount).to.be.greaterThan(0);
          expect(grid.getHeaderCellContent(1, 1).childElementCount).to.equal(0);
        });

        it('combine filters (and) when you type in multiple fields', async () => {
          const service = personService();
          const grid = await GridController.init(render(<TestAutoGrid service={service} />), user);
          const firstNameFilterField = await TextFieldController.initByParent(grid.getHeaderCellContent(1, 0), user);
          await firstNameFilterField.type('filterFirst');
          const lastNameFilterField = await TextFieldController.initByParent(grid.getHeaderCellContent(1, 1), user);
          await lastNameFilterField.type('filterLast');
          await clock.tickAsync(200);

          const expectedFirstNameFilter: PropertyStringFilter = {
            '@type': 'propertyString',
            filterValue: 'filterFirst',
            propertyId: 'firstName',
            matcher: Matcher.CONTAINS,
          };
          const expectedLastNameFilter: PropertyStringFilter = {
            '@type': 'propertyString',
            filterValue: 'filterLast',
            propertyId: 'lastName',
            matcher: Matcher.CONTAINS,
          };
          const expectedFilter: AndFilter = {
            '@type': 'and',
            children: [expectedFirstNameFilter, expectedLastNameFilter],
          };
          expect(service.lastFilter).to.deep.equal(expectedFilter);
        });
        it('removes filters if turning header filters off', async () => {
          const service = personService();
          const result = render(<TestAutoGrid service={service} model={PersonModel} />);
          let grid = await GridController.init(result, user);
          expect(grid.getHeaderRows().length).to.equal(2);

          const companyNameFilter = await TextFieldController.initByParent(grid.getHeaderCellContent(1, 0), user);
          await companyNameFilter.type('Joh');
          await clock.tickAsync(200);

          const filter: PropertyStringFilter = {
            '@type': 'propertyString',
            filterValue: 'Joh',
            matcher: Matcher.CONTAINS,
            propertyId: 'firstName',
          };
          const expectedFilter1: AndFilter = {
            '@type': 'and',
            children: [filter],
          };
          expect(service.lastFilter).to.deep.equal(expectedFilter1);

          result.rerender(<AutoGrid service={service} model={PersonModel} noHeaderFilters />);
          grid = await GridController.init(result, user);
          expect(grid.getHeaderRows().length).to.equal(1);

          const expectedFilter2: AndFilter = {
            '@type': 'and',
            children: [],
          };
          expect(service.lastFilter).to.deep.equal(expectedFilter2);
        });

        it('filters correctly after changing model', async () => {
          const _personService = personService();
          const _companyService = companyService();

          const result = render(<AutoGrid service={_personService} model={PersonModel} />);
          await GridController.init(result, user);
          result.rerender(<AutoGrid service={_companyService} model={CompanyModel} />);
          const grid = await GridController.init(result, user);

          const companyNameFilter = await TextFieldController.initByParent(grid.getHeaderCellContent(1, 0), user);
          await companyNameFilter.type('vaad');

          await clock.tickAsync(200);

          const expectedPropertyFilter: PropertyStringFilter = {
            '@type': 'propertyString',
            filterValue: 'vaad',
            propertyId: 'name',
            matcher: Matcher.CONTAINS,
          };
          const expectedFilter: AndFilter = { '@type': 'and', children: [expectedPropertyFilter] };
          expect(_personService.lastFilter).to.deep.equal(expectedFilter);
        });

        it('shows custom placeholder of filter', async () => {
          const grid = await GridController.init(
            render(
              <TestAutoGrid
                columnOptions={{
                  firstName: { filterPlaceholder: 'Custom placeholder' },
                  someInteger: { filterPlaceholder: 'Custom placeholder' },
                  birthDate: { filterPlaceholder: 'Custom placeholder' },
                  shiftStart: { filterPlaceholder: 'Custom placeholder' },
                }}
              />,
            ),
            user,
          );

          const firstNameFilterField = grid.getHeaderCellContent(1, 0).querySelector('vaadin-text-field')!;
          expect(firstNameFilterField.placeholder).to.deep.equal('Custom placeholder');
          const someIntegerFilterField = grid.getHeaderCellContent(1, 4).querySelector('vaadin-number-field')!;
          expect(someIntegerFilterField.placeholder).to.deep.equal('Custom placeholder');
          const birthDateFilterField = grid.getHeaderCellContent(1, 7).querySelector('vaadin-date-picker')!;
          expect(birthDateFilterField.placeholder).to.deep.equal('Custom placeholder');
          const shiftStartFilterField = grid.getHeaderCellContent(1, 8).querySelector('vaadin-time-picker')!;
          expect(shiftStartFilterField.placeholder).to.deep.equal('Custom placeholder');
        });

        it('waits custom debounce time to filter string column', async () => {
          const service = personService();
          const grid = await GridController.init(
            render(<TestAutoGrid columnOptions={{ firstName: { filterDebounceTime: 1000 } }} service={service} />),
            user,
          );

          const firstNameFilterField = grid.getHeaderCellContent(1, 0).querySelector('vaadin-text-field')!;
          firstNameFilterField.value = 'filter-value';
          firstNameFilterField.dispatchEvent(new CustomEvent('input'));

          expect(service.lastFilter).to.deep.equal({ '@type': 'and', children: [] });
          await clock.tickAsync(500);
          expect(service.lastFilter).to.deep.equal({ '@type': 'and', children: [] });
          await clock.tickAsync(500);

          const expectedPropertyFilter: PropertyStringFilter = {
            '@type': 'propertyString',
            filterValue: 'filter-value',
            propertyId: 'firstName',
            matcher: Matcher.CONTAINS,
          };
          const expectedFilter: AndFilter = { '@type': 'and', children: [expectedPropertyFilter] };
          expect(service.lastFilter).to.deep.equal(expectedFilter);
        });

        it('waits custom debounce time to filter number column', async () => {
          const service = personService();
          const grid = await GridController.init(
            render(<TestAutoGrid columnOptions={{ someInteger: { filterDebounceTime: 1000 } }} service={service} />),
            user,
          );

          const someNumberFilter = grid.getHeaderCellContent(1, 4);
          const [someNumberFilterField, someNumberFieldSelect] = await Promise.all([
            TextFieldController.initByParent(someNumberFilter, user, 'vaadin-number-field'),
            SelectController.init(someNumberFilter, user),
          ]);
          await someNumberFilterField.type('123');

          expect(service.lastFilter).to.deep.equal({ '@type': 'and', children: [] });
          await clock.tickAsync(500);
          expect(service.lastFilter).to.deep.equal({ '@type': 'and', children: [] });
          await clock.tickAsync(500);

          const expectedPropertyFilter: PropertyStringFilter = {
            '@type': 'propertyString',
            filterValue: '123',
            propertyId: 'someInteger',
            matcher: Matcher.GREATER_THAN,
          };
          const expectedFilter: AndFilter = { '@type': 'and', children: [expectedPropertyFilter] };
          expect(service.lastFilter).to.deep.equal(expectedFilter);
          clock.restore();
        });

        it('filters with custom min length of filter and clears filter', async () => {
          const service = personService();
          const grid = await GridController.init(
            render(<TestAutoGrid columnOptions={{ firstName: { filterMinLength: 3 } }} service={service} />),
            user,
          );

          const firstNameFilterField = grid.getHeaderCellContent(1, 0).querySelector('vaadin-text-field')!;
          firstNameFilterField.value = 'fi';
          firstNameFilterField.dispatchEvent(new CustomEvent('input'));
          await clock.tickAsync(200);

          expect(service.lastFilter).to.deep.equal({ '@type': 'and', children: [] });

          firstNameFilterField.value = 'filter-value';
          firstNameFilterField.dispatchEvent(new CustomEvent('input'));
          await clock.tickAsync(200);

          const expectedPropertyFilter: PropertyStringFilter = {
            '@type': 'propertyString',
            filterValue: 'filter-value',
            propertyId: 'firstName',
            matcher: Matcher.CONTAINS,
          };
          const expectedFilter: AndFilter = { '@type': 'and', children: [expectedPropertyFilter] };
          expect(service.lastFilter).to.deep.equal(expectedFilter);

          firstNameFilterField.value = 'fi';
          firstNameFilterField.dispatchEvent(new CustomEvent('input'));
          await clock.tickAsync(200);

          expect(service.lastFilter).to.deep.equal({ '@type': 'and', children: [] });
        });

        it('removes the filters when you clear the fields', async () => {
          const service = personService();
          const grid = await GridController.init(render(<TestAutoGrid service={service} />), user);
          const [firstNameFilter, lastNameFilter] = await Promise.all([
            TextFieldController.initByParent(grid.getHeaderCellContent(1, 0), user),
            TextFieldController.initByParent(grid.getHeaderCellContent(1, 1), user),
          ]);
          await firstNameFilter.type('filterFirst');
          await clock.tickAsync(200);
          await lastNameFilter.type('filterLast');
          await clock.tickAsync(200);

          const expectedFilter: AndFilter = {
            '@type': 'and',
            children: [],
          };
          expect(service.lastFilter).not.to.deep.equal(expectedFilter);

          await firstNameFilter.type('[Delete]');
          await clock.tickAsync(200);
          await lastNameFilter.type('[Delete]');
          await clock.tickAsync(200);
          expect(service.lastFilter).to.deep.equal(expectedFilter);
        });
      });
    });

    describe('customize columns', () => {
      it('should only show configured columns in specified order', async () => {
        const grid = await GridController.init(render(<TestAutoGrid visibleColumns={['email', 'firstName']} />), user);
        await assertColumns(grid, 'email', 'firstName');
      });

      it('should show columns that would be excluded by default', async () => {
        const grid = await GridController.init(render(<TestAutoGrid visibleColumns={['id', 'version']} />), user);
        await assertColumns(grid, 'id', 'version');
      });

      it('should show columns for nested properties that are not included by default', async () => {
        const grid = await GridController.init(render(<TestAutoGrid visibleColumns={['department.name']} />), user);
        await assertColumns(grid, 'department.name');
        expect(grid.getBodyCellContent(0, 0)).to.have.rendered.text('IT');
      });

      it('should ignore unknown columns', async () => {
        const grid = await GridController.init(
          render(
            <TestAutoGrid visibleColumns={['foo', 'email', 'bar', 'firstName', 'address.foo', 'department.foo']} />,
          ),
          user,
        );
        await assertColumns(grid, 'email', 'firstName');
      });

      it('uses custom column options on top of the type defaults', async () => {
        const NameRenderer = ({ item }: { item: Person }): JSX.Element => <span>{item.firstName.toUpperCase()}</span>;
        const StreetRenderer = ({ item }: { item: Person }): JSX.Element => (
          <span>{item.address?.street.toUpperCase()}</span>
        );
        const grid = await GridController.init(
          render(
            <TestAutoGrid
              columnOptions={{
                firstName: { renderer: NameRenderer },
                'address.street': { renderer: StreetRenderer },
              }}
            />,
          ),
          user,
        );
        await assertColumns(
          grid,
          'firstName',
          'lastName',
          'gender',
          'email',
          'someInteger',
          'someDecimal',
          'vip',
          'birthDate',
          'shiftStart',
          'appointmentTime',
          'address.street',
          'address.city',
          'address.country',
          'department',
        );
        expect(grid.getBodyCellContent(0, 0)).to.have.rendered.text('JANE');
        expect(grid.getBodyCellContent(0, 1)).to.have.rendered.text('Love');
        expect(grid.getBodyCellContent(0, 10)).to.have.rendered.text('122 SOUTH STREET');
        expect(grid.getBodyCellContent(0, 11)).to.have.rendered.text('South Town');
        // The header filter was not overridden
        const cell = grid.getHeaderCellContent(1, 0);
        expect(cell.firstElementChild!.querySelector('vaadin-text-field')).to.exist;
      });

      it('respects the header setting from custom column options', async () => {
        // With header filters
        let result = render(<TestAutoGrid columnOptions={{ firstName: { header: 'FIRSTNAME' } }} />);
        let grid = await GridController.init(result, user);
        expect(grid.getHeaderCellContent(0, 0).innerText).to.equal('FIRSTNAME');

        // Without header filters
        result.unmount();
        result = render(<TestAutoGrid noHeaderFilters columnOptions={{ firstName: { header: 'FIRSTNAME' } }} />);
        grid = await GridController.init(result, user);
        expect(grid.getHeaderCellContent(0, 0).innerText).to.equal('FIRSTNAME');
      });

      it('renders row numbers if requested', async () => {
        const grid = await GridController.init(render(<TestAutoGrid rowNumbers />), user);
        await assertColumns(
          grid,
          '',
          'firstName',
          'lastName',
          'gender',
          'email',
          'someInteger',
          'someDecimal',
          'vip',
          'birthDate',
          'shiftStart',
          'appointmentTime',
          'address.street',
          'address.city',
          'address.country',
          'department',
        );
        expect(grid.getBodyCellContent(0, 0)).to.have.rendered.text('1');
        expect(grid.getBodyCell(0, 0).style.flexGrow).to.equal('0');
      });
    });

    describe('custom columns', () => {
      const FullNameRenderer = ({ item }: { item: Person }): JSX.Element => (
        <span>
          {item.firstName} {item.lastName}
        </span>
      );
      const FullNameHyphenRenderer = ({ item }: { item: Person }): JSX.Element => (
        <span>
          {item.firstName}-{item.lastName}
        </span>
      );

      it('renders custom columns at the specified index by visibleColumns', async () => {
        const grid = await GridController.init(
          render(
            <TestAutoGrid
              noHeaderFilters
              visibleColumns={['fullName', 'gender', 'email', 'secondFullName', 'vip', 'birthDate', 'shiftStart']}
              customColumns={[
                <GridColumn key="fullName" header="Full name" autoWidth renderer={FullNameRenderer}></GridColumn>,
                <GridColumn
                  key="secondFullName"
                  header="Second full name"
                  autoWidth
                  renderer={FullNameHyphenRenderer}
                ></GridColumn>,
              ]}
            />,
          ),
          user,
        );
        await assertColumnsOrder(
          grid,
          'fullName',
          'gender',
          'email',
          'secondFullName',
          'vip',
          'birthDate',
          'shiftStart',
        );
        expect(grid.getBodyCellContent(0, 0)).to.have.rendered.text('Jane Love');
        expect(grid.getBodyCellContent(0, 3)).to.have.rendered.text('Jane-Love');
      });

      it('renders custom columns at the end if visibleColumns is absent', async () => {
        const grid = await GridController.init(
          render(
            <TestAutoGrid
              noHeaderFilters
              customColumns={[
                <GridColumn key="fullName" header="Full name" autoWidth renderer={FullNameRenderer}></GridColumn>,
                <GridColumn
                  key="secondFullName"
                  header="Second full name"
                  autoWidth
                  renderer={FullNameHyphenRenderer}
                ></GridColumn>,
              ]}
            />,
          ),
          user,
        );
        await assertColumnsOrder(
          grid,
          'firstName',
          'lastName',
          'gender',
          'email',
          'someInteger',
          'someDecimal',
          'vip',
          'birthDate',
          'shiftStart',
          'appointmentTime',
          'address.street',
          'address.city',
          'address.country',
          'department',
          'fullName',
          'secondFullName',
        );
        expect(grid.getBodyCellContent(0, 14)).to.have.rendered.text('Jane Love');
        expect(grid.getBodyCellContent(0, 15)).to.have.rendered.text('Jane-Love');
      });

      it('if visibleColumns is present, renders only the custom columns listed in visibleColumns', async () => {
        const grid = await GridController.init(
          render(
            <TestAutoGrid
              noHeaderFilters
              visibleColumns={['fullName', 'gender', 'email', 'vip', 'birthDate', 'shiftStart']}
              customColumns={[
                <GridColumn key="fullName" header="Full name" autoWidth renderer={FullNameRenderer}></GridColumn>,
                <GridColumn
                  key="secondFullName"
                  header="Second full name"
                  autoWidth
                  renderer={FullNameHyphenRenderer}
                ></GridColumn>,
              ]}
            />,
          ),
          user,
        );
        await assertColumnsOrder(grid, 'fullName', 'gender', 'email', 'vip', 'birthDate', 'shiftStart');
        expect(grid.getBodyCellContent(0, 0)).to.have.rendered.text('Jane Love');
      });

      describe('with header filters', () => {
        it('wraps custom columns in a column group and moves header text', async () => {
          const grid = await GridController.init(
            render(
              <TestAutoGrid
                visibleColumns={['fullName']}
                customColumns={[
                  <GridColumn key="fullName" header="Full name" autoWidth renderer={FullNameRenderer}></GridColumn>,
                ]}
              />,
            ),
            user,
          );
          // Group header row has header text
          expect(grid.getHeaderCellContent(0, 0)).to.have.rendered.text('Full name');
          // Column header row is empty
          expect(grid.getHeaderCellContent(1, 0)).to.be.rendered.empty;
          expect(grid.getHeaderCellContent(1, 0)).to.be.rendered.text('');
        });

        it('wraps custom columns in a column group and moves header renderer', async () => {
          function HeaderRenderer() {
            return <span>Full name</span>;
          }

          const grid = await GridController.init(
            render(
              <TestAutoGrid
                visibleColumns={['fullName']}
                customColumns={[
                  <GridColumn
                    key="fullName"
                    headerRenderer={HeaderRenderer}
                    autoWidth
                    renderer={FullNameRenderer}
                  ></GridColumn>,
                ]}
              />,
            ),
            user,
          );
          // Group header row has header text
          expect(grid.getHeaderCellContent(0, 0)).to.have.rendered.text('Full name');
          // Column header row is empty
          expect(grid.getHeaderCellContent(1, 0)).to.be.rendered.empty;
          expect(grid.getHeaderCellContent(1, 0)).to.be.rendered.text('');
        });
      });
    });

    describe('default renderers', () => {
      let grid: GridController;

      beforeEach(async () => {
        grid = await GridController.init(
          render(
            <LocaleContext.Provider value="en-US">
              <AutoGrid service={columnRendererTestService()} model={ColumnRendererTestModel} />,
            </LocaleContext.Provider>,
          ),
          user,
        );
      });

      it('renders strings without formatting and with default alignment', async () => {
        const columnIndex = await grid.findColumnIndexByHeaderText('String');
        expect(grid.getBodyCellContent(0, columnIndex)).to.have.style('text-align', 'start');
        expect(grid.getBodyCellContent(0, columnIndex)).to.have.rendered.text('Hello World 1');
        expect(grid.getBodyCellContent(1, columnIndex)).to.have.rendered.text('Hello World 2');
      });

      it('renders integers as right aligned numbers', async () => {
        const columnIndex = await grid.findColumnIndexByHeaderText('Integer');
        expect(grid.getBodyCellContent(0, columnIndex)).to.have.style('text-align', 'end');
        expect(grid.getBodyCellContent(0, columnIndex)).to.have.rendered.text('123,456');
        expect(grid.getBodyCellContent(1, columnIndex)).to.have.rendered.text('-12');
      });

      it('renders decimals as right aligned numbers', async () => {
        const columnIndex = await grid.findColumnIndexByHeaderText('Decimal');
        expect(grid.getBodyCellContent(0, columnIndex)).to.have.style('text-align', 'end');
        expect(grid.getBodyCellContent(0, columnIndex)).to.have.rendered.text('123.46');
        expect(grid.getBodyCellContent(1, columnIndex)).to.have.rendered.text('-0.12');
        expect(grid.getBodyCellContent(2, columnIndex)).to.have.rendered.text('123.40');
        expect(grid.getBodyCellContent(3, columnIndex)).to.have.rendered.text('-12.00');
      });

      it('renders booleans as icons', async () => {
        const columnIndex = await grid.findColumnIndexByHeaderText('Boolean');
        expect(grid.getBodyCellContent(0, columnIndex).querySelector('vaadin-icon')).to.have.attribute(
          'icon',
          'lumo:checkmark',
        );
        expect(grid.getBodyCellContent(1, columnIndex).querySelector('vaadin-icon')).to.have.attribute(
          'icon',
          'lumo:minus',
        );
      });

      it('renders enum values as title case', async () => {
        const columnIndex = await grid.findColumnIndexByHeaderText('Enum');
        expect(grid.getBodyCellContent(0, columnIndex)).to.have.rendered.text('Male');
        expect(grid.getBodyCellContent(1, columnIndex)).to.have.rendered.text('Female');
        expect(grid.getBodyCellContent(2, columnIndex)).to.have.rendered.text('Non Binary');
        expect(grid.getBodyCellContent(3, columnIndex)).to.have.rendered.text('');
      });

      it('renders java.time.LocalDate as right aligned', async () => {
        const columnIndex = await grid.findColumnIndexByHeaderText('Local date');
        expect(grid.getBodyCellContent(0, columnIndex)).to.have.style('text-align', 'end');
        expect(grid.getBodyCellContent(0, columnIndex)).to.have.text('5/13/2021');
        expect(grid.getBodyCellContent(1, columnIndex)).to.have.text('5/14/2021');
        expect(grid.getBodyCellContent(2, columnIndex)).to.have.text('');
        expect(grid.getBodyCellContent(3, columnIndex)).to.have.text('');
      });

      it('renders java.time.LocalTime as right aligned', async () => {
        const columnIndex = await grid.findColumnIndexByHeaderText('Local time');
        expect(grid.getBodyCellContent(0, columnIndex)).to.have.style('text-align', 'end');
        expect(grid.getBodyCellContent(0, columnIndex)).to.have.text('8:45 AM');
        expect(grid.getBodyCellContent(1, columnIndex)).to.have.text('8:45 PM');
        expect(grid.getBodyCellContent(2, columnIndex)).to.have.text('');
        expect(grid.getBodyCellContent(3, columnIndex)).to.have.text('');
      });

      it('renders java.time.LocalDateTime as right aligned', async () => {
        const columnIndex = await grid.findColumnIndexByHeaderText('Local date time');
        expect(grid.getBodyCellContent(0, columnIndex)).to.have.style('text-align', 'end');
        expect(grid.getBodyCellContent(0, columnIndex)).to.have.text('5/13/2021, 8:45 AM');
        expect(grid.getBodyCellContent(1, columnIndex)).to.have.text('5/14/2021, 8:45 PM');
        expect(grid.getBodyCellContent(2, columnIndex)).to.have.text('');
        expect(grid.getBodyCellContent(3, columnIndex)).to.have.text('');
      });

      it('renders nested strings without formatting and with default alignment', async () => {
        const columnIndex = await grid.findColumnIndexByHeaderText('Nested string');
        expect(grid.getBodyCellContent(0, columnIndex)).to.have.style('text-align', 'start');
        expect(grid.getBodyCellContent(0, columnIndex)).to.have.rendered.text('Nested string 1');
        expect(grid.getBodyCellContent(1, columnIndex)).to.have.rendered.text('');
      });

      it('renders nested numbers as right aligned numbers', async () => {
        const columnIndex = await grid.findColumnIndexByHeaderText('Nested number');
        expect(grid.getBodyCellContent(0, columnIndex)).to.have.style('text-align', 'end');
        expect(grid.getBodyCellContent(0, columnIndex)).to.have.rendered.text('123,456');
        expect(grid.getBodyCellContent(1, columnIndex)).to.have.rendered.text('');
      });

      it('renders nested booleans as icons', async () => {
        const columnIndex = await grid.findColumnIndexByHeaderText('Nested boolean');
        expect(grid.getBodyCellContent(0, columnIndex).querySelector('vaadin-icon')).to.have.attribute(
          'icon',
          'lumo:checkmark',
        );
        expect(grid.getBodyCellContent(1, columnIndex).querySelector('vaadin-icon')).to.have.attribute(
          'icon',
          'lumo:minus',
        );
      });

      it('renders nested java.util.Date as right aligned', async () => {
        const columnIndex = await grid.findColumnIndexByHeaderText('Nested date');
        expect(grid.getBodyCellContent(0, columnIndex)).to.have.style('text-align', 'end');
        expect(grid.getBodyCellContent(0, columnIndex)).to.have.text('5/13/2021');
        expect(grid.getBodyCellContent(1, columnIndex)).to.have.text('');
      });

      it('renders objects as JSON string', async () => {
        const service = personService();
        const person = (await getItem(service, 1))!;
        grid = await GridController.init(
          render(<AutoGrid service={service} model={PersonModel} visibleColumns={['address', 'department']} />),
          user,
        );

        // JSON is truncated to fifty chars
        // Assert that test data matches that
        const addressJson = JSON.stringify(person.address);
        expect(addressJson.length).to.be.greaterThan(50);
        const truncatedAddressJson = `${addressJson.substring(0, 50)}...`;
        expect(grid.getBodyCellContent(0, 0)).to.have.text(truncatedAddressJson);
        expect(grid.getBodyCellContent(0, 1)).to.have.text(JSON.stringify(person.department));
      });

      it('renders undefined objects as empty string', async () => {
        const service = createService(personData.map((p) => ({ ...p, address: undefined })));
        grid = await GridController.init(
          render(<AutoGrid service={service} model={PersonModel} visibleColumns={['address']} />),
          user,
        );

        expect(grid.getBodyCellContent(0, 0)).to.have.text('');
      });
    });

    describe('auto grid ref', () => {
      let autoGridRef: AutoGridRef;

      const AutoGridRefreshTestWrapper = ({ service }: { service: ListService<any> }) => {
        const ref = useRef<AutoGridRef>(null);
        useEffect(() => {
          if (ref.current) {
            autoGridRef = ref.current;
          }
        }, []);
        return (
          <span>
            <AutoGrid service={service} model={PersonModel} ref={ref} />
          </span>
        );
      };

      it('reloads data when refresh is called on ref', async () => {
        const service = personService();
        const listSpy = sinon.spy(service, 'list');
        render(<AutoGridRefreshTestWrapper service={service} />);
        await nextFrame();
        await nextFrame();
        expect(listSpy).to.have.been.calledOnce;
        autoGridRef.refresh();
        expect(listSpy).to.have.been.calledTwice;
      });

      it('exposes vaadin-grid element on ref', () => {
        render(<AutoGridRefreshTestWrapper service={personService()} />);
        expect(autoGridRef.grid).to.exist;
        expect(autoGridRef.grid!.localName).to.equal('vaadin-grid');
      });
    });

    describe('item id path', () => {
      it('properly configures item ID path for models that have an ID property', async () => {
        // Model with JPA annotations
        let grid = await GridController.init(render(<AutoGrid service={personService()} model={PersonModel} />), user);
        expect(grid.instance.itemIdPath).to.equal('id');

        // Model with simple ID property
        grid = await GridController.init(
          render(<AutoGrid service={personService()} model={PersonWithSimpleIdPropertyModel} />),
          user,
        );
        expect(grid.instance.itemIdPath).to.equal('id');

        // Model with custom ID property
        grid = await GridController.init(
          render(<AutoGrid service={personService()} model={PersonModel} itemIdProperty="email" />),
          user,
        );
        expect(grid.instance.itemIdPath).to.equal('email');

        // Model without discernible ID property
        grid = await GridController.init(
          render(<AutoGrid service={personService()} model={PersonWithoutIdPropertyModel} />),
          user,
        );
        expect(grid.instance.itemIdPath).to.be.undefined;
      });
    });
  });
});
