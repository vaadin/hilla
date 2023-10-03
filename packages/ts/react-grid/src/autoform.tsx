import { ValidationError, type AbstractModel, type DetachedModelConstructor } from '@hilla/form';
import { Button } from '@hilla/react-components/Button.js';
import { HorizontalLayout } from '@hilla/react-components/HorizontalLayout.js';
import { VerticalLayout } from '@hilla/react-components/VerticalLayout.js';
import { useForm } from '@hilla/react-form';
import { useEffect, useState, type JSX } from 'react';
import { AutoFormField } from './autoform-field';
import type { CrudService } from './crud';
import { getProperties, includeProperty } from './property-info';

type SubmitErrorEvent = {
  error: unknown;
};
type SubmitEvent<TItem> = {
  item: TItem;
};
export type AutoFormProps<TItem> = Readonly<{
  service: CrudService<TItem>;
  model: DetachedModelConstructor<AbstractModel<TItem>>;
  item?: TItem;
  disabled?: boolean;
  onSubmitError?({ error }: SubmitErrorEvent): void;
  afterSubmit?({ item }: SubmitEvent<TItem>): void;
}>;

export function ExperimentalAutoForm<TItem>({
  service,
  model,
  item,
  onSubmitError,
  afterSubmit,
  disabled,
}: AutoFormProps<TItem>): JSX.Element {
  const form = useForm(model, {
    onSubmit: async (formItem) => service.save(formItem),
  });
  const [formError, setFormError] = useState('');
  useEffect(() => {
    form.read(item);
  }, [item]);

  async function submitButtonClicked(): Promise<void> {
    try {
      setFormError('');
      const newItem = await form.submit();
      if (!newItem) {
        // If update returns an empty object, then no update was performed
        throw new Error('generic error');
      } else if (afterSubmit) {
        afterSubmit({ item: newItem });
      }
    } catch (error) {
      if (error instanceof ValidationError) {
        // Handled automatically
        return;
      }
      const genericError = 'Something went wrong, please check all your values';
      if (onSubmitError) {
        onSubmitError({ error });
      } else {
        setFormError(genericError);
      }
    }
  }

  return (
    <VerticalLayout theme="padding">
      {getProperties(model)
        .filter(includeProperty)
        .map((propertyInfo) => (
          <AutoFormField
            key={propertyInfo.name}
            propertyInfo={propertyInfo}
            form={form}
            disabled={disabled}
          ></AutoFormField>
        ))}
      {formError ? <div style={{ color: 'var(--lumo-error-color)' }}>{formError}</div> : <></>}
      <HorizontalLayout style={{ marginTop: 'var(--lumo-space-m)' }}>
        <Button
          disabled={disabled}
          // eslint-disable-next-line @typescript-eslint/no-misused-promises
          onClick={submitButtonClicked}
        >
          Submit
        </Button>
      </HorizontalLayout>
    </VerticalLayout>
  );
}
