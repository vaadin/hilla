import { AutoGrid, useDataProvider } from '@hilla/react-crud';
import type Person from 'Frontend/generated/dev/hilla/test/reactgrid/Person.js';
import PersonModel from 'Frontend/generated/dev/hilla/test/reactgrid/PersonModel.js';
import { PersonListOnlyService } from 'Frontend/generated/endpoints.js';
import { Grid } from '@hilla/react-components/Grid';
import { GridColumn } from '@hilla/react-components/GridColumn';
import { GridSortColumn } from '@hilla/react-components/GridSortColumn';

type GridBodyReactRendererProps<TItem> = {
  item: TItem;
};

function LuckyNumberRenderer({ item }: GridBodyReactRendererProps<Person>): JSX.Element {
  const value = item.luckyNumber;
  return <span style={{ fontWeight: 'bold', color: value % 2 === 0 ? 'green' : 'red' }}>{value}</span>;
}

export function CustomGrid(): JSX.Element {
  const { gridRef } = useDataProvider(PersonListOnlyService);

  return (
    <Grid pageSize={10} ref={gridRef as any}>
      <GridSortColumn path="firstName" />
      <GridSortColumn path="lastName" />
      <GridSortColumn path="gender" />
      <GridSortColumn path="luckyNumber" renderer={LuckyNumberRenderer} />
    </Grid>
  );
  /* page size is defined only to make testing easier */
}
