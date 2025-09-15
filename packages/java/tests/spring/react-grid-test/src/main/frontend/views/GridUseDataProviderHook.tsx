import { useDataProvider } from '@vaadin/hilla-react-crud';
import type Person from 'Frontend/generated/com/vaadin/hilla/test/reactgrid/Person.js';
import { PersonService } from 'Frontend/generated/endpoints.js';
import { Grid } from '@vaadin/react-components/Grid';
import { GridSortColumn } from '@vaadin/react-components/GridSortColumn';
import { Button } from '@vaadin/react-components/Button.js';
import { TextField } from '@vaadin/react-components/TextField.js';
import PropertyStringFilter from 'Frontend/generated/com/vaadin/hilla/crud/filter/PropertyStringFilter';
import { useState } from 'react';
import Matcher from 'Frontend/generated/com/vaadin/hilla/crud/filter/PropertyStringFilter/Matcher';
import Gender from 'Frontend/generated/com/vaadin/hilla/test/reactgrid/Person/Gender';

type GridBodyReactRendererProps<TItem> = {
  item: TItem;
};

function LuckyNumberRenderer({ item }: GridBodyReactRendererProps<Person>): React.JSX.Element {
  const value = item.luckyNumber;
  return <span style={{ fontWeight: 'bold', color: value % 2 === 0 ? 'green' : 'red' }}>{value}</span>;
}

export default function GridUseDataProviderHook(): React.JSX.Element {
  const [filter, setFilter] = useState<PropertyStringFilter>();
  const { dataProvider, refresh } = useDataProvider(PersonService, filter);
  const [savedPerson, setSavedPerson] = useState<Person | undefined>(undefined);
  const newPerson = {
    firstName: 'New',
    lastName: 'Person',
    gender: Gender.NON_BINARY,
    luckyNumber: 100,
    averageGrade: 4,
    emailVerified: true,
    birthDate: '2000-01-01',
    shiftStart: '08:00',
    appointmentTime: '',
  } as Person;

  function addNewPerson() {
    PersonService.save(newPerson).then(setSavedPerson);
  }

  function removeNewPerson() {
    if (savedPerson) {
      PersonService.delete(savedPerson!.id!).then(() => setSavedPerson(undefined));
    }
  }

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
        <TextField id="filter" label="Filter by last name" onInput={handleFilterChange} />
        <Button onClick={refresh}>Refresh</Button>
        <Button onClick={addNewPerson}>Add Person</Button>
        <Button onClick={removeNewPerson}>Remove Person</Button>
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
