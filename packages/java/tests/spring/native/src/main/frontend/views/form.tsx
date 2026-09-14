import type { ViewConfig } from '@vaadin/hilla-file-router/types.js';
import { AutoForm } from '@vaadin/hilla-react-crud';
import { useEffect, useState } from 'react';
import type Person from 'Frontend/generated/com/example/application/service/Person.js';
import PersonModel from 'Frontend/generated/com/example/application/service/PersonModel.js';
import { PersonService } from 'Frontend/generated/endpoints.js';

export const config: ViewConfig = {
  title: 'Form',
};

export default function FormView(): React.JSX.Element {
  const [person, setPerson] = useState<Person | undefined>(undefined);
  const [saved, setSaved] = useState<string | undefined>(undefined);

  useEffect(() => {
    PersonService.get(13).then(setPerson);
  }, []);

  if (!person) {
    return <span id="loading">Loading</span>;
  }

  return (
    <>
      <AutoForm
        service={PersonService}
        model={PersonModel}
        item={person}
        onSubmitSuccess={({ item }) => setSaved(item.lastName)}
      />
      {saved ? <span id="saved">{saved}</span> : <></>}
    </>
  );
}
