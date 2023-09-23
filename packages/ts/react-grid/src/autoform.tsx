import { type AbstractModel, ValidationError, type DetachedModelConstructor } from '@hilla/form';
import { Button } from '@hilla/react-components/Button.js';
import { HorizontalLayout } from '@hilla/react-components/HorizontalLayout.js';
import { VerticalLayout } from '@hilla/react-components/VerticalLayout.js';
import { useForm } from '@hilla/react-form';
import { useEffect, useState, type JSX } from 'react';
import { AutoFormField } from './autoform-field';
import type { CrudService } from './crud';
import { getProperties } from './utils';

type SubmitErrorEvent = {
  errorMessage: string;
};
type SubmitEvent<TItem> = {
  item: TItem;
};
export type AutoFormProps<TItem> = Readonly<{
  service: CrudService<TItem>;
  model: DetachedModelConstructor<AbstractModel<TItem>>;
  item?: TItem;
  onSubmitError?({ errorMessage }: SubmitErrorEvent): void;
  onSubmit?({ item }: SubmitEvent<TItem>): void;
}>;

export function AutoForm<TItem>({ service, model, item, onSubmitError, onSubmit }: AutoFormProps<TItem>): JSX.Element {
  const form = useForm(model, {
    onSubmit: async (formItem) => service.update(formItem),
  });
  const [formError, setFormError] = useState('');
  useEffect(() => {
    form.read(item);
  }, [item]);

  return (
    <VerticalLayout theme="padding spacing">
      {getProperties(model).map((propertyInfo) => (
        <AutoFormField
          key={`auto-form-field-${propertyInfo.name}`}
          propertyInfo={propertyInfo}
          form={form}
        ></AutoFormField>
      ))}
      <div id="formerror" style={{ color: 'red' }}>
        {formError}
      </div>
      <HorizontalLayout theme="spacing">
        <Button
          // eslint-disable-next-line @typescript-eslint/no-misused-promises
          onClick={async () => {
            try {
              setFormError('');
              const newItem = await form.submit();
              if (!newItem) {
                // If update returns an empty object, then no update was performed
                throw new Error('generic error');
              } else if (onSubmit) {
                onSubmit({ item: newItem });
              } else {
                form.clear();
              }
            } catch (e) {
              if (e instanceof ValidationError) {
                // Handled automatically
                return;
              }
              const genericError = 'Something went wrong, please check all your values';
              if (onSubmitError) {
                onSubmitError({ errorMessage: genericError });
              } else {
                setFormError(genericError);
              }
            }
          }}
        >
          Submit
        </Button>
      </HorizontalLayout>
    </VerticalLayout>
  );
}
