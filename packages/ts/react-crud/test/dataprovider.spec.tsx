import { expect, use } from '@esm-bundle/chai';
import { render } from '@testing-library/react';
import type { GridDataProvider, GridSorterDefinition } from '@vaadin/react-components/Grid.js';
import { Grid } from '@vaadin/react-components/Grid.js';
import { useEffect } from 'react';
import sinon from 'sinon';
import sinonChai from 'sinon-chai';
import type { CountService, ListService } from '../crud';
import type { DataProvider } from '../src/data-provider';
import {
  createDataProvider,
  FixedSizeDataProvider,
  InfiniteDataProvider,
  useDataProvider,
  type ItemCounts,
} from '../src/data-provider';
import type AndFilter from '../types/com/vaadin/hilla/crud/filter/AndFilter';
import type FilterUnion from '../types/com/vaadin/hilla/crud/filter/FilterUnion';
import type PropertyStringFilter from '../types/com/vaadin/hilla/crud/filter/PropertyStringFilter';
import Matcher from '../types/com/vaadin/hilla/crud/filter/PropertyStringFilter/Matcher';
import type Pageable from '../types/com/vaadin/hilla/mappedtypes/Pageable';

use(sinonChai);

class MockGrid {
  pageSize = 10;
  loadSpy = sinon.spy();

  readonly dataProvider: GridDataProvider<any>;

  constructor(dataProvider: DataProvider<any>) {
    this.dataProvider = (params, callback) => {
      // eslint-disable-next-line no-void
      void dataProvider.load(params, (items, size) => {
        this.loadSpy(items, size);
        callback(items, size);
      });
    };
  }

  async requestPage(page: number, sortOrders: GridSorterDefinition[] = []): Promise<void> {
    return new Promise((resolve) => {
      this.dataProvider({ page, pageSize: this.pageSize, sortOrders, filters: [] }, (_, size) => {
        resolve();
      });
    });
  }
}

class MockGridWithGridDataProvider {
  pageSize = 10;

  readonly dataProvider: GridDataProvider<any>;

  constructor(gridDataProvider: GridDataProvider<any>) {
    this.dataProvider = gridDataProvider;
  }

  async requestPage(page: number, sortOrders: GridSorterDefinition[] = []): Promise<void> {
    return new Promise((resolve) => {
      this.dataProvider({ page, pageSize: this.pageSize, sortOrders, filters: [] }, (_, size) => {
        resolve();
      });
    });
  }
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
  grid: MockGrid,
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

async function testPageLoadForUseDataProvider(
  grid: MockGridWithGridDataProvider,
  serviceSpy: sinon.SinonSpy<[request: Pageable, filter: FilterUnion | undefined], Promise<number[]>>,
  pageNumber: number,
  filter?: FilterUnion | undefined,
) {
  serviceSpy.resetHistory();

  await grid.requestPage(pageNumber);

  expect(serviceSpy).to.have.been.calledOnce;
  expect(serviceSpy.lastCall.args[1]).to.equal(filter);
  const pageable = serviceSpy.lastCall.args[0];
  expect(pageable.pageNumber).to.equal(pageNumber);
  expect(pageable.pageSize).to.equal(grid.pageSize);
}

describe('@hilla/react-crud', () => {
  describe('useDataProvider', () => {
    let listSpy: sinon.SinonSpy<[request: Pageable, filter: FilterUnion | undefined], Promise<number[]>>;
    let listAndCountSpy: sinon.SinonSpy<[request: Pageable, filter: FilterUnion | undefined], Promise<number[]>>;

    beforeEach(() => {
      listSpy = sinon.spy(listService, 'list');
      listAndCountSpy = sinon.spy(listAndCountService, 'list');
    });

    afterEach(() => {
      listSpy.restore();
      listAndCountSpy.restore();
    });

    let dataProvider: GridDataProvider<any>;
    const GridWithDataProviderByHook = ({
      service,
      filter,
    }: {
      service: ListService<any> | (CountService<any> & ListService<any>);
      filter?: FilterUnion | undefined;
    }) => {
      const useDataProviderResult = useDataProvider(service, filter);
      useEffect(() => {
        // eslint-disable-next-line prefer-destructuring
        dataProvider = useDataProviderResult.dataProvider;
      }, []);
      return <Grid dataProvider={dataProvider} />;
    };

    it('load pages', async () => {
      render(<GridWithDataProviderByHook service={listService} />);

      const grid = new MockGridWithGridDataProvider(dataProvider);

      // First page
      await testPageLoadForUseDataProvider(grid, listSpy, 0);

      // Second page
      await testPageLoadForUseDataProvider(grid, listSpy, 1);

      // Last page
      await testPageLoadForUseDataProvider(grid, listSpy, 2);
    });

    it('passes filter to service', async () => {
      const filter = createTestFilter();

      render(<GridWithDataProviderByHook service={listService} filter={filter} />);

      const grid = new MockGridWithGridDataProvider(dataProvider);

      // First page
      await testPageLoadForUseDataProvider(grid, listSpy, 0, filter);

      // Second page
      await testPageLoadForUseDataProvider(grid, listSpy, 1, filter);
    });

    it('passes sort to service', async () => {
      let pageable: Pageable;
      render(<GridWithDataProviderByHook service={listService} />);
      const grid = new MockGridWithGridDataProvider(dataProvider);

      await grid.requestPage(0, [{ path: 'foo', direction: 'asc' }]);
      pageable = listSpy.lastCall.args[0];
      expect(pageable.sort).to.eql({ orders: [{ property: 'foo', direction: 'ASC', ignoreCase: false }] });

      await grid.requestPage(0, [{ path: 'bar', direction: 'desc' }]);
      pageable = listSpy.lastCall.args[0];
      expect(pageable.sort).to.eql({ orders: [{ property: 'bar', direction: 'DESC', ignoreCase: false }] });
    });

    it('works with a ListAndCountService', async () => {
      const filter = createTestFilter();
      render(<GridWithDataProviderByHook service={listAndCountService} filter={filter} />);
      const grid = new MockGridWithGridDataProvider(dataProvider);

      // First page
      await testPageLoadForUseDataProvider(grid, listAndCountSpy, 0, filter);

      // Second page
      await testPageLoadForUseDataProvider(grid, listAndCountSpy, 1, filter);

      await grid.requestPage(0);
      const passedFilter = listAndCountSpy.lastCall.args[1];
      expect(passedFilter).to.equal(filter);

      await grid.requestPage(0, [{ path: 'bar', direction: 'desc' }]);
      const pageable = listAndCountSpy.lastCall.args[0];
      expect(pageable.sort).to.eql({ orders: [{ property: 'bar', direction: 'DESC', ignoreCase: false }] });
    });
  });

  describe('createDataProvider', () => {
    it('creates InfiniteDataProvider for list service', () => {
      const dataProvider = createDataProvider(listService);
      expect(dataProvider).to.be.instanceOf(InfiniteDataProvider);
    });

    it('creates FixedSizeDataProvider for list and count service', () => {
      const dataProvider = createDataProvider(listAndCountService);
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
      const dataProvider = new InfiniteDataProvider(listService);
      const grid = new MockGrid(dataProvider);

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
      const dataProvider = new InfiniteDataProvider(listService);
      const grid = new MockGrid(dataProvider);

      await testPageLoad(grid, listSpy, 0, data.slice(0, 10), 11);
      await testPageLoad(grid, listSpy, 1, data.slice(10, 20), 21);
      await testPageLoad(grid, listSpy, 2, data.slice(20, 25), 25);

      // Should return previous size to prevent shrinking
      await testPageLoad(grid, listSpy, 1, data.slice(10, 20), 25);
    });

    it('returns correct item counts', async () => {
      const afterLoadSpy = sinon.spy();
      const dataProvider = new InfiniteDataProvider(listService, {
        afterLoad: afterLoadSpy,
      });
      const grid = new MockGrid(dataProvider);

      await grid.requestPage(0);
      expect(afterLoadSpy.lastCall.args[0]).to.eql({ totalCount: undefined, filteredCount: 11 });

      await grid.requestPage(1);
      expect(afterLoadSpy.lastCall.args[0]).to.eql({ totalCount: undefined, filteredCount: 21 });

      await grid.requestPage(2);
      expect(afterLoadSpy.lastCall.args[0]).to.eql({ totalCount: undefined, filteredCount: 25 });
    });

    it('passes sort to service', async () => {
      let pageable: Pageable;
      const dataProvider = new InfiniteDataProvider(listService);
      const grid = new MockGrid(dataProvider);

      await grid.requestPage(0, [{ path: 'foo', direction: 'asc' }]);
      pageable = listSpy.lastCall.args[0];
      expect(pageable.sort).to.eql({ orders: [{ property: 'foo', direction: 'ASC', ignoreCase: false }] });

      await grid.requestPage(0, [{ path: 'bar', direction: 'desc' }]);
      pageable = listSpy.lastCall.args[0];
      expect(pageable.sort).to.eql({ orders: [{ property: 'bar', direction: 'DESC', ignoreCase: false }] });
    });

    it('passes filter to service', async () => {
      const filter = createTestFilter();
      const dataProvider = new InfiniteDataProvider(listService, {
        initialFilter: filter,
      });
      const grid = new MockGrid(dataProvider);

      await grid.requestPage(0);
      const passedFilter = listSpy.lastCall.args[1];
      expect(passedFilter).to.equal(filter);
    });

    it('resets cached size when reset is called', async () => {
      const dataProvider = new InfiniteDataProvider(listService);
      const grid = new MockGrid(dataProvider);

      await grid.requestPage(0);
      await grid.requestPage(1);
      await grid.requestPage(2);
      expect(grid.loadSpy.lastCall.lastArg).to.equal(25);

      dataProvider.reset();

      await grid.requestPage(0);
      expect(grid.loadSpy.lastCall.lastArg).to.equal(11);
    });

    it('refreshes when filter is changed', async () => {
      const dataProvider = new InfiniteDataProvider(listService);
      const grid = new MockGrid(dataProvider);

      await grid.requestPage(0);
      await grid.requestPage(1);
      await grid.requestPage(2);
      expect(grid.loadSpy.lastCall.lastArg).to.equal(25);

      const filter = createTestFilter();
      dataProvider.setFilter(filter);

      await grid.requestPage(0);
      expect(grid.loadSpy.lastCall.lastArg).to.equal(11);
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
      expect(() => {
        const dataProvider = new FixedSizeDataProvider(listService);
      }).to.throw('The provided service does not implement the CountService interface.');
    });

    it('loads pages', async () => {
      const dataProvider = new FixedSizeDataProvider(listAndCountService);
      const grid = new MockGrid(dataProvider);

      // First page
      await testPageLoad(grid, listSpy, 0, data.slice(0, 10), 25);

      // Second page
      await testPageLoad(grid, listSpy, 1, data.slice(10, 20), 25);

      // Last page
      await testPageLoad(grid, listSpy, 2, data.slice(20, 25), 25);
    });

    it('returns correct item counts', async () => {
      const afterLoadSpy = sinon.spy() as sinon.SinonSpy<[result: ItemCounts], void>;
      const dataProvider = new FixedSizeDataProvider(listAndCountService, {
        afterLoad: afterLoadSpy,
      });
      const grid = new MockGrid(dataProvider);

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
      const afterLoadSpy = sinon.spy() as sinon.SinonSpy<[result: ItemCounts], void>;
      // Use filter to get different filtered count than total count
      const filter = createTestFilter();
      const dataProvider = new FixedSizeDataProvider(listAndCountService, {
        afterLoad: afterLoadSpy,
        loadTotalCount: true,
        initialFilter: filter,
      });
      const grid = new MockGrid(dataProvider);

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
      const dataProvider = new FixedSizeDataProvider(listAndCountService);
      const grid = new MockGrid(dataProvider);

      await grid.requestPage(0, [{ path: 'foo', direction: 'asc' }]);
      pageable = listSpy.lastCall.args[0];
      expect(pageable.sort).to.eql({ orders: [{ property: 'foo', direction: 'ASC', ignoreCase: false }] });

      await grid.requestPage(0, [{ path: 'bar', direction: 'desc' }]);
      pageable = listSpy.lastCall.args[0];
      expect(pageable.sort).to.eql({ orders: [{ property: 'bar', direction: 'DESC', ignoreCase: false }] });
    });

    it('passes filter to service', async () => {
      const filter = createTestFilter();
      const dataProvider = new FixedSizeDataProvider(listAndCountService, {
        initialFilter: filter,
      });
      const grid = new MockGrid(dataProvider);

      await grid.requestPage(0);
      const passedFilter = listSpy.lastCall.args[1];
      expect(passedFilter).to.equal(filter);
      expect(countSpy).to.have.been.calledOnceWithExactly(filter);
    });

    it('resets cached size when reset is called', async () => {
      const dataProvider = new FixedSizeDataProvider(listAndCountService);
      const grid = new MockGrid(dataProvider);

      await grid.requestPage(0);
      await grid.requestPage(1);
      await grid.requestPage(2);
      expect(grid.loadSpy.lastCall.lastArg).to.equal(25);
      expect(countSpy).to.have.been.calledOnce;

      dataProvider.reset();

      await grid.requestPage(0);
      expect(grid.loadSpy.lastCall.lastArg).to.equal(25);
      expect(countSpy).to.have.been.calledTwice;
    });

    it('refreshes when filter is changed', async () => {
      const dataProvider = new FixedSizeDataProvider(listAndCountService);
      const grid = new MockGrid(dataProvider);

      await grid.requestPage(0);
      await grid.requestPage(1);
      await grid.requestPage(2);
      expect(grid.loadSpy.lastCall.lastArg).to.equal(25);
      expect(countSpy).to.have.been.calledOnceWith(undefined);

      const filter = createTestFilter();
      dataProvider.setFilter(filter);

      await grid.requestPage(0);
      expect(grid.loadSpy.lastCall.lastArg).to.equal(10);
      expect(countSpy).to.have.been.calledTwice;
      expect(countSpy).to.have.been.calledWith(filter);
      expect(listSpy.lastCall.args[1]).to.equal(filter);
    });
  });
});
