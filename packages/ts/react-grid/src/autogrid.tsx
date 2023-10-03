import type { AbstractModel, DetachedModelConstructor } from '@hilla/form';
import {
  Grid,
  type GridDataProvider,
  type GridDataProviderCallback,
  type GridDataProviderParams,
  type GridDefaultItem,
  type GridElement,
  type GridProps,
} from '@hilla/react-components/Grid.js';
import { GridColumn } from '@hilla/react-components/GridColumn.js';
import { GridColumnGroup } from '@hilla/react-components/GridColumnGroup.js';
import { useEffect, useRef, useState, type JSX } from 'react';
import { ColumnContext, type SortState } from './autogrid-column-context.js';
import { getColumnProps } from './autogrid-columns.js';
import type { CrudService } from './crud';
import { HeaderSorter } from './header-sorter';
import { getIdProperty, getProperties, includeProperty, type PropertyInfo } from './property-info.js';
import type AndFilter from './types/dev/hilla/crud/filter/AndFilter';
import type Filter from './types/dev/hilla/crud/filter/Filter';
import type PropertyStringFilter from './types/dev/hilla/crud/filter/PropertyStringFilter';
import type Sort from './types/dev/hilla/mappedtypes/Sort';
import Direction from './types/org/springframework/data/domain/Sort/Direction';

export type AutoGridProps<TItem> = GridProps<TItem> &
  Readonly<{
    service: CrudService<TItem>;
    model: DetachedModelConstructor<AbstractModel<TItem>>;
    filter?: Filter;
    visibleColumns?: string[];
    noHeaderFilters?: boolean;
    refreshTrigger?: number;
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
      orders: params.sortOrders
        .filter((order) => order.direction != null)
        .map((order) => ({
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

function useColumns(
  properties: PropertyInfo[],
  setPropertyFilter: (propertyFilter: PropertyStringFilter) => void,
  options: { visibleColumns?: string[]; noHeaderFilters?: boolean },
) {
  const effectiveColumns = options.visibleColumns ?? properties.filter(includeProperty).map((p) => p.name);
  const effectiveProperties = effectiveColumns
    .map((name) => properties.find((prop) => prop.name === name))
    .filter(Boolean) as PropertyInfo[];

  const [sortState, setSortState] = useState<SortState | null>(
    effectiveProperties.length > 0 ? { path: effectiveProperties[0].name, direction: 'asc' } : null,
  );

  return effectiveProperties.map((propertyInfo) => {
    let column;
    // Header renderer is effectively the header filter, which should only be
    // applied when header filters are enabled
    const { headerRenderer, ...columnProps } = getColumnProps(propertyInfo);

    if (!options.noHeaderFilters) {
      column = (
        <GridColumnGroup headerRenderer={HeaderSorter}>
          <GridColumn path={propertyInfo.name} headerRenderer={headerRenderer} {...columnProps}></GridColumn>
        </GridColumnGroup>
      );
    } else {
      column = <GridColumn path={propertyInfo.name} headerRenderer={HeaderSorter} {...columnProps}></GridColumn>;
    }
    return (
      <ColumnContext.Provider
        key={propertyInfo.name}
        value={{ propertyInfo, setPropertyFilter, sortState, setSortState }}
      >
        {column}
      </ColumnContext.Provider>
    );
  });
}

export function AutoGrid<TItem>({
  service,
  model,
  filter,
  visibleColumns,
  noHeaderFilters,
  refreshTrigger = 0,
  ...gridProps
}: AutoGridProps<TItem>): JSX.Element {
  const [internalFilter, setInternalFilter] = useState<AndFilter>({ ...{ t: 'and' }, children: [] });

  const setHeaderPropertyFilter = (propertyFilter: PropertyStringFilter) => {
    const filterIndex = internalFilter.children.findIndex(
      (f) => (f as PropertyStringFilter).propertyId === propertyFilter.propertyId,
    );
    let changed = false;
    if (propertyFilter.filterValue === '') {
      // Delete empty filter
      if (filterIndex >= 0) {
        internalFilter.children.splice(filterIndex, 1);
        changed = true;
      }
    } else if (filterIndex >= 0) {
      internalFilter.children[filterIndex] = propertyFilter;
      changed = true;
    } else {
      internalFilter.children.push(propertyFilter);
      changed = true;
    }
    if (changed) {
      setInternalFilter({ ...internalFilter });
    }
  };

  const properties = getProperties(model);
  const children = useColumns(properties, setHeaderPropertyFilter, {
    visibleColumns,
    noHeaderFilters,
  });

  useEffect(() => {
    // Remove all filtering if header filters are removed
    if (noHeaderFilters) {
      setInternalFilter({ ...{ t: 'and' }, children: [] });
    }
  }, [noHeaderFilters]);

  const ref = useRef<GridElement<TItem>>(null);
  const dataProviderFilter = useRef<Filter | undefined>(undefined);

  useEffect(() => {
    // Sets the data provider, should be done only once
    const grid = ref.current!;
    setTimeout(() => {
      // Wait for the sorting headers to be rendered so that the sorting state is correct for the first data provider call
      grid.dataProvider = createDataProvider(grid, service, dataProviderFilter);
    }, 1);
  }, [model, service]);

  useEffect(() => {
    // Update the filtering, whenever the filter changes
    const grid = ref.current;
    if (grid) {
      dataProviderFilter.current = filter ?? internalFilter;
      grid.clearCache();
    }
  }, [filter, internalFilter, refreshTrigger]);

  return <Grid itemIdPath={getIdProperty(properties)?.name} {...gridProps} ref={ref} children={children}></Grid>;
}
