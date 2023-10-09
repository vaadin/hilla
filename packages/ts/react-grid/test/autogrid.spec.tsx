import { expect, use } from '@esm-bundle/chai';
import type { GridElement } from '@hilla/react-components/Grid.js';
import { GridColumn } from '@hilla/react-components/GridColumn.js';
import type { SelectElement } from '@hilla/react-components/Select.js';
import type { TextFieldElement } from '@hilla/react-components/TextField.js';
import { render, type RenderResult } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import chaiAsPromised from 'chai-as-promised';

import sinonChai from 'sinon-chai';
import { AutoGrid, type AutoGridProps } from '../src/autogrid.js';
import type AndFilter from '../src/types/dev/hilla/crud/filter/AndFilter.js';
import Matcher from '../src/types/dev/hilla/crud/filter/PropertyStringFilter/Matcher.js';
import type PropertyStringFilter from '../src/types/dev/hilla/crud/filter/PropertyStringFilter.js';
import GridController from './GridController.js';
import SelectController from './SelectController.js';
import {
  ColumnRendererTestModel,
  CompanyModel,
  PersonModel,
  columnRendererTestService,
  companyService,
  personService,
  type Person,
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

async function assertColumns(grid: GridController, ...ids: string[]) {
  const columns = await grid.getColumns();
  expect(columns).to.have.length(ids.length);
  await expect(grid.getHeaderCellContents()).to.eventually.deep.equal(grid.generateColumnHeaders(ids));

  for (let i = 0; i < ids.length; i++) {
    if (ids[i] === '') {
      expect(columns[i].path).to.equal(undefined);
    } else {
      expect(columns[i].path).to.equal(ids[i]);
    }
  }
}

describe('@hilla/react-grid', () => {
  describe('Auto grid', () => {
    function TestAutoGridNoHeaderFilters(customProps: Partial<AutoGridProps<Person>>) {
      return (
        <AutoGrid service={personService()} model={PersonModel} noHeaderFilters data-testid="grid" {...customProps} />
      );
    }
    function TestAutoGrid(customProps: Partial<AutoGridProps<Person>>) {
      return <AutoGrid service={personService()} model={PersonModel} data-testid="grid" {...customProps} />;
    }

    let user: ReturnType<(typeof userEvent)['setup']>;

    beforeEach(() => {
      user = userEvent.setup();
    });

    describe('basics', () => {
      it('creates columns based on model', async () => {
        const grid = await GridController.init(render(<TestAutoGridNoHeaderFilters />), user);
        await assertColumns(grid, 'firstName', 'lastName', 'email', 'someNumber', 'vip');
      });

      it('can change model and recreate columns', async () => {
        const result = render(<AutoGrid data-testid="grid" service={personService()} model={PersonModel} />);
        await assertColumns(
          await GridController.init(result, user),
          'firstName',
          'lastName',
          'email',
          'someNumber',
          'vip',
        );
        result.rerender(<AutoGrid data-testid="grid" service={companyService()} model={CompanyModel} />);
        await assertColumns(await GridController.init(result, user), 'name', 'foundedDate');
      });

      it('sorts according to first column by default', async () => {
        const grid = await GridController.init(render(<TestAutoGridNoHeaderFilters />), user);
        expect(grid.getBodyCellContent(0, 0)).to.have.property('innerText', 'Jane');
        expect(grid.getBodyCellContent(1, 0)).to.have.property('innerText', 'John');
      });
      it('retains sorting when re-rendering', async () => {
        const result = render(<TestAutoGridNoHeaderFilters />);
        let grid = await GridController.init(result, user);
        await grid.sort('lastName', 'desc');
        await expect(grid.getSortOrder()).to.eventually.be.deep.equal({ path: 'lastName', direction: 'desc' });
        result.rerender(<TestAutoGridNoHeaderFilters />);
        grid = await GridController.init(result, user);
        await expect(grid.getSortOrder()).to.eventually.deep.equal({ path: 'lastName', direction: 'desc' });
      });
      it('creates sortable columns', async () => {
        const grid = await GridController.init(render(<TestAutoGridNoHeaderFilters />), user);
        await grid.sort('firstName', 'desc');
        expect(grid.getBodyCellContent(0, 0)).to.have.property('innerText', 'John');
        expect(grid.getBodyCellContent(1, 0)).to.have.property('innerText', 'Jane');
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
        expect(grid.getBodyCellContent(0, 0)).to.have.property('innerText', 'Jane');
        expect(grid.getBodyCellContent(0, 1)).to.have.property('innerText', 'Love');
        expect(grid.getBodyCellContent(1, 0)).to.have.property('innerText', 'John');
        expect(grid.getBodyCellContent(1, 1)).to.have.property('innerText', 'Dove');
      });

      it('does not pass its own parameters to the underlying grid', async () => {
        const grid = await GridController.init(render(<TestAutoGridNoHeaderFilters />), user);
        expect(grid.instance.getAttribute('model')).to.be.null;
        expect(grid.instance.getAttribute('service')).to.be.null;
      });

      it('passes filter to the data provider', async () => {
        const filter: PropertyStringFilter = { filterValue: 'Jan', matcher: Matcher.CONTAINS, propertyId: 'firstName' };
        // @ts-expect-error: getting internal property
        // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
        filter.t = 'propertyString'; // Workaround for https://github.com/vaadin/hilla/issues/438

        const grid = await GridController.init(render(<TestAutoGrid filter={filter} />), user);
        expect(grid.getVisibleRowCount()).to.equal(1);
        expect(grid.getBodyCellContent(0, 0)).to.have.property('innerText', 'Jane');
        expect(grid.getBodyCellContent(0, 1)).to.have.property('innerText', 'Love');
      });

      describe('header filters', () => {
        it('created for string columns', async () => {
          const grid = await GridController.init(render(<TestAutoGrid />), user);
          const cell = grid.getHeaderCellContent(1, 0);
          expect(cell.firstElementChild?.localName).to.equal('vaadin-text-field');
        });

        it('created for number columns', async () => {
          const grid = await GridController.init(render(<TestAutoGrid />), user);
          const cell = grid.getHeaderCellContent(1, 3);
          expect(cell.firstElementChild?.localName).to.equal('vaadin-select');
        });

        it('filter when you type in the field for a string column', async () => {
          const service = personService();
          const grid = await GridController.init(render(<TestAutoGrid service={service} />), user);

          const firstNameFilterField = grid.getHeaderCellContent(1, 0).firstElementChild as TextFieldElement;
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
          expect(service.lastFilter).to.deep.equal(expectedFilter);
        });

        it('filter when you type in the field for a number column', async () => {
          const service = personService();
          const grid = await GridController.init(render(<TestAutoGrid service={service} />), user);
          const someNumberFilter = grid.getHeaderCellContent(1, 3);
          const [someNumberFilterField, someNumberFieldSelect] = await Promise.all([
            TextFieldController.initByParent(someNumberFilter, user, 'vaadin-number-field'),
            SelectController.init(someNumberFilter, user),
          ]);
          await someNumberFilterField.type('123');

          const expectedPropertyFilter: PropertyStringFilter & { t: string } = {
            t: 'propertyString',
            filterValue: '123',
            propertyId: 'someNumber',
            matcher: Matcher.GREATER_THAN,
          };
          const expectedFilter: AndFilter & { t: string } = { t: 'and', children: [expectedPropertyFilter] };
          expect(service.lastFilter).to.deep.equal(expectedFilter);

          await someNumberFieldSelect.select(Matcher.EQUALS);

          const expectedPropertyFilter2: PropertyStringFilter & { t: string } = {
            t: 'propertyString',
            filterValue: '123',
            propertyId: 'someNumber',
            matcher: Matcher.EQUALS,
          };

          const expectedFilter2: AndFilter & { t: string } = { t: 'and', children: [expectedPropertyFilter2] };
          expect(service.lastFilter).to.deep.equal(expectedFilter2);
        });

        it('filters for a boolean column', async () => {
          const service = personService();
          const grid = await GridController.init(render(<TestAutoGrid service={service} />), user);
          const controller = await SelectController.init(grid.getHeaderCellContent(1, 4), user);
          await controller.select('True');

          const expectedPropertyFilter: PropertyStringFilter = {
            ...{ t: 'propertyString' },
            filterValue: 'True',
            propertyId: 'vip',
            matcher: Matcher.EQUALS,
          };
          const expectedFilter: AndFilter = { ...{ t: 'and' }, children: [expectedPropertyFilter] };
          expect(service.lastFilter).to.deep.equal(expectedFilter);

          await controller.select('False');

          const expectedPropertyFilter2: PropertyStringFilter = {
            ...{ t: 'propertyString' },
            filterValue: 'False',
            propertyId: 'vip',
            matcher: Matcher.EQUALS,
          };
          const expectedFilter2: AndFilter = { ...{ t: 'and' }, children: [expectedPropertyFilter2] };
          expect(service.lastFilter).to.deep.equal(expectedFilter2);
        });

        it('combine filters (and) when you type in multiple fields', async () => {
          const service = personService();
          const grid = await GridController.init(render(<TestAutoGrid service={service} />), user);
          const firstNameFilterField = await TextFieldController.initByParent(grid.getHeaderCellContent(1, 0), user);
          await firstNameFilterField.type('filterFirst');
          const lastNameFilterField = await TextFieldController.initByParent(grid.getHeaderCellContent(1, 1), user);
          await lastNameFilterField.type('filterLast');

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
          expect(service.lastFilter).to.deep.equal(expectedFilter);
        });
        it('removes filters if turning header filters off', async () => {
          const service = personService();
          const result = render(<TestAutoGrid service={service} model={PersonModel} />);
          let grid = await GridController.init(result, user);
          expect(grid.getHeaderRows().length).to.equal(2);

          const companyNameFilter = await TextFieldController.initByParent(grid.getHeaderCellContent(1, 0), user);
          await companyNameFilter.type('Joh');

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
          expect(service.lastFilter).to.deep.equal(expectedFilter1);

          result.rerender(<AutoGrid data-testid="grid" service={service} model={PersonModel} noHeaderFilters />);
          grid = await GridController.init(result, user);
          expect(grid.getHeaderRows().length).to.equal(1);

          const expectedFilter2: AndFilter = {
            ...{ t: 'and' },
            children: [],
          };
          expect(service.lastFilter).to.deep.equal(expectedFilter2);
        });

        it('filters correctly after changing model', async () => {
          const _personService = personService();
          const _companyService = companyService();

          const result = render(<AutoGrid data-testid="grid" service={_personService} model={PersonModel} />);
          await GridController.init(result, user);
          result.rerender(<AutoGrid data-testid="grid" service={_companyService} model={CompanyModel} />);
          const grid = await GridController.init(result, user);

          const companyNameFilter = await TextFieldController.initByParent(grid.getHeaderCellContent(1, 0), user);
          await companyNameFilter.type('vaad');

          const expectedPropertyFilter: PropertyStringFilter = {
            ...{ t: 'propertyString' },
            filterValue: 'vaad',
            propertyId: 'name',
            matcher: Matcher.CONTAINS,
          };
          const expectedFilter: AndFilter = { ...{ t: 'and' }, children: [expectedPropertyFilter] };
          expect(_personService.lastFilter).to.deep.equal(expectedFilter);
        });
      });

      it('removes the filters when you clear the fields', async () => {
        const service = personService();
        const grid = await GridController.init(render(<TestAutoGrid service={service} />), user);
        const [firstNameFilter, lastNameFilter] = await Promise.all([
          TextFieldController.initByParent(grid.getHeaderCellContent(1, 0), user),
          TextFieldController.initByParent(grid.getHeaderCellContent(1, 1), user),
        ]);
        await firstNameFilter.type('filterFirst');
        await lastNameFilter.type('filterLast');

        const expectedFilter: AndFilter = {
          ...{ t: 'and' },
          children: [],
        };
        expect(service.lastFilter).not.to.deep.equal(expectedFilter);

        await firstNameFilter.type('[Delete]');
        await lastNameFilter.type('[Delete]');
        expect(service.lastFilter).to.deep.equal(expectedFilter);
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

      it('should ignore unknown columns', async () => {
        const grid = await GridController.init(
          render(<TestAutoGrid visibleColumns={['foo', 'email', 'bar', 'firstName']} />),
          user,
        );
        await assertColumns(grid, 'email', 'firstName');
      });

      it('renders custom columns at the end', async () => {
        const NameRenderer = ({ item }: { item: Person }): JSX.Element => (
          <span>
            {item.firstName} {item.lastName}
          </span>
        );
        const grid = await GridController.init(
          render(<TestAutoGrid customColumns={[<GridColumn autoWidth renderer={NameRenderer}></GridColumn>]} />),
          user,
        );
        await assertColumns(grid, 'firstName', 'lastName', 'email', 'someNumber', 'vip', '');
        expect(grid.getBodyCellContent(0, 5)).to.have.property('innerText', 'Jane Love');
      });
    });

    describe('default renderers', () => {
      let grid: GridController;

      beforeEach(async () => {
        grid = await GridController.init(
          render(
            <AutoGrid
              data-testid="grid"
              service={columnRendererTestService()}
              model={ColumnRendererTestModel}
            ></AutoGrid>,
          ),
          user,
        );
      });

      it('renders strings without formatting and with default alignment', () => {
        expect(grid.getBodyCellContent(0, 0)).to.have.style('text-align', 'start');
        expect(grid.getBodyCellContent(0, 0)).to.have.property('innerText', 'Hello World 1');
        expect(grid.getBodyCellContent(1, 0)).to.have.property('innerText', 'Hello World 2');
      });

      it('renders numbers as right aligned numbers', () => {
        expect(grid.getBodyCellContent(0, 1)).to.have.style('text-align', 'end');
        expect(grid.getBodyCellContent(0, 1)).to.have.property('innerText', '123,456');
        expect(grid.getBodyCellContent(1, 1)).to.have.property('innerText', '-12');
      });

      it('renders booleans as icons', () => {
        const vip = grid.getBodyCellContent(0, 2).querySelector('vaadin-icon')!;
        expect(vip).to.have.attribute('icon', 'lumo:checkmark');
        const notVip = grid.getBodyCellContent(1, 2).querySelector('vaadin-icon')!;
        expect(notVip).to.have.attribute('icon', 'lumo:minus');
      });

      it('renders java.util.Date as right aligned', () => {
        expect(grid.getBodyCellContent(0, 3)).to.have.style('text-align', 'end');
        expect(grid.getBodyCellContent(0, 3)).to.have.text('5/13/2021');
        expect(grid.getBodyCellContent(1, 3)).to.have.text('5/14/2021');
        expect(grid.getBodyCellContent(2, 3)).to.have.text('');
        expect(grid.getBodyCellContent(3, 3)).to.have.text('');
      });

      it('renders java.time.LocalDate as right aligned', () => {
        expect(grid.getBodyCellContent(0, 4)).to.have.style('text-align', 'end');
        expect(grid.getBodyCellContent(0, 4)).to.have.text('5/13/2021');
        expect(grid.getBodyCellContent(1, 4)).to.have.text('5/14/2021');
        expect(grid.getBodyCellContent(2, 4)).to.have.text('');
        expect(grid.getBodyCellContent(3, 4)).to.have.text('');
      });

      it('renders java.time.LocalTime as right aligned', () => {
        expect(grid.getBodyCellContent(0, 5)).to.have.style('text-align', 'end');
        expect(grid.getBodyCellContent(0, 5)).to.have.text('8:45 AM');
        expect(grid.getBodyCellContent(1, 5)).to.have.text('8:45 PM');
        expect(grid.getBodyCellContent(2, 5)).to.have.text('');
        expect(grid.getBodyCellContent(3, 5)).to.have.text('');
      });

      it('renders java.time.LocalDateTime as right aligned', () => {
        expect(grid.getBodyCellContent(0, 6)).to.have.style('text-align', 'end');
        expect(grid.getBodyCellContent(0, 6)).to.have.text('5/13/2021, 8:45 AM');
        expect(grid.getBodyCellContent(1, 6)).to.have.text('5/14/2021, 8:45 PM');
        expect(grid.getBodyCellContent(2, 6)).to.have.text('');
        expect(grid.getBodyCellContent(3, 6)).to.have.text('');
      });
    });
  });
});
