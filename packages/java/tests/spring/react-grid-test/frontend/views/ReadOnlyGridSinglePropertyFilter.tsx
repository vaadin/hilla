import { TextField } from '@hilla/react-components/TextField.js';
import { AutoGrid } from '@hilla/react-crud';
import Filter from 'Frontend/generated/dev/hilla/crud/filter/Filter.js';
import PersonModel from 'Frontend/generated/dev/hilla/test/reactgrid/PersonModel.js';
import { PersonService } from 'Frontend/generated/endpoints.js';
import { useState } from 'react';

export function ReadOnlyGridSinglePropertyFilter() {
  const [filter, setFilter] = useState<Filter | undefined>(undefined);
  return (
    <div>
      <TextField
        id="filter"
        label="Search for first name"
        onValueChanged={(e: any) => {
          const propertyFilter: any = {
            t: 'propertyString',
            propertyId: 'firstName',
            matcher: 'CONTAINS',
            filterValue: e.detail.value,
          };
          setFilter(propertyFilter);
        }}
      ></TextField>
      <AutoGrid pageSize={10} service={PersonService} experimentalFilter={filter} model={PersonModel} noHeaderFilters />
    </div>
    /* page size is defined only to make testing easier */
  );
}
