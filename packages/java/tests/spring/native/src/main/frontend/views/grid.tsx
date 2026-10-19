import type { ViewConfig } from '@vaadin/hilla-file-router/types.js';
import { AutoGrid } from '@vaadin/hilla-react-crud';
import PersonModel from 'Frontend/generated/com/example/application/service/PersonModel.js';
import { PersonService } from 'Frontend/generated/endpoints.js';

export const config: ViewConfig = {
  title: 'Grid',
};

export default function GridView(): React.JSX.Element {
  /* The page size is set only to make the ITs easier to write. */
  return <AutoGrid pageSize={10} service={PersonService} model={PersonModel} />;
}
