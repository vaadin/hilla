import { type AbstractModel, type DetachedModelConstructor, ValidationError } from '@hilla/form';
import { Button } from '@hilla/react-components/Button.js';
import { HorizontalLayout } from '@hilla/react-components/HorizontalLayout.js';
import { VerticalLayout } from '@hilla/react-components/VerticalLayout.js';
import { useForm } from '@hilla/react-form';
import React, { type JSX, useEffect, useState } from 'react';
import { AutoFormField } from './autoform-field.js';
import type { CrudService } from './crud.js';
import { getProperties, includeProperty } from './property-info.js';
import { FormLayout } from '@hilla/react-components/FormLayout';

export const emptyItem = Symbol();

export const noCustomFormLayout = Symbol();

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
  customFormLayout?: AutoFormLayoutProps | typeof noCustomFormLayout;
  onSubmitError?({ error }: SubmitErrorEvent): void;
  afterSubmit?({ item }: SubmitEvent<TItem>): void;
}>;

interface FieldColSpan {
  property: string;
  colSpan: number;
}

type AutoFormLayoutProps = Readonly<{
  template: string[][] | FieldColSpan[][];
  responsiveSteps?: { minWidth: string; columns: number }[];
}>;

export function ExperimentalAutoForm<TItem>({
  service,
  model,
  item = emptyItem,
  onSubmitError,
  afterSubmit,
  disabled,
  customFormLayout = noCustomFormLayout,
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

  function createFormActions(isEditMode: boolean, disabled: boolean | undefined): JSX.Element {
    return (
      <HorizontalLayout theme="spacing" style={{ marginTop: 'var(--lumo-space-m)', alignSelf: 'flex-end' }}>
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
    );
  }

  function findColumnCount(responsiveSteps: { minWidth: string; columns: number }[]): number {
    return responsiveSteps
      .map((step) => {
        let minWidthNum: number;
        if (step.minWidth.includes('px')) {
          minWidthNum = parseInt(step.minWidth.substring(0, step.minWidth.length - 2));
        } else {
          minWidthNum = parseInt(step.minWidth);
        }
        return { minWidth: minWidthNum, columns: step.columns };
      })
      .filter(({ minWidth }) => {
        return minWidth <= window.innerWidth;
      })
      .reduce(
        (maxStep, step) => {
          if (step.minWidth > maxStep.minWidth) {
            return step;
          } else {
            return maxStep;
          }
        },
        { minWidth: 0, columns: 1 },
      ).columns;
  }

  function createGenericResponsiveSteps(
    customFormLayout: AutoFormLayoutProps,
  ): { minWidth: string; columns: number }[] {
    function gcd(a: number, b: number): number {
      return a > 0 ? gcd(b % a, a) : b;
    }
    const lcm = (a: number, b: number) => (a * b) / gcd(a, b);

    const minNeededColumns = customFormLayout.template
      .map((row) => row.length)
      .filter((value, index, array) => array.indexOf(value) === index)
      .reduce(lcm);

    return [
      { minWidth: '0', columns: 1 },
      { minWidth: '800px', columns: minNeededColumns },
    ];
  }

  function createCustomFormLayout(customFormLayout: AutoFormLayoutProps): JSX.Element {
    const fieldsByPropertyName = new Map<string, JSX.Element>();
    getProperties(model)
      .filter(includeProperty)
      .forEach((propertyInfo) =>
        fieldsByPropertyName.set(
          propertyInfo.name,
          <AutoFormField key={propertyInfo.name} propertyInfo={propertyInfo} form={form} disabled={disabled} />,
        ),
      );

    let responsiveSteps: { minWidth: string; columns: number }[];
    if (customFormLayout.responsiveSteps == null) {
      responsiveSteps = createGenericResponsiveSteps(customFormLayout);
    } else {
      responsiveSteps = customFormLayout.responsiveSteps;
    }

    let weightedTemplate: FieldColSpan[][];
    if (typeof customFormLayout.template[0][0] === 'string') {
      const columnCount = findColumnCount(responsiveSteps);
      weightedTemplate = (customFormLayout.template as string[][]).map((row) => {
        const rowSize = row.length;
        const colSpan = Math.ceil(columnCount / rowSize);
        return row.map((property) => {
          return { property, colSpan: colSpan };
        });
      });
    } else {
      weightedTemplate = customFormLayout.template as FieldColSpan[][];
    }

    const spannedFields: JSX.Element[] = [];
    weightedTemplate.forEach((row: FieldColSpan[]) => {
      row.forEach((fieldColSpan: FieldColSpan) => {
        const field = fieldsByPropertyName.get(fieldColSpan.property)!;
        const spannedField = React.cloneElement(field, { colspan: fieldColSpan.colSpan });
        spannedFields.push(spannedField);
      });
    });

    return (
      <section className="flex flex-col p-m gap-m">
        <FormLayout responsiveSteps={responsiveSteps}>{spannedFields}</FormLayout>
        {formError ? <div style={{ color: 'var(--lumo-error-color)' }}>{formError}</div> : <></>}
        {createFormActions(isEditMode, disabled)}
      </section>
    );
  }

  if (customFormLayout !== noCustomFormLayout) {
    return createCustomFormLayout(customFormLayout);
  }

  return (
    <section className="flex flex-col p-m gap-m">
      <FormLayout>
        {getProperties(model)
          .filter(includeProperty)
          .map((propertyInfo) => (
            <AutoFormField key={propertyInfo.name} propertyInfo={propertyInfo} form={form} disabled={disabled} />
          ))}
      </FormLayout>
      {formError ? <div style={{ color: 'var(--lumo-error-color)' }}>{formError}</div> : <></>}
      {createFormActions(isEditMode, disabled)}
    </section>
  );
}
