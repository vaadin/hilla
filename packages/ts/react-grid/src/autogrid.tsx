import type { AbstractModel, ModelConstructor } from '@hilla/form';
import {
  Grid,
  type GridProps,
  type GridDataProviderCallback,
  type GridDataProviderParams,
  type GridElement,
} from '@hilla/react-components/Grid.js';
import { GridSortColumn } from '@hilla/react-components/GridSortColumn.js';
import { useEffect, useRef } from 'react';
import type { CrudService } from './crud';
import { getProperties } from './modelutil.js';
import type Sort from './types/dev/hilla/mappedtypes/Sort';
import Direction from './types/org/springframework/data/domain/Sort/Direction';

type _AutoGridProps<TItem> = {
  service: CrudService<TItem>;
  model: ModelConstructor<TItem, AbstractModel<TItem>>;
};
export type AutoGridProps<TItem> = _AutoGridProps<TItem> & GridProps<TItem>;

const createDataProvider = <TItem,>(grid: GridElement<TItem>, service: CrudService<TItem>) => {
  let first = true;

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

    const items = await service.list(req);
    let size;
    if (items.length === pageSize) {
      size = (pageNumber + 1) * pageSize + 1;
      // eslint-disable-next-line
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
    const customProps: any = { autoWidth: true };

    const column = (
      <GridSortColumn path={p.name} header={p.humanReadableName} key={p.name} {...customProps}></GridSortColumn>
    );

    return column;
  });
  return columns;
};

export function AutoGrid<TItem>(props: AutoGridProps<TItem>): JSX.Element {
  const { service, model, ...gridProps } = props;

  const ref = useRef(null);

  useEffect(() => {
    const grid = ref.current as any as GridElement<TItem>;
    // eslint-disable-next-line
    grid.dataProvider = createDataProvider(grid, service);
  }, []);

  const children = createColumns(model);

  return <Grid {...gridProps} ref={ref} children={children}></Grid>;
}
