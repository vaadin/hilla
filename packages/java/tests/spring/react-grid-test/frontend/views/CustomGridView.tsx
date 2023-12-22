import { useDataProvider } from '@hilla/react-crud';
import type Person from 'Frontend/generated/dev/hilla/test/reactgrid/Person.js';
import { PersonListOnlyService } from 'Frontend/generated/endpoints.js';
import { Grid } from '@hilla/react-components/Grid';
import { GridSortColumn } from '@hilla/react-components/GridSortColumn';
import { Button } from '@hilla/react-components/Button.js';
import { TextField } from '@hilla/react-components/TextField.js';
import PropertyStringFilter from 'Frontend/generated/dev/hilla/crud/filter/PropertyStringFilter';
import { useState } from 'react';
import Matcher from 'Frontend/generated/dev/hilla/crud/filter/PropertyStringFilter/Matcher';

type GridBodyReactRendererProps<TItem> = {
  item: TItem;
};

function LuckyNumberRenderer({ item }: GridBodyReactRendererProps<Person>): JSX.Element {
  const value = item.luckyNumber;
  return <span style={{ fontWeight: 'bold', color: value % 2 === 0 ? 'green' : 'red' }}>{value}</span>;
}

export function CustomGrid(): JSX.Element {
  const [filter, setFilter] = useState<PropertyStringFilter>();
  const { dataProvider, refresh } = useDataProvider(PersonListOnlyService, filter);

  function handleFilterChange(event: Event) {
    const filter = (event.target as HTMLInputElement).value;
    setFilter(
      filter
        ? {
            '@type': 'propertyString',
            propertyId: 'lastName',
            filterValue: filter,
            matcher: Matcher.CONTAINS,
          }
        : undefined,
    );
  }

  return (
    <div className="p-m flex flex-col gap-m">
      <div className="flex gap-s items-baseline">
        <TextField label="Filter by last name" onInput={handleFilterChange} />
        <Button onClick={refresh}>Refresh</Button>
      </div>
      <Grid pageSize={10} dataProvider={dataProvider}>
        <GridSortColumn path="firstName" />
        <GridSortColumn path="lastName" />
        <GridSortColumn path="gender" />
        <GridSortColumn path="luckyNumber" renderer={LuckyNumberRenderer} />
      </Grid>
    </div>
  );
}
