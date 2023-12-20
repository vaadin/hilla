import type {
  GridDataProviderCallback,
  GridDataProviderParams,
  GridDefaultItem,
  GridElement,
} from '@hilla/react-components/Grid';
import { useEffect, useRef, useState } from 'react';
import type { CountService, ListService } from './crud';
import type FilterUnion from './types/dev/hilla/crud/filter/FilterUnion';
import type Sort from './types/dev/hilla/mappedtypes/Sort';
import Direction from './types/org/springframework/data/domain/Sort/Direction';

export type GridElementWithInternalAPI<TItem = GridDefaultItem> = GridElement<TItem> &
  Readonly<{
    _dataProviderController: {
      rootCache: {
        size?: number;
      };
    };
  }>;

type MaybeCountService<TItem> = Partial<CountService<TItem>>;
type ListAndMaybeCountService<TItem> = ListService<TItem> & MaybeCountService<TItem>;
type ListAndCountService<TItem> = CountService<TItem> & ListService<TItem>;

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

type AfterLoadCallback = (result: ItemCounts) => void;

type DataProviderOptions = {
  initialFilter?: FilterUnion;
  loadTotalCount?: boolean;
  afterLoad?: AfterLoadCallback;
};

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

export function isCountService<TItem>(service: ListAndMaybeCountService<TItem>): service is ListAndCountService<TItem> {
  return !!service.count;
}

export abstract class DataProvider<TItem> {
  protected readonly grid: GridElement;
  protected readonly service: ListAndMaybeCountService<TItem>;
  protected readonly loadTotalCount?: boolean;
  protected readonly afterLoadCallback?: AfterLoadCallback;

  protected filter: FilterUnion | undefined;
  protected totalCount: number | undefined;
  protected filteredCount: number | undefined;

  constructor(grid: GridElement, service: ListAndMaybeCountService<TItem>, options: DataProviderOptions = {}) {
    this.grid = grid;
    this.service = service;
    this.filter = options.initialFilter;
    this.loadTotalCount = options.loadTotalCount;
    this.afterLoadCallback = options.afterLoad;

    // eslint-disable-next-line @typescript-eslint/no-misused-promises
    this.grid.dataProvider = this.load.bind(this);
  }

  refresh(): void {
    this.totalCount = undefined;
    this.filteredCount = undefined;
    this.grid.clearCache();
  }

  setFilter(filter: FilterUnion | undefined): void {
    this.filter = filter;
    this.refresh();
  }

  protected async load(
    params: GridDataProviderParams<TItem>,
    callback: GridDataProviderCallback<TItem>,
  ): Promise<void> {
    // Fetch page and filtered count
    const page = await this.fetchPage(params);
    this.filteredCount = await this.fetchFilteredCount(page);
    // Only fetch total count if it's specific in options
    if (this.loadTotalCount) {
      this.totalCount = await this.fetchTotalCount(page);
    }

    // Pass results to grid
    callback(page.items, this.filteredCount);

    // Pass results to callback
    if (this.afterLoadCallback) {
      this.afterLoadCallback({
        totalCount: this.totalCount,
        filteredCount: this.filteredCount,
      });
    }
  }

  protected async fetchPage(params: GridDataProviderParams<TItem>): Promise<DataPage<TItem>> {
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

  protected abstract fetchTotalCount(page: DataPage<TItem>): Promise<number | undefined> | number | undefined;

  protected abstract fetchFilteredCount(page: DataPage<TItem>): Promise<number | undefined> | number | undefined;
}

export class InfiniteDataProvider<TItem> extends DataProvider<TItem> {
  protected fetchTotalCount(): undefined {
    return undefined;
  }

  protected fetchFilteredCount(page: DataPage<TItem>): number | undefined {
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

    return infiniteScrollingSize;
  }
}

export class FixedSizeDataProvider<TItem> extends DataProvider<TItem> {
  declare service: ListAndCountService<TItem>;

  constructor(grid: GridElement, service: ListAndMaybeCountService<TItem>, options: DataProviderOptions = {}) {
    if (!isCountService(service)) {
      throw new Error('The provided service does not implement the CountService interface.');
    }
    super(grid, service, options);
  }

  protected async fetchTotalCount(): Promise<number | undefined> {
    // Use cached count if it's already known
    if (this.totalCount !== undefined) {
      return this.totalCount;
    }
    return this.service.count(undefined);
  }

  protected async fetchFilteredCount(): Promise<number | undefined> {
    // Use cached count if it's already known
    if (this.filteredCount !== undefined) {
      return this.filteredCount;
    }
    return this.service.count(this.filter);
  }
}

export function createDataProvider<TItem>(
  grid: GridElement,
  service: ListAndMaybeCountService<TItem>,
  options: DataProviderOptions = {},
): DataProvider<TItem> {
  if (isCountService(service)) {
    return new FixedSizeDataProvider(grid, service, options);
  }
  return new InfiniteDataProvider(grid, service, options);
}

export type UseDataProviderResult = Readonly<{
  /**
   * Should be passed to the grid's `ref` prop in order to set its data provider.
   * @param grid - the grid to set the data provider for
   */
  gridRef(grid: GridElement | null): void;
  /**
   * Refreshes the grid's data by reloading it again from the server.
   */
  refresh(): void;
}>;

/**
 * Creates a data provider for connecting a <Grid> to a Java backend service.
 * The data provider supports lazy loading, sorting and filtering.
 *
 * In order connect the grid to the data provider, pass the `gridRef` that is
 * returned from this hook to the grid's `ref` prop.
 *
 * Example:
 * ```tsx
 * const { gridRef } = useDataProvider(service);
 *
 * <Grid ref={gridRef}>
 *   ...
 * </Grid>
 * ```
 *
 * @param service - The service to use for fetching the data. This must be a
 * TypeScript service that has been generated by Hilla from a backend Java
 * service that implements the `dev.hilla.crud.ListService` interface.
 *
 * @param filter - The filter to use for fetching the data. Changing the filter
 * will cause the grid to reload the data.
 */
export function useDataProvider<TItem>(
  service: ListAndMaybeCountService<TItem>,
  filter?: FilterUnion,
): UseDataProviderResult {
  const [currentGrid, setCurrentGrid] = useState<GridElement | null>(null);
  const dataProviderRef = useRef<DataProvider<TItem>>();

  useEffect(() => {
    if (!currentGrid) {
      return;
    }
    // Delay setting data provider to allow applying custom props such as
    // pageSize to the grid element. It looks like the ref is always applied
    // before other props, so the setting the data provider would first load
    // pages with a default page size, and then reload with a custom page size.
    setTimeout(() => {
      dataProviderRef.current = createDataProvider(currentGrid, service, {
        initialFilter: filter,
      });
    });
  }, [currentGrid, service]);

  useEffect(() => {
    if (dataProviderRef.current) {
      dataProviderRef.current.setFilter(filter);
    }
  }, [filter]);

  return {
    gridRef: setCurrentGrid,
    refresh: () => {
      dataProviderRef.current?.refresh();
    },
  };
}
