import { ExperimentalAutoCrud } from '@hilla/react-grid';
import PersonModel from 'Frontend/generated/dev/hilla/test/reactgrid/PersonModel';
import { PersonService } from 'Frontend/generated/endpoints';

export function AutoCrudView(): JSX.Element {
  return <ExperimentalAutoCrud service={PersonService} model={PersonModel} {...{ pageSize: 10 }} header="My crud" />;
  /* page size is defined only to make testing easier */
}
