import { ModelConstructor } from '@hilla/form';
import { Button } from '@hilla/react-components/Button.js';
import { AutoGrid, CrudService } from '@hilla/react-grid';
import CompanyModel from 'Frontend/generated/dev/hilla/test/reactgrid/CompanyModel';
import PersonModel from 'Frontend/generated/dev/hilla/test/reactgrid/PersonModel';
import { CompanyService, PersonService } from 'Frontend/generated/endpoints';
import { useState } from 'react';

export function ReadOnlyGridWithHeaderFilters() {
  const [model, setModel] = useState<ModelConstructor<any, any>[]>([PersonModel]);
  const [service, setService] = useState<CrudService<any>>(PersonService);
  const [headerFilters, setHeaderFilters] = useState(true);
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
          setHeaderFilters(!headerFilters);
        }}
      >
        Toggle header filters
      </Button>
      <AutoGrid pageSize={10} service={service} model={model[0]} headerFilters={headerFilters} />
    </>
  );
  /* page size is defined only to make testing easier */
}
