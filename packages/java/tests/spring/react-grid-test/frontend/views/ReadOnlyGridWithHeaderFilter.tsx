import type { AbstractModel, DetachedModelConstructor } from '@hilla/form';
import { Button } from '@hilla/react-components/Button.js';
import { AutoGrid, type CrudService } from '@hilla/react-crud';
import { useState } from 'react';
import CompanyModel from 'Frontend/generated/dev/hilla/test/reactgrid/CompanyModel.js';
import PersonModel from 'Frontend/generated/dev/hilla/test/reactgrid/PersonModel.js';
import { CompanyService, PersonService } from 'Frontend/generated/endpoints.js';

export function ReadOnlyGridWithHeaderFilters(): JSX.Element {
  const [model, setModel] = useState<Array<DetachedModelConstructor<AbstractModel>>>([PersonModel]);
  const [service, setService] = useState<CrudService<any>>(PersonService);
  const [noHeaderFilters, setNoHeaderFilters] = useState(false);
  return (
    <>
      <Button
        onClick={(e) => {
          if (model[0] === PersonModel) {
            setModel([CompanyModel]);
            setService(CompanyService);
          } else {
            setModel([PersonModel]);
            setService(PersonService);
          }
        }}
      >
        Change bean type
      </Button>
      <Button
        onClick={(e) => {
          setNoHeaderFilters(!noHeaderFilters);
        }}
      >
        Toggle header filters
      </Button>
      <AutoGrid pageSize={10} service={service} model={model[0]} noHeaderFilters={noHeaderFilters} />
    </>
  );
  /* page size is defined only to make testing easier */
}
