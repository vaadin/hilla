import type { GridElement } from '@hilla/react-components/Grid.js';
import type { GridColumnElement } from '@hilla/react-components/GridColumn.js';
import type { GridSorterDirection, GridSorterElement } from '@hilla/react-components/GridSorter.js';
import { type RenderResult, waitFor } from '@testing-library/react';
import type userEvent from '@testing-library/user-event';
import type Direction from '../src/types/org/springframework/data/domain/Sort/Direction.js';
// @ts-expect-error no types for the utils
import { getCellContent, getContainerCell, getPhysicalItems, getRowCells, getRows } from './grid-test-utils.js';
import type { Person } from './test-models-and-services.js';

export type IndexedHTMLTableRowElement = HTMLTableRowElement & { index: number };

export type SortOrder = Array<{ property: string; direction: Direction }>;

export default class GridController {
  readonly instance: GridElement<Person>;
  readonly #user: ReturnType<(typeof userEvent)['setup']>;

  static async init(result: RenderResult, user: ReturnType<(typeof userEvent)['setup']>): Promise<GridController> {
    const grid = await waitFor(() => result.container.querySelector('vaadin-grid')!);
    await waitFor(() => grid.shadowRoot?.querySelector('tbody')?.children);

    return new GridController(grid, user);
  }

  private constructor(grid: GridElement, user: ReturnType<(typeof userEvent)['setup']>) {
    this.instance = grid;
    this.#user = user;
  }

  getBodyRow(row: number): HTMLTableRowElement | undefined {
    const physicalItems = getPhysicalItems(this.instance) as readonly IndexedHTMLTableRowElement[];
    return physicalItems.find((item) => item.index === row);
  }

  async findColumnIndexByHeaderText(text: string): Promise<number> {
    const headers = await this.getHeaderCellContents();
    return headers.indexOf(text);
  }

  getBodyCell(row: number, col: number): HTMLElement {
    const physicalRow = this.getBodyRow(row);
    const cells = getRowCells(physicalRow) as readonly HTMLElement[];
    return cells[col];
  }

  getBodyCellContent(row: number, col: number): HTMLElement {
    const cell = this.getBodyCell(row, col);
    return getCellContent(cell);
  }

  getVisibleRowCount(): number {
    // @ts-expect-error: getting internal property
    // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
    return this.instance._cache.size;
  }

  getHeaderRows(): HTMLElement[] {
    // @ts-expect-error: getting internal property
    // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
    return getRows(this.instance.$.header);
  }

  getHeaderCell(row: number, col: number): HTMLElement {
    // @ts-expect-error: getting internal property
    // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
    return getContainerCell(this.instance.$.header, row, col);
  }

  async getColumns(): Promise<readonly GridColumnElement[]> {
    return Array.from(await waitFor(() => this.instance.querySelectorAll('vaadin-grid-column')));
  }

  async getHeaderCells(): Promise<readonly HTMLElement[]> {
    // @ts-expect-error: getting internal property
    // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
    return await this.getColumns().then((cols) => cols.map((_, i) => getContainerCell(this.instance.$.header, 0, i)));
  }

  async getHeaderCellContents(): Promise<readonly string[]> {
    const cells = await this.getHeaderCells();
    return cells.map((cell) => (getCellContent(cell) as HTMLElement).innerText);
  }

  generateColumnHeaders(paths: readonly string[]): readonly string[] {
    return paths.map((path) =>
      path
        .substring(path.lastIndexOf('.') + 1)
        .replace(/([A-Z])/gu, '-$1')
        .toLowerCase()
        .replace(/-/gu, ' ')
        .replace(/^./u, (match) => match.toUpperCase()),
    );
  }

  getHeaderCellContent(row: number, col: number): HTMLElement {
    return getCellContent(this.getHeaderCell(row, col));
  }

  async setActiveItem(item: Person | undefined): Promise<void> {
    await new Promise<void>((resolve) => {
      this.instance.addEventListener('active-item-changed', () => resolve(), { once: true });
      this.instance.activeItem = item;
    });
  }

  async toggleRowSelected(row: number): Promise<void> {
    await this.#user.click(this.getBodyCellContent(row, 0));
  }

  isSelected(row: number): boolean {
    return !!this.getBodyRow(row)?.part.contains('selected-row');
  }

  async sort(path: string, direction: GridSorterDirection): Promise<void> {
    const sorter = (await this.#getSorter()).find((gridSorter) => gridSorter.path === path);

    if (!sorter) {
      throw new Error(`No sorter found for path ${path}`);
    }

    sorter.direction = direction;
  }

  async getSortOrder(): Promise<SortOrder | undefined> {
    const sorters = Array.from(await waitFor(() => this.instance.querySelectorAll('vaadin-grid-sorter')!));
    const activeSorters = sorters
      .filter((gridSorter) => !!gridSorter.direction)
      // @ts-expect-error: accessing internal property
      .sort((a, b) => a._order - b._order);

    return activeSorters.map((gridSorter) => {
      const { direction, path } = gridSorter;
      return { property: path!, direction: direction?.toUpperCase() as Direction };
    });
  }

  async #getSorter(): Promise<readonly GridSorterElement[]> {
    return Array.from(await waitFor(() => Array.from(this.instance.querySelectorAll('vaadin-grid-sorter'))));
  }
}
