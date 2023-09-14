import type { AbstractModel, ModelConstructor } from '@hilla/form';
import {
  Grid,
  type GridDataProvider,
  type GridDataProviderCallback,
  type GridDataProviderParams,
  type GridDefaultItem,
  type GridElement,
  type GridProps,
} from '@hilla/react-components/Grid.js';
import { GridSortColumn } from '@hilla/react-components/GridSortColumn.js';
import { useEffect, useRef, type JSX } from 'react';
import type { CrudService } from './crud';
import type Filter from './types/dev/hilla/crud/filter/Filter';
import type Sort from './types/dev/hilla/mappedtypes/Sort';
import Direction from './types/org/springframework/data/domain/Sort/Direction';
import { getProperties } from './utils.js';

export type AutoGridProps<TItem> = GridProps<TItem> &
  Readonly<{
    service: CrudService<TItem>;
    model: ModelConstructor<TItem, AbstractModel<TItem>>;
    filter?: Filter;
  }>;

type GridElementWithInternalAPI<TItem = GridDefaultItem> = GridElement<TItem> &
  Readonly<{
    _cache: {
      size?: number;
    };
  }>;

function createDataProvider<TItem>(
  grid: GridElement<TItem>,
  service: CrudService<TItem>,
  filter: React.MutableRefObject<Filter | undefined>,
): GridDataProvider<TItem> {
  let first = true;

  // eslint-disable-next-line @typescript-eslint/no-misused-promises
  return async (params: GridDataProviderParams<TItem>, callback: GridDataProviderCallback<TItem>) => {
    const sort: Sort = {
      orders: params.sortOrders.map((order) => ({
        property: order.path,
        direction: order.direction === 'asc' ? Direction.ASC : Direction.DESC,
        ignoreCase: false,
      })),
    };

    const pageNumber = params.page;
    const { pageSize } = params;
    const req = {
      pageNumber,
      pageSize,
      sort,
    };

    const items = await service.list(req, filter.current);
    let size;
    if (items.length === pageSize) {
      size = (pageNumber + 1) * pageSize + 1;

      const cacheSize = (grid as GridElementWithInternalAPI<TItem>)._cache.size;
      if (cacheSize !== undefined && size < cacheSize) {
        // Only allow size to grow here to avoid shrinking the size when scrolled down and sorting
        size = undefined;
      }
    } else {
      size = pageNumber * pageSize + items.length;
    }
    callback(items, size);
    if (first) {
      // Workaround for https://github.com/vaadin/react-components/issues/129
      first = false;
      setTimeout(() => grid.recalculateColumnWidths(), 0);
    }
  };
}

function createColumns(model: ModelConstructor<unknown, AbstractModel<unknown>>) {
  return getProperties(model).map((p) => (
    <GridSortColumn path={p.name} header={p.humanReadableName} key={p.name} autoWidth></GridSortColumn>
  ));
}

export function AutoGrid<TItem>({ service, model, filter, ...gridProps }: AutoGridProps<TItem>): JSX.Element {
  // This cast should go away with #1252
  const children = createColumns(model as ModelConstructor<unknown, AbstractModel<unknown>>);

  const ref = useRef<GridElement<TItem>>(null);
  const dataProviderFilter = useRef<Filter | undefined>(undefined);

  useEffect(() => {
    // Sets the data provider, should be done only once
    const grid = ref.current!;
    grid.dataProvider = createDataProvider(grid, service, dataProviderFilter);
  }, []);

  useEffect(() => {
    // Update the filtering, whenever the filter changes
    const grid = ref.current!;
    dataProviderFilter.current = filter;
    grid.clearCache();
  }, [filter]);

  return <Grid {...gridProps} ref={ref} children={children}></Grid>;
}
