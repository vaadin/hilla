import type { ModelConstructor } from '@hilla/form';
import {
  Grid,
  GridProps,
  type GridDataProviderCallback,
  type GridDataProviderParams,
  type GridElement,
} from '@hilla/react-components/Grid.js';
import { GridSortColumn } from '@hilla/react-components/GridSortColumn.js';
import { useEffect, useRef } from 'react';
import type { CrudService } from './crud';
import { getProperties } from './modelutil.js';
import Sort from './types/dev/hilla/mappedtypes/Sort';
import Direction from './types/org/springframework/data/domain/Sort/Direction';

type _AutoGridProps<TItem> = {
  service: CrudService<TItem>;
  model: ModelConstructor<TItem, any>;
};
export type AutoGridProps<TItem> = GridProps<TItem> & _AutoGridProps<TItem>;

const createDataProvider = <TItem,>(grid: GridElement<TItem>, service: CrudService<TItem>) => {
  const listMethod = service.list;
  let first = true;

  return async (params: GridDataProviderParams<TItem>, callback: GridDataProviderCallback<TItem>) => {
    const sort: Sort = {
      orders: params.sortOrders.map((order) => ({
        property: order.path,
        direction: order.direction == 'asc' ? Direction.ASC : Direction.DESC,
        ignoreCase: false,
      })),
    };

    const pageNumber = params.page;
    const pageSize = params.pageSize;
    const req = {
      pageNumber,
      pageSize,
      sort,
    };

    const items = await listMethod(req);
    let size;
    if (items.length === pageSize) {
      size = (pageNumber + 1) * pageSize + 1;
      if (size < (grid as any)._cache.size) {
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
};

const createColumns = (model: ModelConstructor<any, any>) => {
  const properties = getProperties(model);

  const columns = properties.map((p) => {
    let customProps: any = { autoWidth: true };

    let column = (
      <GridSortColumn path={p.name} header={p.humanReadableName} key={p.name} {...customProps}></GridSortColumn>
    );

    return column;
  });
  return columns;
};

export function AutoGrid<TItem>(props: AutoGridProps<TItem>) {
  const { service, model, ...gridProps } = props;

  const ref = useRef(null);

  useEffect(() => {
    const grid = ref.current as any as GridElement<TItem>;
    grid.dataProvider = createDataProvider(grid, service);
  }, []);

  const children = createColumns(model);

  return <Grid {...gridProps} ref={ref} children={children}></Grid>;
}
