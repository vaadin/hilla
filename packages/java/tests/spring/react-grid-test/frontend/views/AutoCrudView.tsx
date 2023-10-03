import { ExperimentalAutoCrud } from '@hilla/react-grid';
import PersonModel from 'Frontend/generated/dev/hilla/test/reactgrid/PersonModel';
import { PersonService } from 'Frontend/generated/endpoints';

export function AutoCrudView() {
  return <ExperimentalAutoCrud service={PersonService} model={PersonModel} {...{ pageSize: 10 }} />;
  /* page size is defined only to make testing easier */
}
