import { GridSorter } from '@hilla/react-components/GridSorter.js';
import { useContext, type ReactElement } from 'react';
import { ColumnContext } from './header-column-context.js';

export function HeaderSorter(): ReactElement {
  const context = useContext(ColumnContext)!;

  const direction = context.sortState?.path === context.propertyInfo.name ? context.sortState.direction : null;
  return (
    <GridSorter
      path={context.propertyInfo.name}
      direction={direction}
      onDirectionChanged={(e) => {
        context.setSortState({ path: context.propertyInfo.name, direction: e.detail.value });
      }}
    >
      {context.propertyInfo.humanReadableName}
    </GridSorter>
  );
}
