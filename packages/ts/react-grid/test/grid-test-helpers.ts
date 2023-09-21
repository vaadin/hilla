import type { Grid } from '@vaadin/grid';
// @ts-expect-error no types for the utils
import { getCellContent, getPhysicalItems, getRowCells, getContainerCell, getRows } from './grid-test-utils.js';

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
