import { expect, use } from '@esm-bundle/chai';
import {
  Grid,
  type GridDataProvider,
  type GridElement,
  type GridSorterDefinition,
} from '@hilla/react-components/Grid.js';
import { GridSortColumn } from '@hilla/react-components/GridSortColumn';
import { render } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import sinon from 'sinon';
import sinonChai from 'sinon-chai';
import type { CountService, ListService } from '../crud';
import {
  createDataProvider,
  FixedSizeDataProvider,
  type GridElementWithInternalAPI,
  InfiniteDataProvider,
  type ItemCounts,
  type UseDataProviderResult,
  useDataProvider,
} from '../src/data-provider';
import type AndFilter from '../types/dev/hilla/crud/filter/AndFilter';
import type FilterUnion from '../types/dev/hilla/crud/filter/FilterUnion';
import type PropertyStringFilter from '../types/dev/hilla/crud/filter/PropertyStringFilter';
import Matcher from '../types/dev/hilla/crud/filter/PropertyStringFilter/Matcher';
import type Pageable from '../types/dev/hilla/mappedtypes/Pageable';
import { nextFrame } from './autogrid.spec';
import GridController from './GridController';

use(sinonChai);

class MockGrid {
  pageSize = 10;
  loadSpy = sinon.spy();
  clearCacheSpy = sinon.spy();

  private _dataProvider: GridDataProvider<any> | undefined;
  readonly _dataProviderController = {
    rootCache: {
      size: 0,
    },
  };

  get dataProvider(): GridDataProvider<any> | undefined {
    return this._dataProvider;
  }

  set dataProvider(dataProvider: GridDataProvider<any> | undefined) {
    if (!dataProvider) {
      throw new Error('Invalid test setup, dataProvider not set');
    }
    // Install spy to expose the loaded items and total size from callback
    this.loadSpy = sinon.spy();
    this._dataProvider = (params, callback) => {
      dataProvider(params, (items, size) => {
        this.loadSpy(items, size);
        callback(items, size);
      });
    };
  }

  clearCache() {
    this._dataProviderController.rootCache.size = 0;
    this.clearCacheSpy();
  }

  async requestPage(page: number, sortOrders: GridSorterDefinition[] = []): Promise<void> {
    return new Promise((resolve) => {
      if (!this.dataProvider) {
        throw new Error('Invalid test setup, dataProvider not set');
      }
      this.dataProvider({ page, pageSize: this.pageSize, sortOrders, filters: [] }, (_, size) => {
        this._dataProviderController.rootCache.size = size!;
        resolve();
      });
    });
  }
}

function mockGrid() {
  return new MockGrid() as unknown as GridElement & MockGrid;
}

const data = Array.from({ length: 25 }, (_, i) => i);

const listService: ListService<number> = {
  async list(request: Pageable, filter: FilterUnion | undefined): Promise<number[]> {
    const offset = request.pageNumber * request.pageSize;
    return Promise.resolve(data.slice(offset, offset + request.pageSize));
  },
};

const listAndCountService: CountService<number> & ListService<number> = {
  async list(request: Pageable, filter: FilterUnion | undefined): Promise<number[]> {
    const offset = request.pageNumber * request.pageSize;
    return Promise.resolve(data.slice(offset, offset + request.pageSize));
  },
  async count(filter: FilterUnion | undefined): Promise<number> {
    // If there is a filter, just return a different number than the total size
    if (filter) {
      return Promise.resolve(10);
    }
    return Promise.resolve(data.length);
  },
};

function createTestFilter(): FilterUnion {
  const filter1: PropertyStringFilter = {
    '@type': 'propertyString',
    propertyId: 'foo',
    filterValue: 'fooValue',
    matcher: Matcher.CONTAINS,
  };
  const filter2: PropertyStringFilter = {
    '@type': 'propertyString',
    propertyId: 'bar',
    filterValue: 'barValue',
    matcher: Matcher.CONTAINS,
  };
  const andFilter: AndFilter = {
    '@type': 'and',
    children: [filter1, filter2],
  };
  return andFilter;
}

async function testPageLoad(
  grid: GridElement & MockGrid,
  listSpy: sinon.SinonSpy<[request: Pageable, filter: FilterUnion | undefined], Promise<number[]>>,
  pageNumber: number,
  expectedItems: number[],
  expectedSize: number | undefined,
) {
  listSpy.resetHistory();
  grid.loadSpy.resetHistory();

  await grid.requestPage(pageNumber);

  expect(grid.loadSpy).to.have.been.calledOnce;
  expect(grid.loadSpy).to.have.been.calledWith(expectedItems, expectedSize);

  expect(listSpy).to.have.been.calledOnce;
  const pageable = listSpy.lastCall.args[0];
  expect(pageable.pageNumber).to.equal(pageNumber);
  expect(pageable.pageSize).to.equal(grid.pageSize);
}

describe('@hilla/react-crud', () => {
  describe('createDataProvider', () => {
    it('creates InfiniteDataProvider for list service', () => {
      const grid = mockGrid();
      const dataProvider = createDataProvider(grid, listService);
      expect(dataProvider).to.be.instanceOf(InfiniteDataProvider);
    });

    it('creates FixedSizeDataProvider for list and count service', () => {
      const grid = mockGrid();
      const dataProvider = createDataProvider(grid, listAndCountService);
      expect(dataProvider).to.be.instanceOf(FixedSizeDataProvider);
    });
  });

  describe('InfiniteDataProvider', () => {
    let listSpy: sinon.SinonSpy<[request: Pageable, filter: FilterUnion | undefined], Promise<number[]>>;

    beforeEach(() => {
      listSpy = sinon.spy(listService, 'list');
    });

    afterEach(() => {
      listSpy.restore();
    });

    it('loads pages', async () => {
      const grid = mockGrid();
      const dataProvider = new InfiniteDataProvider(grid, listService);

      // First page
      // Expected size is page size + 1
      await testPageLoad(grid, listSpy, 0, data.slice(0, 10), 11);

      // Second page
      // Expected size is cache size + page size + 1
      await testPageLoad(grid, listSpy, 1, data.slice(10, 20), 21);

      // Last page
      // Expected size is cache size + size of last page
      await testPageLoad(grid, listSpy, 2, data.slice(20, 25), 25);
    });

    it('prevents size from shrinking when requesting previous pages', async () => {
      const grid = mockGrid();
      const dataProvider = new InfiniteDataProvider(grid, listService);

      await testPageLoad(grid, listSpy, 0, data.slice(0, 10), 11);
      await testPageLoad(grid, listSpy, 1, data.slice(10, 20), 21);
      await testPageLoad(grid, listSpy, 2, data.slice(20, 25), 25);

      // Should return undefined for size to prevent shrinking
      await testPageLoad(grid, listSpy, 1, data.slice(10, 20), undefined);
    });

    it('returns correct item counts', async () => {
      const grid = mockGrid();
      const afterLoadSpy = sinon.spy();
      const dataProvider = new InfiniteDataProvider(grid, listService, {
        afterLoad: afterLoadSpy,
      });

      await grid.requestPage(0);
      expect(afterLoadSpy.lastCall.args[0]).to.eql({ totalCount: undefined, filteredCount: 11 });

      await grid.requestPage(1);
      expect(afterLoadSpy.lastCall.args[0]).to.eql({ totalCount: undefined, filteredCount: 21 });

      await grid.requestPage(2);
      expect(afterLoadSpy.lastCall.args[0]).to.eql({ totalCount: undefined, filteredCount: 25 });
    });

    it('passes sort to service', async () => {
      let pageable: Pageable;
      const grid = mockGrid();
      const dataProvider = new InfiniteDataProvider(grid, listService);

      await grid.requestPage(0, [{ path: 'foo', direction: 'asc' }]);
      pageable = listSpy.lastCall.args[0];
      expect(pageable.sort).to.eql({ orders: [{ property: 'foo', direction: 'ASC', ignoreCase: false }] });

      await grid.requestPage(0, [{ path: 'bar', direction: 'desc' }]);
      pageable = listSpy.lastCall.args[0];
      expect(pageable.sort).to.eql({ orders: [{ property: 'bar', direction: 'DESC', ignoreCase: false }] });
    });

    it('passes filter to service', async () => {
      const grid = mockGrid();
      const filter = createTestFilter();
      const dataProvider = new InfiniteDataProvider(grid, listService, {
        initialFilter: filter,
      });

      await grid.requestPage(0);
      const passedFilter = listSpy.lastCall.args[1];
      expect(passedFilter).to.equal(filter);
    });

    it('refreshes when refresh is called', async () => {
      const grid = mockGrid();
      const dataProvider = new InfiniteDataProvider(grid, listService);

      await grid.requestPage(0);
      await grid.requestPage(1);
      await grid.requestPage(2);
      expect(grid._dataProviderController.rootCache.size).to.equal(25);

      dataProvider.refresh();
      expect(grid.clearCacheSpy).to.have.been.calledOnce;

      await grid.requestPage(0);
      expect(grid._dataProviderController.rootCache.size).to.equal(11);
    });

    it('refreshes when filter is changed', async () => {
      const grid = mockGrid();
      const dataProvider = new InfiniteDataProvider(grid, listService);

      await grid.requestPage(0);
      await grid.requestPage(1);
      await grid.requestPage(2);
      expect(grid._dataProviderController.rootCache.size).to.equal(25);

      const filter = createTestFilter();
      dataProvider.setFilter(filter);
      expect(grid.clearCacheSpy).to.have.been.calledOnce;

      await grid.requestPage(0);
      expect(grid._dataProviderController.rootCache.size).to.equal(11);
      expect(listSpy.lastCall.args[1]).to.equal(filter);
    });
  });

  describe('FixedSizeDataProvider', () => {
    let listSpy: sinon.SinonSpy<[request: Pageable, filter: FilterUnion | undefined], Promise<number[]>>;
    let countSpy: sinon.SinonSpy<[filter: FilterUnion | undefined], Promise<number>>;

    beforeEach(() => {
      listSpy = sinon.spy(listAndCountService, 'list');
      countSpy = sinon.spy(listAndCountService, 'count');
    });

    afterEach(() => {
      listSpy.restore();
      countSpy.restore();
    });

    it('does not work with a ListService', () => {
      const grid = mockGrid();

      expect(() => {
        const dataProvider = new FixedSizeDataProvider(grid, listService);
      }).to.throw('The provided service does not implement the CountService interface.');
    });

    it('loads pages', async () => {
      const grid = mockGrid();
      const dataProvider = new FixedSizeDataProvider(grid, listAndCountService);

      // First page
      await testPageLoad(grid, listSpy, 0, data.slice(0, 10), 25);

      // Second page
      await testPageLoad(grid, listSpy, 1, data.slice(10, 20), 25);

      // Last page
      await testPageLoad(grid, listSpy, 2, data.slice(20, 25), 25);
    });

    it('returns correct item counts', async () => {
      const grid = mockGrid();
      const afterLoadSpy = sinon.spy() as sinon.SinonSpy<[result: ItemCounts], void>;
      const dataProvider = new FixedSizeDataProvider(grid, listAndCountService, {
        afterLoad: afterLoadSpy,
      });

      await grid.requestPage(0);
      expect(afterLoadSpy.lastCall.args[0]).to.eql({ totalCount: undefined, filteredCount: 25 });

      await grid.requestPage(1);
      expect(afterLoadSpy.lastCall.args[0]).to.eql({ totalCount: undefined, filteredCount: 25 });

      await grid.requestPage(2);
      expect(afterLoadSpy.lastCall.args[0]).to.eql({ totalCount: undefined, filteredCount: 25 });

      // Verify count was only called once for getting filtered count
      expect(countSpy).to.have.been.calledOnce;
      expect(countSpy).to.have.been.calledWith(undefined);
    });

    it('also returns total count when enabling it in options', async () => {
      const grid = mockGrid();
      const afterLoadSpy = sinon.spy() as sinon.SinonSpy<[result: ItemCounts], void>;
      // Use filter to get different filtered count than total count
      const filter = createTestFilter();
      const dataProvider = new FixedSizeDataProvider(grid, listAndCountService, {
        afterLoad: afterLoadSpy,
        loadTotalCount: true,
        initialFilter: filter,
      });

      await grid.requestPage(0);
      expect(afterLoadSpy.lastCall.args[0]).to.eql({ totalCount: 25, filteredCount: 10 });

      await grid.requestPage(1);
      expect(afterLoadSpy.lastCall.args[0]).to.eql({ totalCount: 25, filteredCount: 10 });

      await grid.requestPage(2);
      expect(afterLoadSpy.lastCall.args[0]).to.eql({ totalCount: 25, filteredCount: 10 });

      // Verify count was only called once for each the filtered count and total count
      expect(countSpy).to.have.been.calledTwice;
      expect(countSpy).to.have.been.calledWith(undefined);
      expect(countSpy).to.have.been.calledWith(filter);
    });

    it('passes sort to service', async () => {
      let pageable: Pageable;
      const grid = mockGrid();
      const dataProvider = new FixedSizeDataProvider(grid, listAndCountService);

      await grid.requestPage(0, [{ path: 'foo', direction: 'asc' }]);
      pageable = listSpy.lastCall.args[0];
      expect(pageable.sort).to.eql({ orders: [{ property: 'foo', direction: 'ASC', ignoreCase: false }] });

      await grid.requestPage(0, [{ path: 'bar', direction: 'desc' }]);
      pageable = listSpy.lastCall.args[0];
      expect(pageable.sort).to.eql({ orders: [{ property: 'bar', direction: 'DESC', ignoreCase: false }] });
    });

    it('passes filter to service', async () => {
      const grid = mockGrid();
      const filter = createTestFilter();
      const dataProvider = new FixedSizeDataProvider(grid, listAndCountService, {
        initialFilter: filter,
      });

      await grid.requestPage(0);
      const passedFilter = listSpy.lastCall.args[1];
      expect(passedFilter).to.equal(filter);
      expect(countSpy).to.have.been.calledOnceWithExactly(filter);
    });

    it('refreshes when refresh is called', async () => {
      const grid = mockGrid();
      const dataProvider = new FixedSizeDataProvider(grid, listAndCountService);

      await grid.requestPage(0);
      await grid.requestPage(1);
      await grid.requestPage(2);
      expect(countSpy).to.have.been.calledOnce;

      dataProvider.refresh();
      expect(grid.clearCacheSpy).to.have.been.calledOnce;

      await grid.requestPage(0);
      expect(countSpy).to.have.been.calledTwice;
    });

    it('refreshes when filter is changed', async () => {
      const grid = mockGrid();
      const dataProvider = new FixedSizeDataProvider(grid, listAndCountService);

      await grid.requestPage(0);
      await grid.requestPage(1);
      await grid.requestPage(2);
      expect(countSpy).to.have.been.calledOnceWith(undefined);

      const filter = createTestFilter();
      dataProvider.setFilter(filter);
      expect(grid.clearCacheSpy).to.have.been.calledOnce;

      await grid.requestPage(0);
      expect(countSpy).to.have.been.calledTwice;
      expect(countSpy).to.have.been.calledWith(filter);
      expect(listSpy.lastCall.args[1]).to.equal(filter);
    });
  });

  describe('useDataProvider', () => {
    type TestItem = { id: number; label: string };

    let user: ReturnType<(typeof userEvent)['setup']>;
    let testData: TestItem[];
    let testService: CountService<TestItem> & ListService<TestItem>;
    let listSpy: sinon.SinonSpy;
    let countSpy: sinon.SinonSpy;
    let dataProviderResult: UseDataProviderResult;

    function TestGrid({ service, filter }: { service: ListService<TestItem>; filter?: FilterUnion }) {
      dataProviderResult = useDataProvider(service, filter);

      return (
        <Grid ref={dataProviderResult.gridRef}>
          <GridSortColumn path="id" />
          <GridSortColumn path="label" />
          <GridSortColumn path="gender" />
        </Grid>
      );
    }

    beforeEach(() => {
      user = userEvent.setup();
      testData = Array.from({ length: 200 }, (_, i) => ({
        id: i + 1,
        label: `Label ${i + 1}`,
      }));
      testService = {
        async list(request: Pageable): Promise<any> {
          const offset = request.pageNumber * request.pageSize;
          return Promise.resolve(testData.slice(offset, offset + request.pageSize));
        },
        async count(): Promise<number> {
          return Promise.resolve(testData.length);
        },
      };
      listSpy = sinon.spy(testService, 'list');
      countSpy = sinon.spy(testService, 'count');
    });

    it('loads data for a custom grid', async () => {
      const result = render(<TestGrid service={testService} />);
      const grid = await GridController.init(result, user);

      // First page
      expect(grid.getBodyCellContent(0, 0)).to.have.text('1');
      expect(grid.getBodyCellContent(0, 1)).to.have.text('Label 1');
      expect(grid.getBodyCellContent(1, 0)).to.have.text('2');
      expect(grid.getBodyCellContent(1, 1)).to.have.text('Label 2');

      // Service calls
      expect(listSpy).to.have.been.calledOnce;
      expect(listSpy).to.have.been.calledWithMatch({ pageNumber: 0, pageSize: 50 });
      expect(countSpy).to.have.been.calledOnce;

      // Correct total size
      const totalSize = (grid.instance as GridElementWithInternalAPI)._dataProviderController.rootCache.size;
      expect(totalSize).to.equal(200);

      // Scroll down
      grid.instance.scrollToIndex(100);
      await nextFrame();
      expect(grid.getBodyCellContent(100, 0)).to.have.text('101');
      expect(grid.getBodyCellContent(100, 1)).to.have.text('Label 101');
      expect(grid.getBodyCellContent(101, 0)).to.have.text('102');
      expect(grid.getBodyCellContent(101, 1)).to.have.text('Label 102');

      // Service calls
      expect(listSpy).to.have.been.calledThrice;
      expect(listSpy).to.have.been.calledWithMatch({ pageNumber: 1, pageSize: 50 });
      expect(listSpy).to.have.been.calledWithMatch({ pageNumber: 2, pageSize: 50 });
      expect(countSpy).to.have.been.calledOnce;
    });

    it('refreshes when refresh is called', async () => {
      const result = render(<TestGrid service={testService} />);
      const grid = await GridController.init(result, user);

      // Initial render
      expect(grid.getBodyCellContent(0, 0)).to.have.text('1');
      expect(grid.getBodyCellContent(0, 1)).to.have.text('Label 1');
      expect(listSpy).to.have.been.calledOnce;
      expect(listSpy).to.have.been.calledWithMatch({ pageNumber: 0, pageSize: 50 });
      expect(countSpy).to.have.been.calledOnce;

      // Update data and refresh
      listSpy.resetHistory();
      countSpy.resetHistory();
      testData = testData.map((item) => ({
        ...item,
        label: `Updated ${item.id}`,
      }));
      dataProviderResult.refresh();
      await nextFrame();

      // Updated render
      expect(grid.getBodyCellContent(0, 0)).to.have.text('1');
      expect(grid.getBodyCellContent(0, 1)).to.have.text('Updated 1');
      expect(listSpy).to.have.been.calledOnce;
      expect(listSpy).to.have.been.calledWithMatch({ pageNumber: 0, pageSize: 50 });
      expect(countSpy).to.have.been.calledOnce;
    });

    it('uses an initial filter', async () => {
      const testFilter = createTestFilter();
      const result = render(<TestGrid service={testService} filter={testFilter} />);
      await GridController.init(result, user);

      expect(listSpy).to.have.been.calledOnce;
      expect(listSpy).to.have.been.calledWithMatch({ pageNumber: 0, pageSize: 50 }, testFilter);
      expect(countSpy).to.have.been.calledOnceWithExactly(testFilter);
    });

    it('allows to update the filter', async () => {
      const testFilter = createTestFilter();
      const result = render(<TestGrid service={testService} filter={testFilter} />);
      await GridController.init(result, user);

      // Initial service calls
      expect(listSpy).to.have.been.calledOnce;
      expect(listSpy).to.have.been.calledWithMatch({ pageNumber: 0, pageSize: 50 }, testFilter);
      expect(countSpy).to.have.been.calledOnceWithExactly(testFilter);

      // Re-render with new filter
      listSpy.resetHistory();
      countSpy.resetHistory();
      const newFilter = createTestFilter();
      result.rerender(<TestGrid service={testService} filter={newFilter} />);
      await nextFrame();

      // Updated service calls
      expect(listSpy).to.have.been.calledOnce;
      expect(listSpy).to.have.been.calledWithMatch({ pageNumber: 0, pageSize: 50 }, newFilter);
      expect(countSpy).to.have.been.calledOnceWithExactly(newFilter);
    });

    it('allows to sort the grid', async () => {
      const result = render(<TestGrid service={testService} />);
      const grid = await GridController.init(result, user);

      // Initial service calls
      expect(listSpy).to.have.been.calledOnce;
      expect(listSpy).to.have.been.calledWithMatch({ pageNumber: 0, pageSize: 50 });
      expect(countSpy).to.have.been.calledOnce;

      // Sort by label
      listSpy.resetHistory();
      countSpy.resetHistory();
      await grid.sort('label', 'desc');
      await nextFrame();

      // Updated service calls
      expect(listSpy).to.have.been.calledOnce;
      expect(listSpy).to.have.been.calledWithMatch({
        pageNumber: 0,
        pageSize: 50,
        sort: { orders: [{ property: 'label', direction: 'DESC', ignoreCase: false }] },
      });
      expect(countSpy).to.not.have.been.called;
    });
  });
});
