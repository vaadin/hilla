import { GridSorter } from '@hilla/react-components/GridSorter.js';
import { useContext, type ReactElement } from 'react';
import { ColumnContext } from './autogrid-column-context.js';
import { hasAnnotation } from './property-info.js';

export function HeaderSorter(): ReactElement | undefined {
  const context = useContext(ColumnContext)!;
  const sorterState = context.sortState[context.propertyInfo.name];
  const direction = sorterState?.direction ?? null;
  const headerLabel = context.customColumnOptions?.header ?? context.propertyInfo.humanReadableName;

  return hasAnnotation(context.propertyInfo.meta, 'jakarta.persistence.Column') ? (
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
  ) : (
    <>{headerLabel}</>
  );
}
