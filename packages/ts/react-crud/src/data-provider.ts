import type {
  GridDataProviderCallback,
  GridDataProviderParams,
  GridDefaultItem,
  GridElement,
} from '@hilla/react-components/Grid';
import type { CountService, ListService } from './crud';
import type FilterUnion from './types/dev/hilla/crud/filter/FilterUnion';
import type Sort from './types/dev/hilla/mappedtypes/Sort';
import Direction from './types/org/springframework/data/domain/Sort/Direction';

type GridElementWithInternalAPI<TItem = GridDefaultItem> = GridElement<TItem> &
  Readonly<{
    _dataProviderController: {
      rootCache: {
        size?: number;
      };
    };
  }>;

type MaybeCountService<TItem> = Partial<CountService<TItem>>;
type ListAndMaybeCountService<TItem> = ListService<TItem> & MaybeCountService<TItem>;

type PageRequest = {
  pageNumber: number;
  pageSize: number;
  sort: Sort;
};

type DataPage<TItem> = {
  items: TItem[];
  pageRequest: PageRequest;
};

export type ItemCounts = {
  totalCount?: number;
  filteredCount?: number;
};

type LoadCallback = (result: ItemCounts) => void;

function createSort<TItem>(params: GridDataProviderParams<TItem>): Sort {
  return {
    orders: params.sortOrders
      .filter((order) => order.direction != null)
      .map((order) => ({
        property: order.path,
        direction: order.direction === 'asc' ? Direction.ASC : Direction.DESC,
        ignoreCase: false,
      })),
  };
}

export function isCountService<TItem>(
  service: ListAndMaybeCountService<TItem>,
): service is CountService<TItem> & ListService<TItem> {
  return !!service.count;
}

export abstract class DataProvider<TItem> {
  protected readonly grid: GridElement;
  protected readonly service: ListAndMaybeCountService<TItem>;
  protected readonly loadCallback?: LoadCallback;

  protected filter: FilterUnion | undefined;
  protected totalCount: number | undefined;
  protected filteredCount: number | undefined;

  constructor(
    grid: GridElement,
    service: ListAndMaybeCountService<TItem>,
    filter: FilterUnion | undefined,
    loadCallback?: LoadCallback,
  ) {
    this.grid = grid;
    this.service = service;
    this.filter = filter;
    this.loadCallback = loadCallback;

    // eslint-disable-next-line @typescript-eslint/no-misused-promises
    this.grid.dataProvider = this.load.bind(this);
  }

  private async load(params: GridDataProviderParams<TItem>, callback: GridDataProviderCallback<TItem>) {
    // Fetch page, total count and filtered count
    const page = await this.fetchPage(params);
    if (this.totalCount === undefined) {
      this.totalCount = await this.fetchTotalCount(page);
    }
    if (this.filteredCount === undefined) {
      this.filteredCount = await this.fetchFilteredCount(page);
    }

    // Pass results to grid
    callback(page.items, this.filteredCount);

    // Pass results to callback
    if (this.loadCallback) {
      this.loadCallback({
        totalCount: this.totalCount,
        filteredCount: this.filteredCount,
      });
    }
  }

  private async fetchPage(params: GridDataProviderParams<TItem>): Promise<DataPage<TItem>> {
    const sort = createSort(params);
    const pageNumber = params.page;
    const { pageSize } = params;
    const pageRequest = {
      pageNumber,
      pageSize,
      sort,
    };
    const items = await this.service.list(pageRequest, this.filter);

    return { items, pageRequest };
  }

  abstract fetchTotalCount(page: DataPage<TItem>): Promise<number | undefined>;

  abstract fetchFilteredCount(page: DataPage<TItem>): Promise<number | undefined>;

  refresh(): void {
    this.totalCount = undefined;
    this.filteredCount = undefined;
    this.grid.clearCache();
  }

  setFilter(filter: FilterUnion | undefined): void {
    this.filter = filter;
    this.refresh();
  }
}

class InfiniteDataProvider<TItem> extends DataProvider<TItem> {
  // eslint-disable-next-line @typescript-eslint/require-await
  async fetchTotalCount(): Promise<number | undefined> {
    return undefined;
  }

  // eslint-disable-next-line @typescript-eslint/require-await
  async fetchFilteredCount(page: DataPage<TItem>): Promise<number | undefined> {
    const { items, pageRequest } = page;
    const { pageNumber, pageSize } = pageRequest;
    let infiniteScrollingSize;

    if (items.length === pageSize) {
      infiniteScrollingSize = (pageNumber + 1) * pageSize + 1;
      const cacheSize = (this.grid as GridElementWithInternalAPI<TItem>)._dataProviderController.rootCache.size;
      if (cacheSize !== undefined && infiniteScrollingSize < cacheSize) {
        // Only allow size to grow here to avoid shrinking the size when scrolled down and sorting
        infiniteScrollingSize = undefined;
      }
    } else {
      infiniteScrollingSize = pageNumber * pageSize + items.length;
    }

    return Promise.resolve(infiniteScrollingSize);
  }
}

class FixedSizeDataProvider<TItem> extends DataProvider<TItem> {
  async fetchTotalCount(): Promise<number | undefined> {
    if (!isCountService(this.service)) {
      throw new Error('The provided service does not implement the CountService interface.');
    }
    return this.service.count(undefined);
  }

  async fetchFilteredCount(): Promise<number | undefined> {
    if (!isCountService(this.service)) {
      throw new Error('The provided service does not implement the CountService interface.');
    }
    return this.service.count(this.filter);
  }
}

export function createDataProvider<TItem>(
  grid: GridElement,
  service: ListAndMaybeCountService<TItem>,
  filter: FilterUnion | undefined,
  loadCallback: LoadCallback,
): DataProvider<TItem> {
  if (isCountService(service)) {
    return new FixedSizeDataProvider(grid, service, filter, loadCallback);
  }
  return new InfiniteDataProvider(grid, service, filter, loadCallback);
}
