import type { ModelConstructor } from '@hilla/form';
import type { GridDataProviderCallback, GridDataProviderParams, GridElement } from '@hilla/react-components/Grid.js';
import { GridSortColumn } from '@hilla/react-components/GridSortColumn.js';
import { useEffect, useRef } from 'react';
import type { CrudEndpoint } from './crud';
import { getProperties } from './modelutil.js';

// import Sort from "Frontend/generated/dev/hilla/mappedtypes/Sort";
// import Direction from "Frontend/generated/org/springframework/data/domain/Sort/Direction";
type Sort = any;
enum Direction {
  ASC = 'ASC',
  DESC = 'DESC',
}

export const useAutoGrid = <T,>(endpoint: CrudEndpoint<T>, itemType: ModelConstructor<T, any>) => {
  const listMethod = endpoint.list;
  const ref = useRef(null);

  useEffect(() => {
    const grid = ref.current as any as GridElement<T>;

    let first = true;

    grid.dataProvider = async (params: GridDataProviderParams<T>, callback: GridDataProviderCallback<T>) => {
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
  }, []);

  const properties = getProperties(itemType);
  const children = properties.map((p) => {
    let customProps: any = { autoWidth: true };

    let column = (
      <GridSortColumn path={p.name} header={p.humanReadableName} key={p.name} {...customProps}></GridSortColumn>
    );

    return column;
  });

  return {
    ref,
    children: [...children],
  };
};
