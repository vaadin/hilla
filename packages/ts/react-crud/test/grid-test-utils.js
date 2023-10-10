// From https://github.com/vaadin/web-components/blob/4c8a3f9402d5c0d4b93eb1b73b2f898bbfb904be/packages/grid/test/helpers.js
export const getCell = (grid, index) => grid.$.items.querySelectorAll('[part~="cell"]')[index];

export const getFirstCell = (grid) => getCell(grid, 0);

export const infiniteDataProvider = (params, callback) => {
  callback(
    Array.from({ length: params.pageSize }, (_, index) => ({
      value: `foo${params.page * params.pageSize + index}`,
    })),
  );
};

export const buildItem = (index) => ({
  index,
});

export const wheel = (target, deltaX, deltaY, ctrlKey) => {
  const e = new CustomEvent('wheel', { bubbles: true, cancelable: true });
  e.deltaX = deltaX;
  e.deltaY = deltaY;
  e.ctrlKey = ctrlKey;
  target.dispatchEvent(e);
  return e;
};

export const buildDataSet = (size) => {
  const data = [];
  while (data.length < size) {
    data.push(buildItem(data.length));
  }
  return data;
};

export const scrollToEnd = (grid, callback) => {
  grid.scrollToIndex(grid.size - 1);
  flushGrid(grid);
  if (callback) {
    callback();
  }
};

const isVisible = (item, grid) => {
  const scrollTarget = grid.shadowRoot.querySelector('table');
  const scrollTargetRect = scrollTarget.getBoundingClientRect();
  const itemRect = item.getBoundingClientRect();
  const offset = parseInt(getComputedStyle(item.firstElementChild).borderTopWidth);
  const headerHeight = grid.shadowRoot.querySelector('thead').offsetHeight;
  const footerHeight = grid.shadowRoot.querySelector('tfoot').offsetHeight;
  return (
    itemRect.bottom > scrollTargetRect.top + headerHeight + offset &&
    itemRect.top < scrollTargetRect.bottom - footerHeight - offset
  );
};

export const getPhysicalItems = (grid) =>
  Array.from(grid.shadowRoot.querySelector('tbody').children)
    .filter((item) => !item.hidden)
    .sort((a, b) => a.index - b.index);

export const getPhysicalAverage = (grid) => {
  const physicalItems = getPhysicalItems(grid);
  return physicalItems.map((el) => el.offsetHeight).reduce((sum, value) => sum + value, 0) / physicalItems.length;
};

export const scrollGrid = (grid, left, top) => {
  grid.shadowRoot.querySelector('table').scroll(left, top);
};

export const getVisibleItems = (grid) => {
  flushGrid(grid);
  return getPhysicalItems(grid).filter((item) => isVisible(item, grid));
};

export const getFirstVisibleItem = (grid) => getVisibleItems(grid)[0] || null;

export const getLastVisibleItem = (grid) => getVisibleItems(grid).pop() || null;

export const isWithinParentConstraints = (el, parent) => {
  const elRect = el.getBoundingClientRect();
  const parentRect = parent.getBoundingClientRect();
  return (
    elRect.top >= parentRect.top &&
    elRect.right <= parentRect.right &&
    elRect.bottom <= parentRect.bottom &&
    elRect.left >= parentRect.left
  );
};

export const getRows = (container) => container.querySelectorAll('tr');

export const getRowCells = (row) => Array.from(row.querySelectorAll('[part~="cell"]'));

export const getRowBodyCells = (row) => Array.from(row.querySelectorAll('[part~="cell"]:not([part~="details-cell"]'));

export const getCellContent = (cell) => (cell ? cell.querySelector('slot').assignedNodes()[0] : null);

export const getContainerCell = (container, row, col) => {
  const rows = getRows(container);
  const cells = getRowCells(rows[row]);
  return cells[col];
};

export const getContainerCellContent = (container, row, col) => getCellContent(getContainerCell(container, row, col));



export const flushGrid = (grid) => {
  grid._observer.flush();

  [
    grid.__updateColumnTreeDebouncer,
    grid._debounceScrolling,
    grid._debounceOverflow,
    grid._debouncerHiddenChanged,
    grid._debouncerApplyCachedData,
    grid.__debounceUpdateFrozenColumn,
  ].forEach((debouncer) => debouncer?.flush());

  grid.__virtualizer.flush();
};
