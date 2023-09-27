import type { GridElement } from '@hilla/react-components/Grid.js';
import type { GridSorterDirection } from '@hilla/react-components/GridSorter.js';
import type { Grid } from '@vaadin/grid';
import type { SortState } from '../src/header-column-context.js';
// @ts-expect-error no types for the utils
import { getCellContent, getContainerCell, getPhysicalItems, getRowCells, getRows } from './grid-test-utils.js';

export const getBodyCellContent = <T>(grid: Grid<T>, row: number, col: number): HTMLElement => {
  const physicalItems = getPhysicalItems(grid);
  // eslint-disable-next-line
  const physicalRow = physicalItems.find((item: any) => item.index === row);
  const cells = getRowCells(physicalRow);
  return getCellContent(cells[col]);
};
export const getVisibleRowCount = <T>(grid: Grid<T>): number =>
  // eslint-disable-next-line
  (grid as any)._cache.size;

export const getHeaderRows = <T>(grid: Grid<T>): HTMLElement[] => {
  // eslint-disable-next-line
  const container = (grid as any).$.header;
  return getRows(container);
};
export const getHeaderCell = <T>(grid: Grid<T>, row: number, col: number): HTMLElement => {
  // eslint-disable-next-line
  const container = (grid as any).$.header;
  return getContainerCell(container, row, col);
};
export const getHeaderCellContent = <T>(grid: Grid<T>, row: number, col: number): HTMLElement =>
  getCellContent(getHeaderCell(grid, row, col));

export const sortGrid = (grid: GridElement, path: string, direction: GridSorterDirection): void => {
  const sorter = Array.from(grid.querySelectorAll('vaadin-grid-sorter')).find((gridSorter) => gridSorter.path === path);
  if (!sorter) {
    throw new Error(`No sorter found for path ${path}`);
  }
  sorter.direction = direction;
};
export const getSortOrder = (grid: GridElement): SortState | undefined => {
  const sorter = Array.from(grid.querySelectorAll('vaadin-grid-sorter')).find((gridSorter) => !!gridSorter.direction);
  if (sorter) {
    return { path: sorter.path!, direction: sorter.direction! };
  }
  return undefined;
};
