import { TextField } from '@vaadin/react-components/TextField.js';
import { AutoGrid, HeaderFilterRendererProps } from '@vaadin/hilla-react-crud';
import PersonModel from 'Frontend/generated/com/vaadin/hilla/test/reactgrid/PersonModel.js';
import Person from 'Frontend/generated/com/vaadin/hilla/test/reactgrid/Person';
import { PersonService } from 'Frontend/generated/endpoints.js';
import { GridColumn } from '@vaadin/react-components/GridColumn';
import type FilterUnion from 'Frontend/generated/com/vaadin/hilla/crud/filter/FilterUnion.js';
import type OrFilter from 'Frontend/generated/com/vaadin/hilla/crud/filter/OrFilter.js';

const HeaderFilterRenderer = ({ setFilter }: HeaderFilterRendererProps) => {
  return (
    <TextField
      id="filter"
      style={{ width: '20em' }}
      placeholder="Search for first or last name"
      onValueChanged={({ detail: { value } }) => {
        const firstNameFilter = {
          '@type': 'propertyString',
          propertyId: 'firstName',
          matcher: 'CONTAINS',
          filterValue: value,
        };
        const lastNameFilter = {
          '@type': 'propertyString',
          propertyId: 'lastName',
          matcher: 'CONTAINS',
          filterValue: value,
        };

        const filter: OrFilter = {
          '@type': 'or',
          children: [firstNameFilter, lastNameFilter],
        };

        setFilter(filter as FilterUnion);
      }}
    ></TextField>
  );
};

const FullNameRenderer = ({ item }: { item: Person }): React.JSX.Element => (
  <span>
    {item.firstName} {item.lastName}
  </span>
);

const HeaderRenderer = () => <div>Full Name (currently: {new Date().toLocaleString()})</div>;

export default function ReadOnlyGridCustomFilter(): React.JSX.Element {
  return (
    <div>
      <AutoGrid
        service={PersonService}
        model={PersonModel}
        visibleColumns={['firstName', 'lastName', 'gender', 'fullName']}
        customColumns={[<GridColumn key="fullName" autoWidth renderer={FullNameRenderer}></GridColumn>]}
        columnOptions={{
          lastName: { filterPlaceholder: 'Search for last name' },
          fullName: { headerRenderer: HeaderRenderer, headerFilterRenderer: HeaderFilterRenderer },
        }}
      />
    </div>
    /* page size is defined only to make testing easier */
  );
}
