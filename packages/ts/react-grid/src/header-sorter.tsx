import { GridSorter } from '@hilla/react-components/GridSorter.js';
import { useContext, type ReactElement } from 'react';
import { HeaderColumnContext } from './header-column-context.js';

export function HeaderSorter(): ReactElement {
  const context = useContext(HeaderColumnContext)!;
  return (
    <GridSorter
      direction={context.sortDirection}
      path={context.propertyInfo.name}
      onDirectionChanged={(e) => {
        context.sortDirection = e.detail.value;
      }}
    >
      {context.propertyInfo.humanReadableName}
    </GridSorter>
  );
}
