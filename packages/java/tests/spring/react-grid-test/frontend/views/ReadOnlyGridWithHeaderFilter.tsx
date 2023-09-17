import { AutoGrid } from '@hilla/react-grid';
import PersonModel from 'Frontend/generated/dev/hilla/test/reactgrid/PersonModel';
import { PersonService } from 'Frontend/generated/endpoints';

export function ReadOnlyGridWithHeaderFilters() {
  return <AutoGrid pageSize={10} service={PersonService} model={PersonModel} headerFilters />;
  /* page size is defined only to make testing easier */
}
