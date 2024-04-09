import { GridSorter } from '@vaadin/react-components/GridSorter.js';
import { useContext, type ReactElement } from 'react';
import { ColumnContext } from './autogrid-column-context.js';

export function HeaderSorter(): ReactElement {
  const context = useContext(ColumnContext)!;
  const sorterState = context.sortState[context.propertyInfo.name];
  const direction = sorterState?.direction ?? null;
  const headerLabel = context.customColumnOptions?.header ?? context.propertyInfo.humanReadableName;

  return context.customColumnOptions?.sortable === false ? (
    <>{headerLabel}</>
  ) : (
    <GridSorter
      path={context.propertyInfo.name}
      direction={direction}
      onDirectionChanged={(e) => {
        context.setSortState((prevState) => {
          const newSorterState = e.detail.value ? { direction: e.detail.value } : undefined;
          return { ...prevState, [context.propertyInfo.name]: newSorterState };
        });
      }}
    >
      {headerLabel}
    </GridSorter>
  );
}
