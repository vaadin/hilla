import { AutoCrud } from '@vaadin/hilla-react-crud';
import PersonModel from 'Frontend/generated/com/vaadin/hilla/test/reactgrid/PersonModel.js';
import { PersonService } from 'Frontend/generated/endpoints.js';

export function AutoCrudView(): JSX.Element {
  /* page size is defined only to make testing easier */
  return (
    <div className="p-l">
      <AutoCrud service={PersonService} model={PersonModel} {...{ pageSize: 10 }} />
    </div>
  );
}
