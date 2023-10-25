import { type AbstractModel, type DetachedModelConstructor, ValidationError } from '@hilla/form';
import { Button } from '@hilla/react-components/Button.js';
import { HorizontalLayout } from '@hilla/react-components/HorizontalLayout.js';
import { VerticalLayout } from '@hilla/react-components/VerticalLayout.js';
import { useForm } from '@hilla/react-form';
import { type JSX, useEffect, useState } from 'react';
import { AutoFormField } from './autoform-field.js';
import type { CrudService } from './crud.js';
import { getProperties, includeProperty } from './property-info.js';

export const emptyItem = Symbol();

type SubmitErrorEvent = {
  error: unknown;
};
type SubmitEvent<TItem> = {
  item: TItem;
};

export type AutoFormProps<TItem> = Readonly<{
  service: CrudService<TItem>;
  model: DetachedModelConstructor<AbstractModel<TItem>>;
  item?: TItem | typeof emptyItem;
  disabled?: boolean;
  onSubmitError?({ error }: SubmitErrorEvent): void;
  afterSubmit?({ item }: SubmitEvent<TItem>): void;
}>;

export function ExperimentalAutoForm<TItem>({
  service,
  model,
  item = emptyItem,
  onSubmitError,
  afterSubmit,
  disabled,
}: AutoFormProps<TItem>): JSX.Element {
  const form = useForm(model, {
    onSubmit: async (formItem) => service.save(formItem),
  });
  const [formError, setFormError] = useState('');
  useEffect(() => {
    if (item !== emptyItem) {
      form.read(item);
    } else {
      form.clear();
    }
  }, [item]);

  async function submitButtonClicked(): Promise<void> {
    try {
      setFormError('');
      const newItem = await form.submit();
      if (newItem === undefined) {
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

  const isEditMode = item !== undefined && item !== null && item !== emptyItem;

  return (
    <VerticalLayout className="auto-form" data-testid="auto-form">
      <VerticalLayout className="auto-form-fields">
        {getProperties(model)
          .filter(includeProperty)
          .map((propertyInfo) => (
            <AutoFormField key={propertyInfo.name} propertyInfo={propertyInfo} form={form} disabled={disabled} />
          ))}
        {formError ? <div style={{ color: 'var(--lumo-error-color)' }}>{formError}</div> : <></>}
      </VerticalLayout>
      <HorizontalLayout className="auto-form-toolbar" theme="spacing">
        {form.dirty ? (
          <Button theme="tertiary" onClick={() => form.reset()}>
            Discard
          </Button>
        ) : null}
        <Button
          theme="primary"
          disabled={!!disabled || (isEditMode && !form.dirty)}
          // eslint-disable-next-line @typescript-eslint/no-misused-promises
          onClick={submitButtonClicked}
        >
          Submit
        </Button>
      </HorizontalLayout>
    </VerticalLayout>
  );
}
