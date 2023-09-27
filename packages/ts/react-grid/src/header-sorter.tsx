import { GridSorter } from '@hilla/react-components/GridSorter.js';
import { useContext, type ReactElement } from 'react';
import { ColumnContext } from './header-column-context.js';

export function HeaderSorter(): ReactElement {
  const context = useContext(ColumnContext)!;
  return <GridSorter path={context.propertyInfo.name}>{context.propertyInfo.humanReadableName}</GridSorter>;
}
