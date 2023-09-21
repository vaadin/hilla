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
import { GridColumn } from '@hilla/react-components/GridColumn.js';
import { GridColumnGroup } from '@hilla/react-components/GridColumnGroup.js';
import { GridSortColumn } from '@hilla/react-components/GridSortColumn.js';
import { GridSorter } from '@hilla/react-components/GridSorter.js';
import { useCallback, useEffect, useRef, useState, type JSX } from 'react';
import type { CrudService } from './crud';
import { createFilterField } from './field-factory';
import type AndFilter from './types/dev/hilla/crud/filter/AndFilter';
import type Filter from './types/dev/hilla/crud/filter/Filter';
import type PropertyStringFilter from './types/dev/hilla/crud/filter/PropertyStringFilter';
import Matcher from './types/dev/hilla/crud/filter/PropertyStringFilter/Matcher';
import type Sort from './types/dev/hilla/mappedtypes/Sort';
import Direction from './types/org/springframework/data/domain/Sort/Direction';
import { getProperties, type PropertyInfo } from './utils.js';

export type AutoGridProps<TItem> = GridProps<TItem> &
  Readonly<{
    service: CrudService<TItem>;
    model: ModelConstructor<TItem, AbstractModel<TItem>>;
    filter?: Filter;
    visibleColumns?: string[];
    headerFilters?: boolean;
  }>;

type GridElementWithInternalAPI<TItem = GridDefaultItem> = GridElement<TItem> &
  Readonly<{
    _cache: {
      size?: number;
    };
  }>;

function pathFromColumn(column: any): string {
  // eslint-disable-next-line
  return column?.original?.path ?? column?.original?.dataset?.path;
}

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

function useHeaderFilterRenderer(
  properties: React.MutableRefObject<PropertyInfo[]>,
  setPropertyFilter: React.MutableRefObject<(propertyFilter: PropertyStringFilter) => void>,
) {
  return useCallback((column: any) => {
    const path = pathFromColumn(column);
    if (!path) {
      return null;
    }
    const propertyInfo: PropertyInfo = properties.current.find((p) => p.name === path)!;

    return createFilterField(propertyInfo, {
      onInput: (e: { target: { value: string } }) => {
        const fieldValue = e.target.value;
        const filterValue = fieldValue;

        const filter = {
          propertyId: propertyInfo.name,
          filterValue,
          matcher: Matcher.CONTAINS,
        };

        // eslint-disable-next-line
        (filter as any).t = 'propertyString';
        setPropertyFilter.current(filter);
      },
    });
  }, []);
}

function useHeaderSorterRenderer(properties: React.MutableRefObject<PropertyInfo[]>) {
  return useCallback((column: any) => {
    const path = pathFromColumn(column);
    if (!path) {
      return null;
    }

    const propertyInfo: PropertyInfo = properties.current.find((p) => p.name === path)!;

    return <GridSorter path={path}>{propertyInfo.humanReadableName}</GridSorter>;
  }, []);
}

function useColumns(
  model: ModelConstructor<unknown, AbstractModel<unknown>>,
  setPropertyFilter: (propertyFilter: PropertyStringFilter) => void,
  options: { visibleColumns?: string[]; headerFilters?: boolean },
) {
  const properties = getProperties(model);
  const effectiveColumns = options.visibleColumns ?? properties.map((p) => p.name);
  const effectiveProperties = effectiveColumns
    .map((name) => properties.find((prop) => prop.name === name))
    .filter(Boolean) as PropertyInfo[];
  const propertiesRef = useRef<PropertyInfo[]>([]);
  propertiesRef.current = properties;
  const setPropertyFilterRef = useRef(setPropertyFilter);
  setPropertyFilterRef.current = setPropertyFilter;
  const headerFilterRenderer = useHeaderFilterRenderer(propertiesRef, setPropertyFilterRef);
  const headerSorterRenderer = useHeaderSorterRenderer(propertiesRef);

  return effectiveProperties.map((p) => {
    if (options.headerFilters) {
      return (
        <GridColumnGroup key={`group-${p.name}`} data-path={p.name} headerRenderer={headerSorterRenderer}>
          <GridColumn path={p.name} headerRenderer={headerFilterRenderer} autoWidth></GridColumn>
        </GridColumnGroup>
      );
    }
    return (
      <GridColumn key={`col-${p.name}`} path={p.name} headerRenderer={headerSorterRenderer} autoWidth></GridColumn>
    );
  });
}

export function AutoGrid<TItem>({
  service,
  model,
  filter,
  visibleColumns,
  headerFilters,
  ...gridProps
}: AutoGridProps<TItem>): JSX.Element {
  const [internalFilter, setInternalFilter] = useState<AndFilter>({ ...{ t: 'and' }, children: [] });

  const setHeaderPropertyFilter = (propertyFilter: PropertyStringFilter) => {
    const filterIndex = internalFilter.children.findIndex(
      (f) => (f as PropertyStringFilter).propertyId === propertyFilter.propertyId,
    );
    if (propertyFilter.filterValue === '') {
      // Delete empty filter
      if (filterIndex >= 0) {
        internalFilter.children.splice(filterIndex, 1);
      }
    } else if (filterIndex >= 0) {
      internalFilter.children[filterIndex] = propertyFilter;
    } else {
      internalFilter.children.push(propertyFilter);
    }
    setInternalFilter({ ...internalFilter });
  };

  // This cast should go away with #1252
  const children = useColumns(model as ModelConstructor<unknown, AbstractModel<unknown>>, setHeaderPropertyFilter, {
    visibleColumns,
    headerFilters,
  });

  useEffect(() => {
    // Remove all filtering if header filters are removed
    if (!headerFilters) {
      setInternalFilter({ ...{ t: 'and' }, children: [] });
    }
  }, [headerFilters]);

  const ref = useRef<GridElement<TItem>>(null);
  const dataProviderFilter = useRef<Filter | undefined>(undefined);

  useEffect(() => {
    // Sets the data provider, should be done only once
    const grid = ref.current!;
    grid.dataProvider = createDataProvider(grid, service, dataProviderFilter);
  }, [model, service]);

  useEffect(() => {
    // Update the filtering, whenever the filter changes
    const grid = ref.current;
    if (grid) {
      dataProviderFilter.current = filter ?? internalFilter;
      grid.clearCache();
    }
  }, [filter, internalFilter]);

  return <Grid {...gridProps} ref={ref} children={children}></Grid>;
}
