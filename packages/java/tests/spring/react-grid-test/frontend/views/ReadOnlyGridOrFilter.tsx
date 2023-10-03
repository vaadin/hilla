import { TextField } from '@hilla/react-components/TextField.js';
import { AutoGrid } from '@hilla/react-grid';
import Filter from 'Frontend/generated/dev/hilla/crud/filter/Filter';
import PersonModel from 'Frontend/generated/dev/hilla/test/reactgrid/PersonModel';
import { PersonService } from 'Frontend/generated/endpoints';
import { useState } from 'react';

export function ReadOnlyGridOrFilter() {
  const [filter, setFilter] = useState<Filter | undefined>(undefined);

  return (
    <div>
      <TextField
        id="filter"
        style={{ width: '20em' }}
        label="Search for first or last name"
        onValueChanged={(e: any) => {
          const firstNameFilter: any = {
            t: 'propertyString',
            propertyId: 'firstName',
            matcher: 'CONTAINS',
            filterValue: e.detail.value,
          };
          const lasttNameFilter: any = {
            t: 'propertyString',
            propertyId: 'lastName',
            matcher: 'CONTAINS',
            filterValue: e.detail.value,
          };
          setFilter({ t: 'or', children: [firstNameFilter, lasttNameFilter] });
        }}
      ></TextField>
      <AutoGrid pageSize={10} service={PersonService} model={PersonModel} filter={filter} noHeaderFilters />
    </div>
    /* page size is defined only to make testing easier */
  );
}
