import { TextField } from '@hilla/react-components/TextField.js';
import { AutoGrid } from '@hilla/react-crud';
import FilterUnion from 'Frontend/generated/dev/hilla/crud/filter/FilterUnion';
import PersonModel from 'Frontend/generated/dev/hilla/test/reactgrid/PersonModel';
import { PersonService } from 'Frontend/generated/endpoints';
import { useState } from 'react';

export function ReadOnlyGridOrFilter() {
  const [filter, setFilter] = useState<FilterUnion | undefined>(undefined);

  return (
    <div>
      <TextField
        id="filter"
        style={{ width: '20em' }}
        label="Search for first or last name"
        onValueChanged={(e: any) => {
          const firstNameFilter: any = {
            '@type': 'propertyString',
            propertyId: 'firstName',
            matcher: 'CONTAINS',
            filterValue: e.detail.value,
          };
          const lasttNameFilter: any = {
            '@type': 'propertyString',
            propertyId: 'lastName',
            matcher: 'CONTAINS',
            filterValue: e.detail.value,
          };
          setFilter({ '@type': 'or', children: [firstNameFilter, lasttNameFilter] });
        }}
      ></TextField>
      <AutoGrid pageSize={10} service={PersonService} model={PersonModel} experimentalFilter={filter} noHeaderFilters />
    </div>
    /* page size is defined only to make testing easier */
  );
}
