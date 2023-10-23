import { TextField } from '@hilla/react-components/TextField.js';
import { AutoGrid } from '@hilla/react-crud';
import { useState } from 'react';
import type FilterUnion from 'Frontend/generated/dev/hilla/crud/filter/FilterUnion.js';
import PersonModel from 'Frontend/generated/dev/hilla/test/reactgrid/PersonModel.js';
import { PersonService } from 'Frontend/generated/endpoints.js';

export function ReadOnlyGridOrFilter(): JSX.Element {
  const [filter, setFilter] = useState<FilterUnion | undefined>(undefined);

  return (
    <div>
      <TextField
        id="filter"
        style={{ width: '20em' }}
        label="Search for first or last name"
        onValueChanged={({ detail: { value } }) => {
          const firstNameFilter = {
            '@type': 'propertyString',
            propertyId: 'firstName',
            matcher: 'CONTAINS',
            filterValue: value,
          };
          const lasttNameFilter = {
            '@type': 'propertyString',
            propertyId: 'lastName',
            matcher: 'CONTAINS',
            filterValue: value,
          };
          setFilter({ '@type': 'or', children: [firstNameFilter, lasttNameFilter] });
        }}
      ></TextField>
      <AutoGrid pageSize={10} service={PersonService} model={PersonModel} experimentalFilter={filter} noHeaderFilters />
    </div>
    /* page size is defined only to make testing easier */
  );
}
