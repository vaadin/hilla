import { GridSorter, type GridSorterChangedEvent, type GridSorterElement } from '@hilla/react-components/GridSorter.js';
import { useContext, type ReactElement, useRef, useEffect } from 'react';
import { HeaderColumnContext } from './header-column-context.js';

export function HeaderSorter(): ReactElement {
  const ref = useRef<GridSorterElement>(null);
  const context = useContext(HeaderColumnContext)!;
  const direction = context.sortState?.path === context.propertyInfo.name ? context.sortState.direction : null;

  useEffect(() => {
    // Add listener for sorter-changed event, needs to be done on the DOM
    // element as there is no React API for it.
    const sorter = ref.current;
    if (!sorter) {
      return;
    }

    sorter.addEventListener('sorter-changed', (event: GridSorterChangedEvent) => {
      if (!event.detail.fromSorterClick) {
        return;
      }

      if (!sorter.direction) {
        context.setSortState(null);
      } else {
        context.setSortState({
          path: context.propertyInfo.name,
          direction: sorter.direction,
        });
      }
    });
  }, []);

  return (
    <GridSorter path={context.propertyInfo.name} direction={direction} ref={ref}>
      {context.propertyInfo.humanReadableName}
    </GridSorter>
  );
}
