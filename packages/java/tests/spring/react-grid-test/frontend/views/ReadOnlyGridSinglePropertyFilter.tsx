import { TextField } from '@hilla/react-components/TextField.js';
import { AutoGrid } from '@hilla/react-grid';
import Filter from 'Frontend/generated/dev/hilla/crud/filter/Filter';
import PersonModel from 'Frontend/generated/dev/hilla/test/reactgrid/PersonModel';
import { PersonService } from 'Frontend/generated/endpoints';
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
      <AutoGrid pageSize={10} service={PersonService} filter={filter} model={PersonModel} />
    </div>
    /* page size is defined only to make testing easier */
  );
}
