import type { GridElement } from '@hilla/react-components/Grid.js';
import type { GridSorterDirection } from '@hilla/react-components/GridSorter.js';
import type { RenderResult } from '@testing-library/react';
import type { Grid } from '@vaadin/grid';
import type { SortState } from '../src/autogrid-column-context.js';
// @ts-expect-error no types for the utils
import { getCellContent, getContainerCell, getPhysicalItems, getRowCells, getRows } from './grid-test-utils.js';

export const getBodyRow = (grid: Grid, row: number): HTMLTableRowElement => {
  const physicalItems = getPhysicalItems(grid);
  // eslint-disable-next-line
  return physicalItems.find((item: any) => item.index === row);
};
export const getBodyCellContent = <T>(grid: Grid<T>, row: number, col: number): HTMLElement => {
  const physicalRow = getBodyRow(grid, row);
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

export function getGrid(result: RenderResult): Grid {
  return result.container.querySelector('vaadin-grid')!;
}

export async function nextFrame(): Promise<void> {
  return new Promise((resolve) => {
    requestAnimationFrame(() => {
      resolve(undefined);
    });
  });
}

export async function setActiveItem<T>(grid: Grid<T>, item: T | undefined): Promise<void> {
  grid.activeItem = item;
  await nextFrame();
  await nextFrame();
  await nextFrame();
}
export function toggleRowSelected(grid: Grid, row: number): void {
  getBodyCellContent(grid, row, 0).click();
}
export function isSelected(grid: Grid, row: number): boolean {
  return getBodyRow(grid, row).part.contains('selected-row');
}

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
