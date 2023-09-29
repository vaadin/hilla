import { AutoGrid } from '@hilla/react-grid';
import PersonModel from 'Frontend/generated/com/example/application/service/PersonModel';
import { PersonService } from 'Frontend/generated/endpoints';

export function ReadOnlyGrid() {
  return <AutoGrid pageSize={10} service={PersonService} model={PersonModel}/>;
}
